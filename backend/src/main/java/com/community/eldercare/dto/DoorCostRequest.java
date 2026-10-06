package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 费用明细登记/更新请求：垫付方与承担方（家庭承担/社区救助/保险理赔）。
 */
public class DoorCostRequest {

    /** LOCKSMITH_FEE 开锁费 / LOCK_REPLACE 锁具更换 / DOOR_WINDOW_REPAIR 门窗修复 / OTHER 其他费用 */
    @NotBlank
    private String itemType;
    private String description;
    @NotNull
    private BigDecimal amount;
    /** COMMUNITY 社区垫付 / FAMILY 家属垫付 / PROPERTY 物业垫付 / NONE 未垫付 */
    private String advanceBy = "COMMUNITY";
    /** PENDING 待确认 / FAMILY 家庭承担 / COMMUNITY_FUND 社区救助 / INSURANCE 保险理赔 */
    private String bearer = "PENDING";
    private String note;

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getAdvanceBy() { return advanceBy; }
    public void setAdvanceBy(String advanceBy) { this.advanceBy = advanceBy; }
    public String getBearer() { return bearer; }
    public void setBearer(String bearer) { this.bearer = bearer; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
