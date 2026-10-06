package com.community.eldercare.dto;

/**
 * 破门后续修复与安置补录请求（锁具更换、门窗修复、物业见证、老人安置）。
 */
public class ForcedEntryUpdateRequest {

    private boolean lockReplaced;
    private String lockReplaceNote;
    private boolean doorWindowRepaired;
    private String repairNote;
    private String propertyWitnessName;
    private String elderPlacement;
    private String placementNote;
    private String damageDescription;

    public boolean isLockReplaced() { return lockReplaced; }
    public void setLockReplaced(boolean lockReplaced) { this.lockReplaced = lockReplaced; }
    public String getLockReplaceNote() { return lockReplaceNote; }
    public void setLockReplaceNote(String lockReplaceNote) { this.lockReplaceNote = lockReplaceNote; }
    public boolean isDoorWindowRepaired() { return doorWindowRepaired; }
    public void setDoorWindowRepaired(boolean doorWindowRepaired) { this.doorWindowRepaired = doorWindowRepaired; }
    public String getRepairNote() { return repairNote; }
    public void setRepairNote(String repairNote) { this.repairNote = repairNote; }
    public String getPropertyWitnessName() { return propertyWitnessName; }
    public void setPropertyWitnessName(String propertyWitnessName) { this.propertyWitnessName = propertyWitnessName; }
    public String getElderPlacement() { return elderPlacement; }
    public void setElderPlacement(String elderPlacement) { this.elderPlacement = elderPlacement; }
    public String getPlacementNote() { return placementNote; }
    public void setPlacementNote(String placementNote) { this.placementNote = placementNote; }
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
}
