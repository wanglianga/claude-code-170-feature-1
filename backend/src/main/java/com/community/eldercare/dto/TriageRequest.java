package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 值班社工研判请求
 */
public class TriageRequest {

    /** LIFE_ASSIST / BODY_DISCOMFORT / FALL_RISK / EMOTIONAL_CRISIS / FALSE_TRIGGER */
    @NotBlank
    private String triageType;
    /** LOW / MEDIUM / HIGH / CRITICAL */
    @NotBlank
    private String severity;
    private String triageNote;

    public String getTriageType() { return triageType; }
    public void setTriageType(String triageType) { this.triageType = triageType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getTriageNote() { return triageNote; }
    public void setTriageNote(String triageNote) { this.triageNote = triageNote; }
}
