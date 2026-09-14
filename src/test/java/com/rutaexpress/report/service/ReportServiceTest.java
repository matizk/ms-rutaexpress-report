package com.rutaexpress.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rutaexpress.report.client.ShipmentReader;
import com.rutaexpress.report.domain.ShipmentSnapshot;
import com.rutaexpress.report.domain.ShipmentStatus;
import com.rutaexpress.report.dto.KpiResponse;
import com.rutaexpress.report.dto.TopDestinationsResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ReportServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-14T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void calculatesOperationalKpisOnlyInsideTheRequestedRange() {
        ReportService service = service(List.of(
                shipment("RX-1", "Valparaíso", ShipmentStatus.CREADO, "2026-09-14T11:30:00", "2026-09-14T11:30:00"),
                shipment("RX-2", "Santiago", ShipmentStatus.ENTREGADO, "2026-09-14T10:00:00", "2026-09-14T11:30:00"),
                shipment("RX-3", "Santiago", ShipmentStatus.CANCELADO, "2026-09-14T11:00:00", "2026-09-14T11:15:00"),
                shipment("RX-OLD", "Concepción", ShipmentStatus.ENTREGADO, "2026-09-12T10:00:00", "2026-09-12T12:00:00")));

        KpiResponse response = service.kpis("last24h");

        assertThat(response.totalShipments()).isEqualTo(3);
        assertThat(response.activeShipments()).isEqualTo(1);
        assertThat(response.deliveredShipments()).isEqualTo(1);
        assertThat(response.cancelledShipments()).isEqualTo(1);
        assertThat(response.averageDeliveredLeadTimeHours()).isEqualByComparingTo("1.50");
        assertThat(response.shipmentsByStatus()).containsEntry("CREADO", 1L)
                .containsEntry("ENTREGADO", 1L).containsEntry("CANCELADO", 1L)
                .containsEntry("EN_RUTA", 0L);
    }

    @Test
    void ordersTopDestinationsByCountAndThenName() {
        ReportService service = service(List.of(
                shipment("RX-1", "Santiago", ShipmentStatus.CREADO, "2026-09-14T11:00:00", "2026-09-14T11:00:00"),
                shipment("RX-2", "Valparaíso", ShipmentStatus.CREADO, "2026-09-14T10:00:00", "2026-09-14T10:00:00"),
                shipment("RX-3", "Santiago", ShipmentStatus.EN_RUTA, "2026-09-14T09:00:00", "2026-09-14T09:30:00"),
                shipment("RX-4", "Valparaíso", ShipmentStatus.ACEPTADO, "2026-09-14T08:00:00", "2026-09-14T08:00:00")));

        TopDestinationsResponse response = service.topDestinations("last24h", 2);

        assertThat(response.destinations()).extracting(destination -> destination.destination())
                .containsExactly("Santiago", "Valparaíso");
        assertThat(response.destinations()).extracting(destination -> destination.shipments())
                .containsExactly(2L, 2L);
    }

    @Test
    void rejectsUnknownRanges() {
        ReportService service = service(List.of());
        assertThatThrownBy(() -> service.kpis("today"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("last24h");
    }

    private ReportService service(List<ShipmentSnapshot> shipments) {
        ShipmentReader reader = (from, to) -> shipments;
        return new ReportService(reader, CLOCK);
    }

    private ShipmentSnapshot shipment(String code, String destination, ShipmentStatus status,
                                      String created, String updated) {
        return new ShipmentSnapshot(1L, code, destination, status,
                LocalDateTime.parse(created), LocalDateTime.parse(updated));
    }
}
