package com.community.eldercare.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 破门救助后续补录：修复完成、锁具更换、费用垫付与老人安置情况。
 * 所有字段可选，仅更新非空字段。
 */
public class RescueUpdateRequest {

    private String sceneDescription;
    private String scenePhotoUrls;

    private Boolean lockReplaced;
    private String lockModel;
    private String newKeyKeptBy;

    private String repairArrangement;
    private LocalDateTime repairedAt;

    private String advancedBy;
    private BigDecimal advancedAmount;

    private String elderPlacement;
    private String placementNote;

    private Boolean propertyWitnessed;
    private String propertyWitnessName;

    public String getSceneDescription() { return sceneDescription; }
    public void setSceneDescription(String sceneDescription) { this.sceneDescription = sceneDescription; }
    public String getScenePhotoUrls() { return scenePhotoUrls; }
    public void setScenePhotoUrls(String scenePhotoUrls) { this.scenePhotoUrls = scenePhotoUrls; }
    public Boolean getLockReplaced() { return lockReplaced; }
    public void setLockReplaced(Boolean lockReplaced) { this.lockReplaced = lockReplaced; }
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
    public Boolean getPropertyWitnessed() { return propertyWitnessed; }
    public void setPropertyWitnessed(Boolean propertyWitnessed) { this.propertyWitnessed = propertyWitnessed; }
    public String getPropertyWitnessName() { return propertyWitnessName; }
    public void setPropertyWitnessName(String propertyWitnessName) { this.propertyWitnessName = propertyWitnessName; }
}
