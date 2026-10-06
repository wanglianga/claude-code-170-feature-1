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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 无法开门场景的多方确认与破门救助服务。
 *
 * 处置链：社工到场老人无响应（屋内有灯光等异常）→ 平台引导依次联系
 * 家属 → 物业 → 邻居 → 派出所（每步留痕联系人/时间/反馈）→
 * 必要时破门/请锁匠（强制留存授权依据与现场见证人）→
 * 登记锁具更换、门窗修复、费用垫付、老人安置 →
 * 家属到场查看完整轨迹并逐项确认费用承担（家庭/社区救助/保险理赔）。
 */
@Service
public class DoorRescueService {

    /** 平台引导的多方联系顺序 */
    public static final List<String> CONTACT_ORDER = List.of("FAMILY", "PROPERTY", "NEIGHBOR", "POLICE");

    private final HelpEventRepository eventRepo;
    private final EventLogRepository logRepo;
    private final DoorContactAttemptRepository contactRepo;
    private final DoorBreakRescueRepository rescueRepo;
    private final SseService sse;

    public DoorRescueService(HelpEventRepository eventRepo, EventLogRepository logRepo,
                             DoorContactAttemptRepository contactRepo,
                             DoorBreakRescueRepository rescueRepo,
                             SseService sse) {
        this.eventRepo = eventRepo;
        this.logRepo = logRepo;
        this.contactRepo = contactRepo;
        this.rescueRepo = rescueRepo;
        this.sse = sse;
    }

    /* ===================== 多方联系确认 ===================== */

    @Transactional
    public DoorContactAttempt addContact(Long eventId, DoorContactRequest req) {
        HelpEvent e = mustGet(eventId);
        long done = contactRepo.countByEventId(eventId);

        DoorContactAttempt c = new DoorContactAttempt();
        c.setEvent(e);
        c.setSeq((int) done + 1);
        // 未指定联系方时，平台按引导顺序自动给出下一方
        c.setPartyType(req.getPartyType() != null && !req.getPartyType().isBlank()
                ? req.getPartyType()
                : CONTACT_ORDER.get(Math.min((int) done, CONTACT_ORDER.size() - 1)));
        c.setPartyName(req.getPartyName());
        c.setPartyPhone(req.getPartyPhone());
        c.setChannel(req.getChannel() == null || req.getChannel().isBlank() ? "电话" : req.getChannel());
        c.setContactedAt(req.getContactedAt() != null ? req.getContactedAt() : LocalDateTime.now());
        c.setReached(req.isReached());
        c.setFeedback(req.getFeedback());
        c.setWillArrive(req.getWillArrive());
        c.setEtaMinutes(req.getEtaMinutes());
        c.setNote(req.getNote());
        c.setOperatorName(UserContext.currentName());
        c = contactRepo.save(c);

        ensureFlag(e, "CANNOT_OPEN_DOOR");

        addLog(e, "DOOR_CONTACT", "多方确认第" + c.getSeq() + "步·联系" + partyLabel(c.getPartyType())
                + "：" + req.getPartyName()
                + (req.getPartyPhone() != null ? " " + req.getPartyPhone() : "")
                + "（" + c.getChannel() + "）"
                + (req.isReached() ? "，已联系上" : "，未联系上")
                + (req.getFeedback() != null && !req.getFeedback().isBlank() ? "，反馈：" + req.getFeedback() : "")
                + (Boolean.TRUE.equals(req.getWillArrive())
                        ? "，承诺" + (req.getEtaMinutes() != null ? req.getEtaMinutes() + "分钟内" : "") + "到场" : ""),
                UserContext.currentName());

        // 走完引导顺序仍无法入户时，系统提示登记破门/锁匠授权
        if (c.getSeq() >= CONTACT_ORDER.size()) {
            addLog(e, "SYSTEM", "已完成家属、物业、邻居、派出所多方联系确认。"
                    + "若仍无法入户且老人可能存在生命危险，请登记破门/锁匠救助（须填写授权依据与现场见证人）", "系统");
        }
        broadcast("event-update", eventId);
        return c;
    }

    /* ===================== 破门/锁匠救助登记 ===================== */

    @Transactional
    public DoorBreakRescue registerRescue(Long eventId, DoorBreakRescueRequest req) {
        HelpEvent e = mustGet(eventId);
        if (rescueRepo.existsByEventId(eventId)) {
            throw new ApiException("该事件已登记破门/锁匠救助，请在原记录上补充");
        }
        if (req.getWitnesses() == null || req.getWitnesses().isEmpty()) {
            throw new ApiException("破门/锁匠救助必须登记至少一名现场见证人");
        }
        if (req.getAuthorizationEvidence() == null || req.getAuthorizationEvidence().isBlank()) {
            throw new ApiException("破门/锁匠救助必须保存授权依据（录音/截图/执法记录号/会议纪要）");
        }

        DoorBreakRescue r = new DoorBreakRescue();
        r.setEvent(e);
        r.setActionType(req.getActionType());
        r.setProviderName(req.getProviderName());
        r.setProviderPhone(req.getProviderPhone());
        r.setRescuedAt(req.getRescuedAt() != null ? req.getRescuedAt() : LocalDateTime.now());
        r.setOperatorName(UserContext.currentName());

        r.setAuthorizationType(req.getAuthorizationType());
        r.setAuthorizerName(req.getAuthorizerName());
        r.setAuthorizerPhone(req.getAuthorizerPhone());
        r.setAuthorizedAt(req.getAuthorizedAt() != null ? req.getAuthorizedAt() : LocalDateTime.now());
        r.setAuthorizationEvidence(req.getAuthorizationEvidence());
        r.setAuthorizationNote(req.getAuthorizationNote());

        r.setSceneDescription(req.getSceneDescription());
        r.setScenePhotoUrls(req.getScenePhotoUrls());
        r.setLockReplaced(req.isLockReplaced());
        r.setLockModel(req.getLockModel());
        r.setNewKeyKeptBy(req.getNewKeyKeptBy());
        r.setRepairArrangement(req.getRepairArrangement());
        r.setRepairedAt(req.getRepairedAt());
        r.setAdvancedBy(req.getAdvancedBy());
        r.setAdvancedAmount(req.getAdvancedAmount());
        r.setElderPlacement(req.getElderPlacement());
        r.setPlacementNote(req.getPlacementNote());
        r.setPropertyWitnessed(req.isPropertyWitnessed());
        r.setPropertyWitnessName(req.getPropertyWitnessName());

        for (DoorBreakRescueRequest.WitnessDto w : req.getWitnesses()) {
            DoorBreakWitness witness = new DoorBreakWitness();
            witness.setRescue(r);
            witness.setName(w.getName());
            witness.setRole(w.getRole());
            witness.setPhone(w.getPhone());
            witness.setEvidenceUrl(w.getEvidenceUrl());
            r.getWitnesses().add(witness);
        }

        if (req.getExpenses() != null) {
            for (DoorBreakRescueRequest.ExpenseDto x : req.getExpenses()) {
                RescueExpense exp = new RescueExpense();
                exp.setRescue(r);
                exp.setItemType(x.getItemType());
                exp.setItemDetail(x.getItemDetail());
                exp.setAmount(x.getAmount());
                exp.setAdvancedBy(x.getAdvancedBy() != null ? x.getAdvancedBy() : req.getAdvancedBy());
                exp.setBearer(x.getBearer() != null && !x.getBearer().isBlank() ? x.getBearer() : "PENDING");
                exp.setClaimNo(x.getClaimNo());
                exp.setReceiptUrls(x.getReceiptUrls());
                r.getExpenses().add(exp);
            }
        }
        r = rescueRepo.save(r);

        ensureFlag(e, "CANNOT_OPEN_DOOR");
        // 破门或请锁匠均属强制入户救助，统一标记
        ensureFlag(e, "DOOR_BREAK_RESCUE");

        StringBuilder witnessNames = new StringBuilder();
        r.getWitnesses().forEach(w -> witnessNames.append(w.getRole()).append(" ").append(w.getName()).append("、"));

        addLog(e, "DOOR_RESCUE", actionLabel(req.getActionType()) + "救助已实施，施工/锁匠方 "
                + nz(req.getProviderName(), "未登记")
                + "；授权依据：" + authLabel(req.getAuthorizationType())
                + "（授权人 " + nz(req.getAuthorizerName(), "—") + "）"
                + "；现场见证人 " + witnessNames
                + (req.isLockReplaced() ? "；已更换锁具（" + nz(req.getLockModel(), "型号未登记") + "），新钥匙由"
                        + nz(req.getNewKeyKeptBy(), "未登记") + "保管" : "")
                + (req.getRepairArrangement() != null ? "；门窗修复：" + req.getRepairArrangement() : "")
                + (req.getAdvancedBy() != null ? "；费用由" + req.getAdvancedBy() + "垫付"
                        + (req.getAdvancedAmount() != null ? req.getAdvancedAmount() + "元" : "") : "")
                + (req.isPropertyWitnessed() ? "；物业已见证（" + nz(req.getPropertyWitnessName(), "—") + "）" : "")
                + (req.getElderPlacement() != null ? "；老人安置：" + req.getElderPlacement() : ""),
                UserContext.currentName());
        broadcast("event-update", eventId);
        return r;
    }

    /* ===================== 救助后续补录（修复/换锁/费用/安置） ===================== */

    @Transactional
    public DoorBreakRescue updateRescue(Long eventId, RescueUpdateRequest req) {
        DoorBreakRescue r = mustRescue(eventId);
        StringBuilder changes = new StringBuilder();

        if (req.getSceneDescription() != null) { r.setSceneDescription(req.getSceneDescription()); }
        if (req.getScenePhotoUrls() != null) { r.setScenePhotoUrls(req.getScenePhotoUrls()); }
        if (req.getLockReplaced() != null) {
            r.setLockReplaced(req.getLockReplaced());
            changes.append("锁具更换：").append(Boolean.TRUE.equals(req.getLockReplaced()) ? "已更换" : "未更换").append("；");
        }
        if (req.getLockModel() != null) { r.setLockModel(req.getLockModel()); }
        if (req.getNewKeyKeptBy() != null) { r.setNewKeyKeptBy(req.getNewKeyKeptBy()); }
        if (req.getRepairArrangement() != null) {
            r.setRepairArrangement(req.getRepairArrangement());
            changes.append("门窗修复安排：").append(req.getRepairArrangement()).append("；");
        }
        if (req.getRepairedAt() != null) {
            r.setRepairedAt(req.getRepairedAt());
            changes.append("修复完成时间 ").append(req.getRepairedAt()).append("；");
        }
        if (req.getAdvancedBy() != null) {
            r.setAdvancedBy(req.getAdvancedBy());
            changes.append("费用垫付方：").append(req.getAdvancedBy()).append("；");
        }
        if (req.getAdvancedAmount() != null) {
            r.setAdvancedAmount(req.getAdvancedAmount());
            changes.append("垫付金额 ").append(req.getAdvancedAmount()).append("元；");
        }
        if (req.getElderPlacement() != null) {
            r.setElderPlacement(req.getElderPlacement());
            changes.append("老人安置：").append(req.getElderPlacement()).append("；");
        }
        if (req.getPlacementNote() != null) { r.setPlacementNote(req.getPlacementNote()); }
        if (req.getPropertyWitnessed() != null) {
            r.setPropertyWitnessed(req.getPropertyWitnessed());
            changes.append(Boolean.TRUE.equals(req.getPropertyWitnessed()) ? "物业已见证；" : "物业未见证；");
        }
        if (req.getPropertyWitnessName() != null) { r.setPropertyWitnessName(req.getPropertyWitnessName()); }

        rescueRepo.save(r);
        addLog(r.getEvent(), "DOOR_RESCUE", "破门救助信息补录："
                + (changes.length() == 0 ? "更新现场说明/凭证" : changes), UserContext.currentName());
        broadcast("event-update", eventId);
        return r;
    }

    @Transactional
    public RescueExpense addExpense(Long eventId, ExpenseRequest req) {
        DoorBreakRescue r = mustRescue(eventId);
        RescueExpense x = new RescueExpense();
        x.setRescue(r);
        x.setItemType(req.getItemType());
        x.setItemDetail(req.getItemDetail());
        x.setAmount(req.getAmount());
        x.setAdvancedBy(req.getAdvancedBy() != null ? req.getAdvancedBy() : r.getAdvancedBy());
        x.setBearer(req.getBearer() != null && !req.getBearer().isBlank() ? req.getBearer() : "PENDING");
        x.setClaimNo(req.getClaimNo());
        x.setReceiptUrls(req.getReceiptUrls());
        r.getExpenses().add(x);
        rescueRepo.save(r);

        addLog(r.getEvent(), "DOOR_RESCUE", "登记救助费用：" + expenseLabel(req.getItemType())
                + (req.getItemDetail() != null ? "（" + req.getItemDetail() + "）" : "")
                + (req.getAmount() != null ? " " + req.getAmount() + "元" : "")
                + "，垫付方 " + nz(x.getAdvancedBy(), "未登记")
                + "，承担方 " + bearerLabel(x.getBearer())
                + (req.getClaimNo() != null && !req.getClaimNo().isBlank() ? "，理赔/救助单号 " + req.getClaimNo() : ""),
                UserContext.currentName());
        broadcast("event-update", eventId);
        return x;
    }

    /* ===================== 家属到场确认完整轨迹与费用承担 ===================== */

    @Transactional
    public DoorBreakRescue familyConfirm(Long eventId, FamilyConfirmRequest req) {
        DoorBreakRescue r = mustRescue(eventId);
        Set<String> allowed = Set.of("FAMILY", "COMMUNITY_AID", "INSURANCE", "PENDING");
        Map<Long, String> bearers = req.getExpenseBearers();
        if (bearers != null) {
            for (RescueExpense x : r.getExpenses()) {
                String b = bearers.get(x.getId());
                if (b != null && allowed.contains(b)) {
                    x.setBearer(b);
                    x.setFamilyConfirmed(true);
                }
            }
        }
        r.setFamilyConfirmed(true);
        r.setFamilyConfirmerName(req.getConfirmerName());
        r.setFamilyConfirmedAt(LocalDateTime.now());
        r.setFamilyConfirmNote(req.getConfirmNote());
        r.setFamilyConfirmEvidence(req.getEvidenceUrl());
        rescueRepo.save(r);

        long familyCount = r.getExpenses().stream().filter(x -> "FAMILY".equals(x.getBearer())).count();
        long aidCount = r.getExpenses().stream().filter(x -> "COMMUNITY_AID".equals(x.getBearer())).count();
        long insCount = r.getExpenses().stream().filter(x -> "INSURANCE".equals(x.getBearer())).count();
        long pending = r.getExpenses().stream().filter(x -> "PENDING".equals(x.getBearer())).count();

        addLog(r.getEvent(), "DOOR_CONFIRM", "家属 " + req.getConfirmerName() + " 到场查看完整处置轨迹并确认："
                + nz(req.getConfirmNote(), "认可处置")
                + "；费用承担——家庭承担 " + familyCount + " 笔、社区救助 " + aidCount
                + " 笔、保险理赔 " + insCount + " 笔、待定 " + pending + " 笔",
                UserContext.currentName());
        broadcast("event-update", eventId);
        return r;
    }

    /* ===================== 公共方法 ===================== */

    private HelpEvent mustGet(Long id) {
        return eventRepo.findById(id).orElseThrow(() -> new ApiException(404, "事件不存在: " + id));
    }

    private DoorBreakRescue mustRescue(Long eventId) {
        return rescueRepo.findByEventId(eventId)
                .orElseThrow(() -> new ApiException("该事件尚未登记破门/锁匠救助，请先登记救助记录"));
    }

    private void ensureFlag(HelpEvent e, String flag) {
        Set<String> flags = new java.util.LinkedHashSet<>();
        if (e.getRiskFlags() != null && !e.getRiskFlags().isBlank()) {
            flags.addAll(java.util.Arrays.asList(e.getRiskFlags().split(",")));
        }
        if (flags.add(flag)) {
            e.setRiskFlags(String.join(",", flags));
            eventRepo.save(e);
        }
    }

    private void addLog(HelpEvent e, String type, String content, String operator) {
        EventLog l = new EventLog();
        l.setEvent(e);
        l.setType(type);
        l.setContent(content);
        l.setOperatorName(operator);
        logRepo.save(l);
    }

    private void broadcast(String name, Long eventId) {
        sse.broadcast(name, Map.of("eventId", eventId, "at", LocalDateTime.now().toString()));
    }

    private String nz(String s, String def) {
        return s == null || s.isBlank() ? def : s;
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

    public static String actionLabel(String s) {
        return switch (s) {
            case "BREAK_DOOR" -> "破门";
            case "LOCKSMITH" -> "请锁匠开锁";
            case "BOTH" -> "破门并换锁";
            default -> s;
        };
    }

    public static String authLabel(String s) {
        return switch (s) {
            case "FAMILY_AUTHORIZED" -> "家属授权";
            case "POLICE_ORDER" -> "民警现场决定";
            case "COMMUNITY_EMERGENCY" -> "社区紧急避险决定";
            case "OTHER" -> "其他依据";
            default -> s;
        };
    }

    public static String expenseLabel(String s) {
        return switch (s) {
            case "LOCK_OPEN" -> "开锁费";
            case "LOCK_REPLACE" -> "锁具更换费";
            case "DOOR_REPAIR" -> "门体修复费";
            case "WINDOW_REPAIR" -> "窗户修复费";
            case "TEMP_CARE" -> "临时陪护费";
            case "OTHER" -> "其他费用";
            default -> s;
        };
    }

    public static String bearerLabel(String s) {
        return switch (s) {
            case "FAMILY" -> "家庭承担";
            case "COMMUNITY_AID" -> "社区救助";
            case "INSURANCE" -> "保险理赔";
            case "PENDING" -> "待家属确认";
            default -> s;
        };
    }
}
