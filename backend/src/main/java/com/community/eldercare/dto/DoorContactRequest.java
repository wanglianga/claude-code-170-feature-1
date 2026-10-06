package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 多方联系步骤登记请求（家属 → 物业 → 邻居 → 派出所）
 */
public class DoorContactRequest {

    /** FAMILY 家属 / PROPERTY 物业 / NEIGHBOR 邻居 / POLICE 派出所 */
    @NotBlank
    private String partyType;
    @NotBlank
    private String contactName;
    private String contactPhone;
    private String channel = "电话";
    private boolean reached;
    private String feedback;
    private boolean willComeToScene;

    public String getPartyType() { return partyType; }
    public void setPartyType(String partyType) { this.partyType = partyType; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public boolean isReached() { return reached; }
    public void setReached(boolean reached) { this.reached = reached; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public boolean isWillComeToScene() { return willComeToScene; }
    public void setWillComeToScene(boolean willComeToScene) { this.willComeToScene = willComeToScene; }
}
