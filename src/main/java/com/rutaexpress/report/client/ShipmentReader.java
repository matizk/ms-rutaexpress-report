package com.rutaexpress.report.client;

import com.rutaexpress.report.domain.ShipmentSnapshot;
import java.time.LocalDate;
import java.util.List;

public interface ShipmentReader {
    List<ShipmentSnapshot> findCreatedBetween(LocalDate from, LocalDate to);
}
