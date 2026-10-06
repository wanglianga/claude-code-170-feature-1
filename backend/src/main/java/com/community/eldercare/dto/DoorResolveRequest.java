package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 无法开门结案请求（由家属/物业/邻居开门，无需破门）
 */
public class DoorResolveRequest {

    /** FAMILY_OPENED 家属到场开门 / PROPERTY_OPENED 物业协助开门 / NEIGHBOR_OPENED 邻居协助开门 */
    @NotBlank
    private String resolution;
    private String resolveNote;

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getResolveNote() { return resolveNote; }
    public void setResolveNote(String resolveNote) { this.resolveNote = resolveNote; }
}
