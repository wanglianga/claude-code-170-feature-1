package com.community.eldercare.service;

import com.community.eldercare.domain.HelpEvent;
import com.community.eldercare.repo.HelpEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 社区管理者复盘统计：风险等级、响应时长、服务类型、回访结果，
 * 以及独居老人照护能力相关指标。
 */
@Service
public class StatsService {

    private final HelpEventRepository eventRepo;

    public StatsService(HelpEventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> overview(String from, String to) {
        LocalDateTime f = parse(from, LocalDate.now().minusDays(30).atStartOfDay());
        LocalDateTime t = parse(to, LocalDate.now().plusDays(1).atStartOfDay());
        List<HelpEvent> events = eventRepo.findByTimeRange(f, t);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("range", List.of(f.toString(), t.toString()));
        result.put("totalEvents", events.size());

        long closed = events.stream().filter(e -> "CLOSED".equals(e.getStatus())).count();
        result.put("closedEvents", closed);
        result.put("openEvents", events.size() - closed);
        result.put("nightEvents", events.stream().filter(HelpEvent::isNightHelp).count());
        result.put("hospitalTransfer", events.stream().filter(HelpEvent::isHospitalTransfer).count());
        result.put("falseTrigger", events.stream().filter(e -> "FALSE_TRIGGER".equals(e.getTriageType())).count());

        // 按风险等级（以研判严重程度近似）
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (String s : List.of("CRITICAL", "HIGH", "MEDIUM", "LOW")) {
            final String key = s;
            bySeverity.put(key, events.stream().filter(e -> key.equals(e.getSeverity())).count());
        }
        result.put("bySeverity", bySeverity);

        // 按研判类型
        Map<String, Long> byType = new LinkedHashMap<>();
        for (String s : List.of("LIFE_ASSIST", "BODY_DISCOMFORT", "FALL_RISK", "EMOTIONAL_CRISIS", "FALSE_TRIGGER")) {
            final String key = s;
            byType.put(key, events.stream().filter(e -> key.equals(e.getTriageType())).count());
        }
        result.put("byTriageType", byType);

        // 按服务类型（一个事件可能有多个服务类型，拆分计数）
        Map<String, Long> byService = new LinkedHashMap<>();
        for (HelpEvent e : events) {
            if (e.getServiceType() != null) {
                for (String s : e.getServiceType().split(",")) {
                    if (!s.isBlank()) {
                        byService.merge(s, 1L, Long::sum);
                    }
                }
            }
        }
        result.put("byServiceType", byService);

        // 按回访结果
        Map<String, Long> byFollowUp = new LinkedHashMap<>();
        for (String s : List.of("IMPROVED", "STABLE", "WORSENED", "NO_ANSWER")) {
            final String key = s;
            byFollowUp.put(key, events.stream().filter(e -> key.equals(e.getFollowUpResult())).count());
        }
        result.put("byFollowUpResult", byFollowUp);

        // 响应时长统计（分钟）
        IntSummaryStatistics resp = events.stream()
                .filter(e -> e.getResponseMinutes() != null)
                .mapToInt(HelpEvent::getResponseMinutes)
                .summaryStatistics();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", resp.getCount());
        response.put("avgMinutes", resp.getCount() == 0 ? 0 : Math.round(resp.getAverage()));
        response.put("maxMinutes", resp.getCount() == 0 ? 0 : resp.getMax());
        response.put("minMinutes", resp.getCount() == 0 ? 0 : resp.getMin());
        // 15 分钟内到场率
        long within15 = events.stream()
                .filter(e -> e.getResponseMinutes() != null && e.getResponseMinutes() <= 15).count();
        long hasResp = resp.getCount();
        response.put("within15Rate", hasResp == 0 ? 0 : Math.round(100.0 * within15 / hasResp));
        result.put("response", response);

        // 风险标记出现次数
        Map<String, Long> flags = new LinkedHashMap<>();
        for (HelpEvent e : events) {
            if (e.getRiskFlags() != null) {
                for (String s : e.getRiskFlags().split(",")) {
                    if (!s.isBlank()) {
                        flags.merge(s, 1L, Long::sum);
                    }
                }
            }
        }
        result.put("riskFlags", flags);

        // 每日求助趋势
        Map<String, Long> daily = new TreeMap<>();
        for (HelpEvent e : events) {
            String d = e.getCreatedAt().toLocalDate().toString();
            daily.merge(d, 1L, Long::sum);
        }
        result.put("dailyTrend", daily);

        return result;
    }

    private LocalDateTime parse(String s, LocalDateTime def) {
        if (s == null || s.isBlank()) {
            return def;
        }
        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (Exception e) {
            return def;
        }
    }
}
