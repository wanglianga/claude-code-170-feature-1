package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发起求助：智能按钮 / 电话 / 社区小程序
 */
public class HelpRequest {

    @NotNull
    private Long elderId;

    /** SMART_BUTTON / PHONE / MINI_APP */
    @NotBlank
    private String source;
    private String sourceDetail;
    private String voiceContent;

    private Double callLongitude;
    private Double callLatitude;
    private String callAddress;

    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceDetail() { return sourceDetail; }
    public void setSourceDetail(String sourceDetail) { this.sourceDetail = sourceDetail; }
    public String getVoiceContent() { return voiceContent; }
    public void setVoiceContent(String voiceContent) { this.voiceContent = voiceContent; }
    public Double getCallLongitude() { return callLongitude; }
    public void setCallLongitude(Double callLongitude) { this.callLongitude = callLongitude; }
    public Double getCallLatitude() { return callLatitude; }
    public void setCallLatitude(Double callLatitude) { this.callLatitude = callLatitude; }
    public String getCallAddress() { return callAddress; }
    public void setCallAddress(String callAddress) { this.callAddress = callAddress; }
}
