package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 家属到场确认请求：查看完整处置轨迹后确认费用承担方案。
 */
public class DoorFamilyReviewRequest {

    @NotBlank
    private String familyName;
    private String relationship;
    private boolean viewedTrajectory = true;
    /** AGREE 认可费用分担 / PARTIAL 部分认可 / DISAGREE 有异议 */
    @NotBlank
    private String costOpinion;
    private String comment;

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public boolean isViewedTrajectory() { return viewedTrajectory; }
    public void setViewedTrajectory(boolean viewedTrajectory) { this.viewedTrajectory = viewedTrajectory; }
    public String getCostOpinion() { return costOpinion; }
    public void setCostOpinion(String costOpinion) { this.costOpinion = costOpinion; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
