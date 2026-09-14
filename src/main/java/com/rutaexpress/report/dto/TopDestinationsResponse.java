package com.rutaexpress.report.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TopDestinationsResponse(
        String range,
        LocalDateTime from,
        LocalDateTime to,
        List<TopDestination> destinations) {
}
