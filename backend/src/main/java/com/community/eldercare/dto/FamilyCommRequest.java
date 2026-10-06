package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 家属沟通请求
 */
public class FamilyCommRequest {

    private Long contactId;
    @NotBlank
    private String contactName;
    private String contactPhone;
    private String channel = "电话";
    private boolean reached;
    /** 积极配合 / 一般 / 推诿拒绝 / 未接通 */
    private String attitude;
    private String note;

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public boolean isReached() { return reached; }
    public void setReached(boolean reached) { this.reached = reached; }
    public String getAttitude() { return attitude; }
    public void setAttitude(String attitude) { this.attitude = attitude; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
