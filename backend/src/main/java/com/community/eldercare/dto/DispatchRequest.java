package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 转介/通知请求
 */
public class DispatchRequest {

    /** GRID_WORKER / NEIGHBOR_VOLUNTEER / FAMILY_DOCTOR / EMERGENCY_120 */
    @NotBlank
    private String targetType;
    @NotBlank
    private String targetName;
    private String targetPhone;
    private String notifyChannel = "电话";
    private String note;

    /** 邻居代报相关 */
    private boolean neighborReported;
    private boolean neighborInfoClear = true;

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getTargetPhone() { return targetPhone; }
    public void setTargetPhone(String targetPhone) { this.targetPhone = targetPhone; }
    public String getNotifyChannel() { return notifyChannel; }
    public void setNotifyChannel(String notifyChannel) { this.notifyChannel = notifyChannel; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isNeighborReported() { return neighborReported; }
    public void setNeighborReported(boolean neighborReported) { this.neighborReported = neighborReported; }
    public boolean isNeighborInfoClear() { return neighborInfoClear; }
    public void setNeighborInfoClear(boolean neighborInfoClear) { this.neighborInfoClear = neighborInfoClear; }
}
