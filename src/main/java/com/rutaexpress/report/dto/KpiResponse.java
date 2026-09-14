package com.rutaexpress.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record KpiResponse(
        String range,
        LocalDateTime from,
        LocalDateTime to,
        long totalShipments,
        long activeShipments,
        long deliveredShipments,
        long cancelledShipments,
        BigDecimal averageDeliveredLeadTimeHours,
        Map<String, Long> shipmentsByStatus) {
}
