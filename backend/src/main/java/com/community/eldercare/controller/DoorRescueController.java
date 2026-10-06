package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.domain.DoorBreakRescue;
import com.community.eldercare.domain.DoorContactAttempt;
import com.community.eldercare.domain.RescueExpense;
import com.community.eldercare.dto.DoorBreakRescueRequest;
import com.community.eldercare.dto.DoorContactRequest;
import com.community.eldercare.dto.ExpenseRequest;
import com.community.eldercare.dto.FamilyConfirmRequest;
import com.community.eldercare.dto.RescueUpdateRequest;
import com.community.eldercare.service.DoorRescueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 无法开门场景：多方联系确认 → 破门/锁匠救助（授权依据+见证人）→
 * 修复/换锁/费用/安置补录 → 家属到场确认处置轨迹与费用承担。
 */
@RestController
@RequestMapping("/api/events/{eventId}")
public class DoorRescueController {

    private final DoorRescueService service;

    public DoorRescueController(DoorRescueService service) {
        this.service = service;
    }

    /** 多方联系确认（家属/物业/邻居/派出所，按序引导，可重复提交逐步留痕） */
    @PostMapping("/door-contacts")
    public R<DoorContactAttempt> addContact(@PathVariable Long eventId,
                                            @Valid @RequestBody DoorContactRequest req) {
        return R.ok(service.addContact(eventId, req));
    }

    /** 登记破门/锁匠救助（必须携带授权依据与至少一名现场见证人） */
    @PostMapping("/door-rescue")
    public R<DoorBreakRescue> registerRescue(@PathVariable Long eventId,
                                             @Valid @RequestBody DoorBreakRescueRequest req) {
        return R.ok(service.registerRescue(eventId, req));
    }

    /** 救助后续补录：锁具更换、门窗修复、物业见证、费用垫付、老人安置 */
    @PutMapping("/door-rescue")
    public R<DoorBreakRescue> updateRescue(@PathVariable Long eventId,
                                           @RequestBody RescueUpdateRequest req) {
        return R.ok(service.updateRescue(eventId, req));
    }

    /** 追加费用项（开锁/换锁/门窗修复/临时陪护等） */
    @PostMapping("/door-rescue/expenses")
    public R<RescueExpense> addExpense(@PathVariable Long eventId,
                                       @Valid @RequestBody ExpenseRequest req) {
        return R.ok(service.addExpense(eventId, req));
    }

    /** 家属到场查看完整处置轨迹，确认费用承担（家庭/社区救助/保险理赔） */
    @PostMapping("/door-rescue/family-confirm")
    public R<DoorBreakRescue> familyConfirm(@PathVariable Long eventId,
                                            @Valid @RequestBody FamilyConfirmRequest req) {
        return R.ok(service.familyConfirm(eventId, req));
    }
}
