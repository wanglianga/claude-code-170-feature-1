package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 破门/锁匠救助登记请求。
 * 强制携带授权依据（authorizationEvidence）与至少一名现场见证人，
 * 同时登记锁具更换、门窗修复、费用垫付与老人后续安置。
 */
public class DoorBreakRescueRequest {

    /** BREAK_DOOR / LOCKSMITH / BOTH */
    @NotBlank
    private String actionType;
    private String providerName;
    private String providerPhone;
    private LocalDateTime rescuedAt;

    /** FAMILY_AUTHORIZED / POLICE_ORDER / COMMUNITY_EMERGENCY / OTHER */
    @NotBlank
    private String authorizationType;
    private String authorizerName;
    private String authorizerPhone;
    private LocalDateTime authorizedAt;
    /** 授权凭证（录音/截图/执法记录号/会议纪要），必填 */
    @NotBlank
    private String authorizationEvidence;
    private String authorizationNote;

    private String sceneDescription;
    private String scenePhotoUrls;

    private boolean lockReplaced;
    private String lockModel;
    private String newKeyKeptBy;

    private String repairArrangement;
    private LocalDateTime repairedAt;

    private String advancedBy;
    private BigDecimal advancedAmount;

    private String elderPlacement;
    private String placementNote;

    private boolean propertyWitnessed;
    private String propertyWitnessName;

    /** 现场见证人（至少 1 人） */
    private List<WitnessDto> witnesses;

    /** 费用项（开锁、换锁、门窗修复等，可先登记后补） */
    private List<ExpenseDto> expenses;

    public static class WitnessDto {
        @NotBlank
        private String name;
        @NotBlank
        private String role;
        private String phone;
        private String evidenceUrl;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEvidenceUrl() { return evidenceUrl; }
        public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    }

    public static class ExpenseDto {
        @NotBlank
        private String itemType;
        private String itemDetail;
        private BigDecimal amount;
        private String advancedBy;
        /** FAMILY / COMMUNITY_AID / INSURANCE / PENDING，默认 PENDING */
        private String bearer;
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

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public LocalDateTime getRescuedAt() { return rescuedAt; }
    public void setRescuedAt(LocalDateTime rescuedAt) { this.rescuedAt = rescuedAt; }
    public String getAuthorizationType() { return authorizationType; }
    public void setAuthorizationType(String authorizationType) { this.authorizationType = authorizationType; }
    public String getAuthorizerName() { return authorizerName; }
    public void setAuthorizerName(String authorizerName) { this.authorizerName = authorizerName; }
    public String getAuthorizerPhone() { return authorizerPhone; }
    public void setAuthorizerPhone(String authorizerPhone) { this.authorizerPhone = authorizerPhone; }
    public LocalDateTime getAuthorizedAt() { return authorizedAt; }
    public void setAuthorizedAt(LocalDateTime authorizedAt) { this.authorizedAt = authorizedAt; }
    public String getAuthorizationEvidence() { return authorizationEvidence; }
    public void setAuthorizationEvidence(String authorizationEvidence) { this.authorizationEvidence = authorizationEvidence; }
    public String getAuthorizationNote() { return authorizationNote; }
    public void setAuthorizationNote(String authorizationNote) { this.authorizationNote = authorizationNote; }
    public String getSceneDescription() { return sceneDescription; }
    public void setSceneDescription(String sceneDescription) { this.sceneDescription = sceneDescription; }
    public String getScenePhotoUrls() { return scenePhotoUrls; }
    public void setScenePhotoUrls(String scenePhotoUrls) { this.scenePhotoUrls = scenePhotoUrls; }
    public boolean isLockReplaced() { return lockReplaced; }
    public void setLockReplaced(boolean lockReplaced) { this.lockReplaced = lockReplaced; }
    public String getLockModel() { return lockModel; }
    public void setLockModel(String lockModel) { this.lockModel = lockModel; }
    public String getNewKeyKeptBy() { return newKeyKeptBy; }
    public void setNewKeyKeptBy(String newKeyKeptBy) { this.newKeyKeptBy = newKeyKeptBy; }
    public String getRepairArrangement() { return repairArrangement; }
    public void setRepairArrangement(String repairArrangement) { this.repairArrangement = repairArrangement; }
    public LocalDateTime getRepairedAt() { return repairedAt; }
    public void setRepairedAt(LocalDateTime repairedAt) { this.repairedAt = repairedAt; }
    public String getAdvancedBy() { return advancedBy; }
    public void setAdvancedBy(String advancedBy) { this.advancedBy = advancedBy; }
    public BigDecimal getAdvancedAmount() { return advancedAmount; }
    public void setAdvancedAmount(BigDecimal advancedAmount) { this.advancedAmount = advancedAmount; }
    public String getElderPlacement() { return elderPlacement; }
    public void setElderPlacement(String elderPlacement) { this.elderPlacement = elderPlacement; }
    public String getPlacementNote() { return placementNote; }
    public void setPlacementNote(String placementNote) { this.placementNote = placementNote; }
    public boolean isPropertyWitnessed() { return propertyWitnessed; }
    public void setPropertyWitnessed(boolean propertyWitnessed) { this.propertyWitnessed = propertyWitnessed; }
    public String getPropertyWitnessName() { return propertyWitnessName; }
    public void setPropertyWitnessName(String propertyWitnessName) { this.propertyWitnessName = propertyWitnessName; }
    public List<WitnessDto> getWitnesses() { return witnesses; }
    public void setWitnesses(List<WitnessDto> witnesses) { this.witnesses = witnesses; }
    public List<ExpenseDto> getExpenses() { return expenses; }
    public void setExpenses(List<ExpenseDto> expenses) { this.expenses = expenses; }
}
