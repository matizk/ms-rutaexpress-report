# ms-rutaexpress-report

Microservicio de reportería de RutaExpress.

## Responsabilidad definida por el caso

- Exponer KPIs y analítica de operaciones.
- Proveer endpoints de solo lectura.

## Endpoints definidos por el caso

- `GET /api/report/kpis?range=last24h`
- `GET /api/report/top-services?range=last7d`

## Estado en Entrega 1

El repositorio se crea ahora para completar la separación de servicios acordada. La implementación de Kafka, Zookeeper, auditoría y procesamiento de eventos queda pendiente de confirmación y no se incorpora todavía.
