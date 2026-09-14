package com.rutaexpress.report.service;

import com.rutaexpress.report.client.ShipmentReader;
import com.rutaexpress.report.domain.ReportRange;
import com.rutaexpress.report.domain.ShipmentSnapshot;
import com.rutaexpress.report.domain.ShipmentStatus;
import com.rutaexpress.report.dto.KpiResponse;
import com.rutaexpress.report.dto.TopDestination;
import com.rutaexpress.report.dto.TopDestinationsResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final ShipmentReader shipmentReader;
    private final Clock clock;

    public ReportService(ShipmentReader shipmentReader, Clock reportClock) {
        this.shipmentReader = shipmentReader;
        this.clock = reportClock;
    }

    public KpiResponse kpis(String rawRange) {
        Window window = window(rawRange);
        List<ShipmentSnapshot> shipments = shipmentsIn(window);
        Map<String, Long> byStatus = statusCounts(shipments);
        long delivered = count(shipments, ShipmentStatus.ENTREGADO);
        long cancelled = count(shipments, ShipmentStatus.CANCELADO);
        long active = shipments.stream().filter(shipment -> isActive(shipment.estado())).count();

        return new KpiResponse(window.range().value(), window.from(), window.to(), shipments.size(), active,
                delivered, cancelled, averageDeliveredLeadTimeHours(shipments), byStatus);
    }

    public TopDestinationsResponse topDestinations(String rawRange, int limit) {
        Window window = window(rawRange);
        List<TopDestination> destinations = shipmentsIn(window).stream()
                .map(ShipmentSnapshot::direccionDestino)
                .filter(destination -> destination != null && !destination.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> new TopDestination(entry.getKey(), entry.getValue()))
                .toList();
        return new TopDestinationsResponse(window.range().value(), window.from(), window.to(), destinations);
    }

    private List<ShipmentSnapshot> shipmentsIn(Window window) {
        return shipmentReader.findCreatedBetween(window.from().toLocalDate(), window.to().toLocalDate()).stream()
                .filter(shipment -> shipment.fechaCreacion() != null)
                .filter(shipment -> !shipment.fechaCreacion().isBefore(window.from()))
                .filter(shipment -> !shipment.fechaCreacion().isAfter(window.to()))
                .toList();
    }

    private Window window(String rawRange) {
        ReportRange range = ReportRange.from(rawRange);
        LocalDateTime to = LocalDateTime.now(clock);
        return new Window(range, to.minus(range.duration()), to);
    }

    private Map<String, Long> statusCounts(List<ShipmentSnapshot> shipments) {
        Map<ShipmentStatus, Long> rawCounts = shipments.stream()
                .filter(shipment -> shipment.estado() != null)
                .collect(Collectors.groupingBy(ShipmentSnapshot::estado,
                        () -> new EnumMap<>(ShipmentStatus.class), Collectors.counting()));
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (ShipmentStatus status : ShipmentStatus.values()) {
            result.put(status.name(), rawCounts.getOrDefault(status, 0L));
        }
        return Map.copyOf(result);
    }

    private long count(List<ShipmentSnapshot> shipments, ShipmentStatus expected) {
        return shipments.stream().filter(shipment -> shipment.estado() == expected).count();
    }

    private boolean isActive(ShipmentStatus status) {
        return status == ShipmentStatus.CREADO || status == ShipmentStatus.ACEPTADO
                || status == ShipmentStatus.EN_BODEGA || status == ShipmentStatus.EN_RUTA;
    }

    private BigDecimal averageDeliveredLeadTimeHours(List<ShipmentSnapshot> shipments) {
        List<Long> minutes = shipments.stream()
                .filter(shipment -> shipment.estado() == ShipmentStatus.ENTREGADO)
                .filter(shipment -> shipment.fechaActualizacion() != null)
                .map(shipment -> Duration.between(shipment.fechaCreacion(), shipment.fechaActualizacion()).toMinutes())
                .filter(value -> value >= 0)
                .toList();
        if (minutes.isEmpty()) return null;
        return BigDecimal.valueOf(minutes.stream().mapToLong(Long::longValue).average().orElseThrow() / 60d)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private record Window(ReportRange range, LocalDateTime from, LocalDateTime to) {
    }
}
