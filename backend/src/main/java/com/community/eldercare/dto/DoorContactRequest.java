package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * 无法开门时的多方联系确认请求（家属 → 物业 → 邻居 → 派出所）。
 */
public class DoorContactRequest {

    /** FAMILY / PROPERTY / NEIGHBOR / POLICE，不传则按已登记次数自动取下一方 */
    private String partyType;

    @NotBlank
    private String partyName;
    private String partyPhone;
    private String channel = "电话";
    private LocalDateTime contactedAt;

    private boolean reached;
    private String feedback;
    private Boolean willArrive;
    private Integer etaMinutes;
    private String note;

    public String getPartyType() { return partyType; }
    public void setPartyType(String partyType) { this.partyType = partyType; }
    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }
    public String getPartyPhone() { return partyPhone; }
    public void setPartyPhone(String partyPhone) { this.partyPhone = partyPhone; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public LocalDateTime getContactedAt() { return contactedAt; }
    public void setContactedAt(LocalDateTime contactedAt) { this.contactedAt = contactedAt; }
    public boolean isReached() { return reached; }
    public void setReached(boolean reached) { this.reached = reached; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Boolean getWillArrive() { return willArrive; }
    public void setWillArrive(Boolean willArrive) { this.willArrive = willArrive; }
    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
