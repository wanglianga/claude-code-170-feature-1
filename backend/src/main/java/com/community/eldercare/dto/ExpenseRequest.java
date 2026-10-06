package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * 破门救助费用项登记/补充。
 */
public class ExpenseRequest {

    @NotBlank
    private String itemType;
    private String itemDetail;
    private BigDecimal amount;
    private String advancedBy;
    /** FAMILY / COMMUNITY_AID / INSURANCE / PENDING */
    private String bearer = "PENDING";
    private String claimNo;
    private String receiptUrls;

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemDetail() { return itemDetail; }
    public void setItemDetail(String itemDetail) { this.itemDetail = itemDetail; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getAdvancedBy() { return advancedBy; }
    public void setAdvancedBy(String advancedBy) { this.advancedBy = advancedBy; }
    public String getBearer() { return bearer; }
    public void setBearer(String bearer) { this.bearer = bearer; }
    public String getClaimNo() { return claimNo; }
    public void setClaimNo(String claimNo) { this.claimNo = claimNo; }
    public String getReceiptUrls() { return receiptUrls; }
    public void setReceiptUrls(String receiptUrls) { this.receiptUrls = receiptUrls; }
}
