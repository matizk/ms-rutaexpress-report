package com.rutaexpress.report.controller;

import com.rutaexpress.report.dto.KpiResponse;
import com.rutaexpress.report.dto.TopDestinationsResponse;
import com.rutaexpress.report.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/kpis")
    public KpiResponse kpis(@RequestParam(defaultValue = "last24h") String range) {
        return reportService.kpis(range);
    }

    @GetMapping("/top-destinations")
    public TopDestinationsResponse topDestinations(
            @RequestParam(defaultValue = "last7d") String range,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        return reportService.topDestinations(range, limit);
    }
}
