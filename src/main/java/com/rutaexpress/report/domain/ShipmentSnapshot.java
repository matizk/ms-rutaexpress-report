package com.rutaexpress.report.domain;

import java.time.LocalDateTime;

/** Read-only contract consumed from ms-rutaexpress-shipments. */
public record ShipmentSnapshot(
        Long id,
        String codigoSeguimiento,
        String direccionDestino,
        ShipmentStatus estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {
}
