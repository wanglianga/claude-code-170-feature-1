package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 后续服务请求
 */
public class EventServiceRequest {

    @NotBlank
    private String serviceType;
    @NotBlank
    private String providerName;
    private String providerPhone;
    private String content;
    private String status = "DONE";

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
