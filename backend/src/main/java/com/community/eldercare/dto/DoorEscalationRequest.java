package com.community.eldercare.dto;

/**
 * 启动无法开门多方确认请求
 */
public class DoorEscalationRequest {

    /** 关联的上门核实记录（未能入户那次），可空 */
    private Long onsiteVisitId;
    private boolean elderNoResponse = true;
    private boolean lightOnInside = true;
    private String sceneNote;

    public Long getOnsiteVisitId() { return onsiteVisitId; }
    public void setOnsiteVisitId(Long onsiteVisitId) { this.onsiteVisitId = onsiteVisitId; }
    public boolean isElderNoResponse() { return elderNoResponse; }
    public void setElderNoResponse(boolean elderNoResponse) { this.elderNoResponse = elderNoResponse; }
    public boolean isLightOnInside() { return lightOnInside; }
    public void setLightOnInside(boolean lightOnInside) { this.lightOnInside = lightOnInside; }
    public String getSceneNote() { return sceneNote; }
    public void setSceneNote(String sceneNote) { this.sceneNote = sceneNote; }
}
