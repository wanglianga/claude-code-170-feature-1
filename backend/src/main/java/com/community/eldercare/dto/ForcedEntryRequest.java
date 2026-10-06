package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 破门 / 请锁匠处置登记请求。
 * 授权依据、授权人、执行人、现场见证人为必填，避免救助后责任争议。
 */
public class ForcedEntryRequest {

    /** LOCKSMITH 请锁匠开锁 / FORCED_ENTRY 破门进入 */
    @NotBlank
    private String method;

    /** FAMILY_PHONE 家属电话授权 / FAMILY_ONSITE 家属现场授权 /
     *  POLICE_ONSITE 民警现场授权 / COMMUNITY_EMERGENCY 社区应急预案授权 */
    @NotBlank
    private String authorizationBasis;
    private String authorizationDetail;
    @NotBlank
    private String authorizedBy;

    @NotBlank
    private String executorName;
    private String executorPhone;

    /** 现场见证人（多人逗号分隔） */
    @NotBlank
    private String witnessNames;
    private String propertyWitnessName;
    private String policeWitnessName;

    private String damageDescription;

    /* ---- 后续修复与安置（可登记时填写，也可事后补录） ---- */
    private boolean lockReplaced;
    private String lockReplaceNote;
    private boolean doorWindowRepaired;
    private String repairNote;
    /** HOME 家中留观 / HOSPITAL 送医观察 / FAMILY_CARE 家属接回照顾 / TEMP_CARE 社区临时照护点 */
    private String elderPlacement;
    private String placementNote;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getAuthorizationBasis() { return authorizationBasis; }
    public void setAuthorizationBasis(String authorizationBasis) { this.authorizationBasis = authorizationBasis; }
    public String getAuthorizationDetail() { return authorizationDetail; }
    public void setAuthorizationDetail(String authorizationDetail) { this.authorizationDetail = authorizationDetail; }
    public String getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(String authorizedBy) { this.authorizedBy = authorizedBy; }
    public String getExecutorName() { return executorName; }
    public void setExecutorName(String executorName) { this.executorName = executorName; }
    public String getExecutorPhone() { return executorPhone; }
    public void setExecutorPhone(String executorPhone) { this.executorPhone = executorPhone; }
    public String getWitnessNames() { return witnessNames; }
    public void setWitnessNames(String witnessNames) { this.witnessNames = witnessNames; }
    public String getPropertyWitnessName() { return propertyWitnessName; }
    public void setPropertyWitnessName(String propertyWitnessName) { this.propertyWitnessName = propertyWitnessName; }
    public String getPoliceWitnessName() { return policeWitnessName; }
    public void setPoliceWitnessName(String policeWitnessName) { this.policeWitnessName = policeWitnessName; }
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
    public boolean isLockReplaced() { return lockReplaced; }
    public void setLockReplaced(boolean lockReplaced) { this.lockReplaced = lockReplaced; }
    public String getLockReplaceNote() { return lockReplaceNote; }
    public void setLockReplaceNote(String lockReplaceNote) { this.lockReplaceNote = lockReplaceNote; }
    public boolean isDoorWindowRepaired() { return doorWindowRepaired; }
    public void setDoorWindowRepaired(boolean doorWindowRepaired) { this.doorWindowRepaired = doorWindowRepaired; }
    public String getRepairNote() { return repairNote; }
    public void setRepairNote(String repairNote) { this.repairNote = repairNote; }
    public String getElderPlacement() { return elderPlacement; }
    public void setElderPlacement(String elderPlacement) { this.elderPlacement = elderPlacement; }
    public String getPlacementNote() { return placementNote; }
    public void setPlacementNote(String placementNote) { this.placementNote = placementNote; }
}
