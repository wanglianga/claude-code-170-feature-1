package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.domain.*;
import com.community.eldercare.dto.*;
import com.community.eldercare.service.DoorEscalationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 无法开门多方确认处置：
 * 依次联系家属/物业/邻居/派出所 → 破门或锁匠（授权依据+见证人）→
 * 锁具更换/门窗修复/费用垫付/老人安置 → 家属到场确认费用承担。
 */
@RestController
@RequestMapping("/api")
public class DoorEscalationController {

    private final DoorEscalationService doorService;

    public DoorEscalationController(DoorEscalationService doorService) {
        this.doorService = doorService;
    }

    /** 启动无法开门多方确认（老人无响应、屋内有灯光时） */
    @PostMapping("/events/{id}/door-escalation")
    public R<DoorEscalation> start(@PathVariable Long id, @RequestBody DoorEscalationRequest req) {
        return R.ok(doorService.start(id, req));
    }

    /** 事件的多方确认聚合（处置单+联系步骤+破门记录+费用+家属确认） */
    @GetMapping("/events/{id}/door-escalation")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(doorService.detailFor(id));
    }

    /** 登记联系步骤（依次：家属 → 物业 → 邻居 → 派出所） */
    @PostMapping("/door-escalations/{id}/contacts")
    public R<DoorContactStep> addContact(@PathVariable Long id, @Valid @RequestBody DoorContactRequest req) {
        return R.ok(doorService.addContact(id, req));
    }

    /** 结案：家属/物业/邻居开门，无需破门 */
    @PostMapping("/door-escalations/{id}/resolve")
    public R<DoorEscalation> resolve(@PathVariable Long id, @Valid @RequestBody DoorResolveRequest req) {
        return R.ok(doorService.resolve(id, req));
    }

    /** 登记破门/锁匠处置（授权依据、现场见证人必填） */
    @PostMapping("/door-escalations/{id}/forced-entry")
    public R<ForcedEntry> createForcedEntry(@PathVariable Long id, @Valid @RequestBody ForcedEntryRequest req) {
        return R.ok(doorService.createForcedEntry(id, req));
    }

    /** 补录后续修复与安置：锁具更换、门窗修复、物业见证、老人安置 */
    @PutMapping("/door-escalations/{id}/forced-entry")
    public R<ForcedEntry> updateForcedEntry(@PathVariable Long id, @RequestBody ForcedEntryUpdateRequest req) {
        return R.ok(doorService.updateForcedEntry(id, req));
    }

    /** 登记费用明细（垫付方、承担方：家庭承担/社区救助/保险理赔） */
    @PostMapping("/forced-entries/{id}/costs")
    public R<DoorCostItem> addCost(@PathVariable Long id, @Valid @RequestBody DoorCostRequest req) {
        return R.ok(doorService.addCost(id, req));
    }

    /** 更新费用明细（确认承担方等） */
    @PutMapping("/door-costs/{id}")
    public R<DoorCostItem> updateCost(@PathVariable Long id, @Valid @RequestBody DoorCostRequest req) {
        return R.ok(doorService.updateCost(id, req));
    }

    /** 家属到场确认：查看完整处置轨迹，确认费用承担方案 */
    @PostMapping("/door-escalations/{id}/family-review")
    public R<DoorFamilyReview> familyReview(@PathVariable Long id, @Valid @RequestBody DoorFamilyReviewRequest req) {
        return R.ok(doorService.addFamilyReview(id, req));
    }
}
