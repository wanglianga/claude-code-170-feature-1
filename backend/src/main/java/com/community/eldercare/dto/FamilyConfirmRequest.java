package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * 家属到场确认：查看完整处置轨迹后，确认整体处置无异议，
 * 并逐项确认费用承担方（FAMILY 家庭 / COMMUNITY_AID 社区救助 / INSURANCE 保险理赔）。
 */
public class FamilyConfirmRequest {

    @NotBlank
    private String confirmerName;
    private String confirmerPhone;
    /** 认可 / 异议说明 */
    private String confirmNote;
    /** 家属签字/合影凭证地址 */
    private String evidenceUrl;

    /** 费用逐项承担确认：expenseId → FAMILY / COMMUNITY_AID / INSURANCE */
    private Map<Long, String> expenseBearers;

    public String getConfirmerName() { return confirmerName; }
    public void setConfirmerName(String confirmerName) { this.confirmerName = confirmerName; }
    public String getConfirmerPhone() { return confirmerPhone; }
    public void setConfirmerPhone(String confirmerPhone) { this.confirmerPhone = confirmerPhone; }
    public String getConfirmNote() { return confirmNote; }
    public void setConfirmNote(String confirmNote) { this.confirmNote = confirmNote; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public Map<Long, String> getExpenseBearers() { return expenseBearers; }
    public void setExpenseBearers(Map<Long, String> expenseBearers) { this.expenseBearers = expenseBearers; }
}
