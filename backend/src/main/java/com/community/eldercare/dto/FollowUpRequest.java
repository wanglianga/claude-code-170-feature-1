package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * 回访请求（回访完成即可归档；归档会联动更新老人画像与下一次主动关怀计划）
 */
public class FollowUpRequest {

    @NotBlank
    private String result;
    private String method = "电话回访";
    private Integer satisfaction;
    private String note;

    /** 可选：显式指定下一次主动关怀；不指定则由系统根据风险自动排期 */
    private String nextCareType;
    private LocalDateTime nextCareAt;

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Integer getSatisfaction() { return satisfaction; }
    public void setSatisfaction(Integer satisfaction) { this.satisfaction = satisfaction; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getNextCareType() { return nextCareType; }
    public void setNextCareType(String nextCareType) { this.nextCareType = nextCareType; }
    public LocalDateTime getNextCareAt() { return nextCareAt; }
    public void setNextCareAt(LocalDateTime nextCareAt) { this.nextCareAt = nextCareAt; }
}
