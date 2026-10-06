package com.community.eldercare.service;

import com.community.eldercare.common.ApiException;
import com.community.eldercare.domain.*;
import com.community.eldercare.dto.*;
import com.community.eldercare.notify.SseService;
import com.community.eldercare.repo.*;
import com.community.eldercare.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 无法开门多方确认处置服务。
 * 社工到达后老人无响应但屋内有灯光时启动：平台引导值班人员依次联系
 * 家属 → 物业 → 邻居 → 派出所，记录每一步联系人、时间与反馈；
 * 需要破门或请锁匠时保存授权依据、现场见证人与后续修复费用处理；
 * 家属到场后可查看完整处置轨迹并确认费用承担方案。
 */
@Service
public class DoorEscalationService {

    /** 多方联系顺序：家属 → 物业 → 邻居 → 派出所 */
    public static final List<String> PARTY_ORDER = List.of("FAMILY", "PROPERTY", "NEIGHBOR", "POLICE");

    private static final Set<String> OPEN_RESOLUTIONS = Set.of("FAMILY_OPENED", "PROPERTY_OPENED", "NEIGHBOR_OPENED");
    private static final Set<String> ENTRY_METHODS = Set.of("LOCKSMITH", "FORCED_ENTRY");
    private static final Set<String> AUTH_BASIS = Set.of("FAMILY_PHONE", "FAMILY_ONSITE", "POLICE_ONSITE", "COMMUNITY_EMERGENCY");
    private static final Set<String> COST_TYPES = Set.of("LOCKSMITH_FEE", "LOCK_REPLACE", "DOOR_WINDOW_REPAIR", "OTHER");
    private static final Set<String> ADVANCE_BY = Set.of("COMMUNITY", "FAMILY", "PROPERTY", "NONE");
    private static final Set<String> BEARERS = Set.of("PENDING", "FAMILY", "COMMUNITY_FUND", "INSURANCE");
    private static final Set<String> PLACEMENTS = Set.of("HOME", "HOSPITAL", "FAMILY_CARE", "TEMP_CARE");
    private static final Set<String> OPINIONS = Set.of("AGREE", "PARTIAL", "DISAGREE");

    private final DoorEscalationRepository escRepo;
    private final DoorContactStepRepository stepRepo;
    private final ForcedEntryRepository entryRepo;
    private final DoorCostItemRepository costRepo;
    private final DoorFamilyReviewRepository reviewRepo;
    private final HelpEventRepository eventRepo;
    private final EventLogRepository logRepo;
    private final SseService sse;

    public DoorEscalationService(DoorEscalationRepository escRepo, DoorContactStepRepository stepRepo,
                                 ForcedEntryRepository entryRepo, DoorCostItemRepository costRepo,
                                 DoorFamilyReviewRepository reviewRepo, HelpEventRepository eventRepo,
                                 EventLogRepository logRepo, SseService sse) {
        this.escRepo = escRepo;
        this.stepRepo = stepRepo;
        this.entryRepo = entryRepo;
        this.costRepo = costRepo;
        this.reviewRepo = reviewRepo;
        this.eventRepo = eventRepo;
        this.logRepo = logRepo;
        this.sse = sse;
    }

    /* ===================== 启动多方确认 ===================== */

    @Transactional
    public DoorEscalation start(Long eventId, DoorEscalationRequest req) {
        HelpEvent e = mustEvent(eventId);
        escRepo.findFirstByEvent_IdAndStatusOrderByStartedAtDesc(eventId, "CONTACTING")
                .ifPresent(x -> {
                    throw new ApiException("该事件已有进行中的多方确认处置（#" + x.getId() + "），请先结案");
                });

        DoorEscalation esc = new DoorEscalation();
        esc.setEvent(e);
        esc.setOnsiteVisitId(req.getOnsiteVisitId());
        esc.setElderNoResponse(req.isElderNoResponse());
        esc.setLightOnInside(req.isLightOnInside());
        esc.setSceneNote(req.getSceneNote());
        esc.setStartedByName(UserContext.currentName());
        esc = escRepo.save(esc);

        // 启动即视为确认“无法开门”风险
        Set<String> flags = flagsOf(e);
        if (flags.add("CANNOT_OPEN_DOOR")) {
            e.setRiskFlags(String.join(",", flags));
            eventRepo.save(e);
        }

        addLog(e, "DOOR", "启动无法开门多方确认：老人"
                + (req.isElderNoResponse() ? "无响应" : "响应情况不明")
                + (req.isLightOnInside() ? "，屋内有灯光" : "")
                + (req.getSceneNote() != null && !req.getSceneNote().isBlank() ? "，现场情况：" + req.getSceneNote() : "")
                + "。请依次联系：家属 → 物业 → 邻居 → 派出所",
                UserContext.currentName());
        broadcast(eventId);
        return esc;
    }

    /* ===================== 登记联系步骤（依次：家属→物业→邻居→派出所） ===================== */

    @Transactional
    public DoorContactStep addContact(Long escalationId, DoorContactRequest req) {
        DoorEscalation esc = mustEsc(escalationId);
        mustBeContacting(esc);
        int idx = PARTY_ORDER.indexOf(req.getPartyType());
        if (idx < 0) {
            throw new ApiException("联系对象类型不合法: " + req.getPartyType());
        }
        // 顺序校验：允许重复联系已联系过的对象，不允许跳过未联系的对象
        List<DoorContactStep> steps = stepRepo.findByEscalation_IdOrderByContactedAtAsc(escalationId);
        int maxIdx = steps.stream().mapToInt(s -> PARTY_ORDER.indexOf(s.getPartyType())).max().orElse(-1);
        if (idx > maxIdx + 1) {
            throw new ApiException("请按顺序联系：请先联系「" + partyLabel(PARTY_ORDER.get(maxIdx + 1)) + "」");
        }

        DoorContactStep s = new DoorContactStep();
        s.setEscalation(esc);
        s.setPartyType(req.getPartyType());
        s.setContactName(req.getContactName());
        s.setContactPhone(req.getContactPhone());
        s.setChannel(req.getChannel() == null || req.getChannel().isBlank() ? "电话" : req.getChannel());
        s.setReached(req.isReached());
        s.setFeedback(req.getFeedback());
        s.setWillComeToScene(req.isWillComeToScene());
        s.setOperatorName(UserContext.currentName());
        s = stepRepo.save(s);

        addLog(esc.getEvent(), "DOOR", "多方确认·联系" + partyLabel(req.getPartyType())
                + " " + req.getContactName()
                + (req.getContactPhone() != null && !req.getContactPhone().isBlank() ? "（" + req.getContactPhone() + "）" : "")
                + "：" + (req.isReached() ? "已联系上" : "未联系上")
                + (req.isWillComeToScene() ? "，将到场协助" : "")
                + (req.getFeedback() != null && !req.getFeedback().isBlank() ? "，反馈：" + req.getFeedback() : ""),
                UserContext.currentName());
        broadcast(esc.getEventId());
        return s;
    }

    /* ===================== 结案：由家属/物业/邻居开门 ===================== */

    @Transactional
    public DoorEscalation resolve(Long escalationId, DoorResolveRequest req) {
        DoorEscalation esc = mustEsc(escalationId);
        mustBeContacting(esc);
        if (!OPEN_RESOLUTIONS.contains(req.getResolution())) {
            throw new ApiException("结案方式不合法（无需破门时仅支持 家属/物业/邻居 开门）：" + req.getResolution());
        }
        esc.setResolution(req.getResolution());
        esc.setResolveNote(req.getResolveNote());
        esc.setStatus("RESOLVED");
        esc.setResolvedAt(LocalDateTime.now());
        esc = escRepo.save(esc);

        addLog(esc.getEvent(), "DOOR", "多方确认结案：" + resolutionLabel(req.getResolution())
                + (req.getResolveNote() != null && !req.getResolveNote().isBlank() ? "，说明：" + req.getResolveNote() : ""),
                UserContext.currentName());
        broadcast(esc.getEventId());
        return esc;
    }

    /* ===================== 破门 / 请锁匠处置 ===================== */

    @Transactional
    public ForcedEntry createForcedEntry(Long escalationId, ForcedEntryRequest req) {
        DoorEscalation esc = mustEsc(escalationId);
        mustBeContacting(esc);
        if (!ENTRY_METHODS.contains(req.getMethod())) {
            throw new ApiException("处置方式不合法: " + req.getMethod());
        }
        if (!AUTH_BASIS.contains(req.getAuthorizationBasis())) {
            throw new ApiException("授权依据不合法: " + req.getAuthorizationBasis());
        }
        if (req.getElderPlacement() != null && !req.getElderPlacement().isBlank()
                && !PLACEMENTS.contains(req.getElderPlacement())) {
            throw new ApiException("老人安置方式不合法: " + req.getElderPlacement());
        }
        if (entryRepo.findByEscalation_Id(escalationId).isPresent()) {
            throw new ApiException("该处置单已登记破门/锁匠处置");
        }
        // 破门救助前应先完成四方联系留痕（家属→物业→邻居→派出所）
        List<DoorContactStep> steps = stepRepo.findByEscalation_IdOrderByContactedAtAsc(escalationId);
        int maxIdx = steps.stream().mapToInt(s -> PARTY_ORDER.indexOf(s.getPartyType())).max().orElse(-1);
        if (maxIdx < PARTY_ORDER.size() - 1) {
            throw new ApiException("破门/锁匠处置前请先依次完成四方联系（当前尚未联系「"
                    + partyLabel(PARTY_ORDER.get(maxIdx + 1)) + "」）");
        }

        LocalDateTime now = LocalDateTime.now();
        ForcedEntry fe = new ForcedEntry();
        fe.setEscalation(esc);
        fe.setMethod(req.getMethod());
        fe.setAuthorizationBasis(req.getAuthorizationBasis());
        fe.setAuthorizationDetail(req.getAuthorizationDetail());
        fe.setAuthorizedBy(req.getAuthorizedBy());
        fe.setAuthorizedAt(now);
        fe.setExecutorName(req.getExecutorName());
        fe.setExecutorPhone(req.getExecutorPhone());
        fe.setExecutedAt(now);
        fe.setWitnessNames(req.getWitnessNames());
        fe.setPropertyWitnessName(req.getPropertyWitnessName());
        fe.setPoliceWitnessName(req.getPoliceWitnessName());
        fe.setDamageDescription(req.getDamageDescription());
        fe.setLockReplaced(req.isLockReplaced());
        fe.setLockReplaceNote(req.getLockReplaceNote());
        fe.setDoorWindowRepaired(req.isDoorWindowRepaired());
        fe.setRepairNote(req.getRepairNote());
        fe.setElderPlacement(req.getElderPlacement());
        fe.setPlacementNote(req.getPlacementNote());
        fe.setCreatedByName(UserContext.currentName());
        fe = entryRepo.save(fe);

        esc.setResolution(req.getMethod());
        esc.setStatus("RESOLVED");
        esc.setResolvedAt(now);
        esc.setResolveNote("授权依据：" + authLabel(req.getAuthorizationBasis()) + "，授权人：" + req.getAuthorizedBy());
        escRepo.save(esc);

        addLog(esc.getEvent(), "DOOR", "采取" + methodLabel(req.getMethod())
                + "。授权依据：" + authLabel(req.getAuthorizationBasis())
                + "（授权人 " + req.getAuthorizedBy() + "）；执行人 " + req.getExecutorName()
                + "；现场见证人 " + req.getWitnessNames()
                + (req.getPropertyWitnessName() != null && !req.getPropertyWitnessName().isBlank()
                ? "；物业见证 " + req.getPropertyWitnessName() : "")
                + (req.getPoliceWitnessName() != null && !req.getPoliceWitnessName().isBlank()
                ? "；民警见证 " + req.getPoliceWitnessName() : "")
                + (req.getDamageDescription() != null && !req.getDamageDescription().isBlank()
                ? "；损坏情况：" + req.getDamageDescription() : ""),
                UserContext.currentName());
        broadcast(esc.getEventId());
        return fe;
    }

    /** 补录后续修复与安置：锁具更换、门窗修复、物业见证、老人后续安置 */
    @Transactional
    public ForcedEntry updateForcedEntry(Long escalationId, ForcedEntryUpdateRequest req) {
        DoorEscalation esc = mustEsc(escalationId);
        ForcedEntry fe = entryRepo.findByEscalation_Id(escalationId)
                .orElseThrow(() -> new ApiException("尚未登记破门/锁匠处置"));
        if (req.getElderPlacement() != null && !req.getElderPlacement().isBlank()
                && !PLACEMENTS.contains(req.getElderPlacement())) {
            throw new ApiException("老人安置方式不合法: " + req.getElderPlacement());
        }
        fe.setLockReplaced(req.isLockReplaced());
        fe.setLockReplaceNote(req.getLockReplaceNote());
        fe.setDoorWindowRepaired(req.isDoorWindowRepaired());
        fe.setRepairNote(req.getRepairNote());
        if (req.getPropertyWitnessName() != null) {
            fe.setPropertyWitnessName(req.getPropertyWitnessName());
        }
        if (req.getDamageDescription() != null) {
            fe.setDamageDescription(req.getDamageDescription());
        }
        fe.setElderPlacement(req.getElderPlacement());
        fe.setPlacementNote(req.getPlacementNote());
        fe.setUpdatedAt(LocalDateTime.now());
        fe = entryRepo.save(fe);

        addLog(esc.getEvent(), "DOOR", "破门后续处理更新：锁具"
                + (req.isLockReplaced() ? "已更换" : "待更换")
                + (req.getLockReplaceNote() != null && !req.getLockReplaceNote().isBlank() ? "（" + req.getLockReplaceNote() + "）" : "")
                + "，门窗" + (req.isDoorWindowRepaired() ? "已修复" : "待修复")
                + (req.getRepairNote() != null && !req.getRepairNote().isBlank() ? "（" + req.getRepairNote() + "）" : "")
                + (req.getElderPlacement() != null && !req.getElderPlacement().isBlank()
                ? "，老人安置：" + placementLabel(req.getElderPlacement()) : "")
                + (req.getPlacementNote() != null && !req.getPlacementNote().isBlank() ? "（" + req.getPlacementNote() + "）" : ""),
                UserContext.currentName());
        broadcast(esc.getEventId());
        return fe;
    }

    /* ===================== 费用明细 ===================== */

    @Transactional
    public DoorCostItem addCost(Long forcedEntryId, DoorCostRequest req) {
        ForcedEntry fe = entryRepo.findById(forcedEntryId)
                .orElseThrow(() -> new ApiException("破门/锁匠处置记录不存在"));
        validateCost(req);
        DoorCostItem c = new DoorCostItem();
        c.setForcedEntry(fe);
        c.setItemType(req.getItemType());
        c.setDescription(req.getDescription());
        c.setAmount(req.getAmount());
        c.setAdvanceBy(req.getAdvanceBy());
        c.setBearer(req.getBearer());
        c.setNote(req.getNote());
        c = costRepo.save(c);

        addLog(fe.getEscalation().getEvent(), "DOOR", "登记费用：" + costTypeLabel(req.getItemType())
                + " ¥" + req.getAmount()
                + "，垫付方 " + advanceLabel(req.getAdvanceBy())
                + "，承担方 " + bearerLabel(req.getBearer())
                + (req.getDescription() != null && !req.getDescription().isBlank() ? "（" + req.getDescription() + "）" : ""),
                UserContext.currentName());
        broadcast(fe.getEscalation().getEventId());
        return c;
    }

    @Transactional
    public DoorCostItem updateCost(Long costId, DoorCostRequest req) {
        DoorCostItem c = costRepo.findById(costId)
                .orElseThrow(() -> new ApiException("费用记录不存在"));
        validateCost(req);
        c.setItemType(req.getItemType());
        c.setDescription(req.getDescription());
        c.setAmount(req.getAmount());
        c.setAdvanceBy(req.getAdvanceBy());
        c.setBearer(req.getBearer());
        c.setNote(req.getNote());
        c = costRepo.save(c);

        addLog(c.getForcedEntry().getEscalation().getEvent(), "DOOR",
                "更新费用：" + costTypeLabel(req.getItemType()) + " ¥" + req.getAmount()
                        + "，垫付方 " + advanceLabel(req.getAdvanceBy())
                        + "，承担方 " + bearerLabel(req.getBearer()),
                UserContext.currentName());
        broadcast(c.getForcedEntry().getEscalation().getEventId());
        return c;
    }

    /* ===================== 家属到场确认 ===================== */

    @Transactional
    public DoorFamilyReview addFamilyReview(Long escalationId, DoorFamilyReviewRequest req) {
        DoorEscalation esc = mustEsc(escalationId);
        if (!OPINIONS.contains(req.getCostOpinion())) {
            throw new ApiException("费用确认意见不合法: " + req.getCostOpinion());
        }
        DoorFamilyReview r = new DoorFamilyReview();
        r.setEscalation(esc);
        r.setFamilyName(req.getFamilyName());
        r.setRelationship(req.getRelationship());
        r.setViewedTrajectory(req.isViewedTrajectory());
        r.setCostOpinion(req.getCostOpinion());
        r.setComment(req.getComment());
        r.setRecordedByName(UserContext.currentName());
        r = reviewRepo.save(r);

        addLog(esc.getEvent(), "DOOR", "家属到场确认：" + req.getFamilyName()
                + (req.getRelationship() != null && !req.getRelationship().isBlank() ? "（" + req.getRelationship() + "）" : "")
                + (req.isViewedTrajectory() ? "，已查看完整处置轨迹" : "")
                + "，费用意见：" + opinionLabel(req.getCostOpinion())
                + (req.getComment() != null && !req.getComment().isBlank() ? "，说明：" + req.getComment() : ""),
                UserContext.currentName());
        broadcast(esc.getEventId());
        return r;
    }

    /* ===================== 查询 ===================== */

    /** 事件详情聚合：最新一次多方确认处置（含联系步骤、破门记录、费用、家属确认） */
    @Transactional(readOnly = true)
    public Map<String, Object> detailFor(Long eventId) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<DoorEscalation> all = escRepo.findByEvent_IdOrderByStartedAtDesc(eventId);
        m.put("doorEscalations", all);
        if (!all.isEmpty()) {
            DoorEscalation esc = all.get(0);
            m.put("doorEscalation", esc);
            m.put("doorSteps", stepRepo.findByEscalation_IdOrderByContactedAtAsc(esc.getId()));
            ForcedEntry fe = entryRepo.findByEscalation_Id(esc.getId()).orElse(null);
            m.put("forcedEntry", fe);
            m.put("doorCosts", fe == null ? List.of() : costRepo.findByForcedEntry_IdOrderByCreatedAtAsc(fe.getId()));
            m.put("doorReviews", reviewRepo.findByEscalation_IdOrderByCreatedAtAsc(esc.getId()));
        }
        return m;
    }

    /* ===================== 内部工具 ===================== */

    private void validateCost(DoorCostRequest req) {
        if (!COST_TYPES.contains(req.getItemType())) {
            throw new ApiException("费用类型不合法: " + req.getItemType());
        }
        if (!ADVANCE_BY.contains(req.getAdvanceBy())) {
            throw new ApiException("垫付方不合法: " + req.getAdvanceBy());
        }
        if (!BEARERS.contains(req.getBearer())) {
            throw new ApiException("承担方不合法: " + req.getBearer());
        }
        if (req.getAmount() == null || req.getAmount().signum() < 0) {
            throw new ApiException("费用金额不合法");
        }
    }

    private DoorEscalation mustEsc(Long id) {
        return escRepo.findById(id).orElseThrow(() -> new ApiException(404, "多方确认处置单不存在: " + id));
    }

    private HelpEvent mustEvent(Long id) {
        return eventRepo.findById(id).orElseThrow(() -> new ApiException(404, "事件不存在: " + id));
    }

    private void mustBeContacting(DoorEscalation esc) {
        if (!"CONTACTING".equals(esc.getStatus())) {
            throw new ApiException("该多方确认处置已结案，不能继续登记");
        }
    }

    private Set<String> flagsOf(HelpEvent e) {
        Set<String> s = new LinkedHashSet<>();
        if (e.getRiskFlags() != null && !e.getRiskFlags().isBlank()) {
            s.addAll(Arrays.asList(e.getRiskFlags().split(",")));
        }
        return s;
    }

    private void addLog(HelpEvent e, String type, String content, String operator) {
        EventLog l = new EventLog();
        l.setEvent(e);
        l.setType(type);
        l.setContent(content);
        l.setOperatorName(operator);
        logRepo.save(l);
    }

    private void broadcast(Long eventId) {
        Map<String, Object> p = new HashMap<>();
        p.put("eventId", eventId);
        p.put("at", LocalDateTime.now().toString());
        sse.broadcast("event-update", p);
    }

    public static String partyLabel(String s) {
        return switch (s) {
            case "FAMILY" -> "家属";
            case "PROPERTY" -> "物业";
            case "NEIGHBOR" -> "邻居";
            case "POLICE" -> "派出所";
            default -> s;
        };
    }

    public static String resolutionLabel(String s) {
        return switch (s) {
            case "FAMILY_OPENED" -> "家属到场开门";
            case "PROPERTY_OPENED" -> "物业协助开门";
            case "NEIGHBOR_OPENED" -> "邻居协助开门";
            case "LOCKSMITH" -> "锁匠开锁";
            case "FORCED_ENTRY" -> "破门救助";
            default -> s;
        };
    }

    public static String methodLabel(String s) {
        return switch (s) {
            case "LOCKSMITH" -> "请锁匠开锁";
            case "FORCED_ENTRY" -> "破门救助";
            default -> s;
        };
    }

    public static String authLabel(String s) {
        return switch (s) {
            case "FAMILY_PHONE" -> "家属电话授权";
            case "FAMILY_ONSITE" -> "家属现场授权";
            case "POLICE_ONSITE" -> "民警现场授权";
            case "COMMUNITY_EMERGENCY" -> "社区应急预案授权";
            default -> s;
        };
    }

    public static String costTypeLabel(String s) {
        return switch (s) {
            case "LOCKSMITH_FEE" -> "开锁费";
            case "LOCK_REPLACE" -> "锁具更换";
            case "DOOR_WINDOW_REPAIR" -> "门窗修复";
            case "OTHER" -> "其他费用";
            default -> s;
        };
    }

    public static String advanceLabel(String s) {
        return switch (s) {
            case "COMMUNITY" -> "社区垫付";
            case "FAMILY" -> "家属垫付";
            case "PROPERTY" -> "物业垫付";
            case "NONE" -> "未垫付";
            default -> s;
        };
    }

    public static String bearerLabel(String s) {
        return switch (s) {
            case "PENDING" -> "待确认";
            case "FAMILY" -> "家庭承担";
            case "COMMUNITY_FUND" -> "社区救助";
            case "INSURANCE" -> "保险理赔";
            default -> s;
        };
    }

    public static String placementLabel(String s) {
        return switch (s) {
            case "HOME" -> "家中留观";
            case "HOSPITAL" -> "送医观察";
            case "FAMILY_CARE" -> "家属接回照顾";
            case "TEMP_CARE" -> "社区临时照护点";
            default -> s;
        };
    }

    public static String opinionLabel(String s) {
        return switch (s) {
            case "AGREE" -> "认可费用分担";
            case "PARTIAL" -> "部分认可";
            case "DISAGREE" -> "有异议";
            default -> s;
        };
    }
}
