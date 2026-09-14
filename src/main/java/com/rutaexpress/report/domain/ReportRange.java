package com.rutaexpress.report.domain;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum ReportRange {
    LAST_24_HOURS("last24h", Duration.ofHours(24)),
    LAST_7_DAYS("last7d", Duration.ofDays(7)),
    LAST_30_DAYS("last30d", Duration.ofDays(30));

    private final String value;
    private final Duration duration;

    ReportRange(String value, Duration duration) {
        this.value = value;
        this.duration = duration;
    }

    public String value() {
        return value;
    }

    public Duration duration() {
        return duration;
    }

    public static ReportRange from(String raw) {
        for (ReportRange range : values()) {
            if (range.value.equals(raw)) return range;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "range must be one of: last24h, last7d, last30d");
    }
}
