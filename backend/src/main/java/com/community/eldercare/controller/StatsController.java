package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public R<java.util.Map<String, Object>> overview(@RequestParam(required = false) String from,
                                                     @RequestParam(required = false) String to) {
        return R.ok(statsService.overview(from, to));
    }
}
