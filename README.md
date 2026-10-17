# ms-rutaexpress-reportes

> **Estás viendo main: versión estable de EV1.** El avance de EV2 y las instrucciones actuales para descargar las seis carpetas están en [desarrollo](https://github.com/matizk/ms-rutaexpress-reportes/tree/desarrollo) y en la [guía central del equipo](https://github.com/matizk/ms-rutaexpress-envios/blob/desarrollo/CONTINUAR_EV2_EQUIPO.md). Esta actualización de main solo cambia documentos; no publica la implementación EV2 ni acredita despliegue cloud.

Los repositorios y carpetas se renombraron al español el 17-10-2026. Ver [COLABORACION.md](COLABORACION.md) para el flujo `matizk → desarrollo → entrega → main`. Las instrucciones EV1 que siguen son antecedentes de esta versión, no la configuración del sistema EV2 con los nuevos nombres. Para ejecutar la integración actual, seguir exclusivamente la guía de desarrollo.

Microservicio Spring Boot de reportería operacional para RutaExpress.

## Responsabilidad

Expone consultas de solo lectura. En Entrega 1 obtiene snapshots desde
`ms-rutaexpress-envios` por una URL interna y calcula KPIs sin escribir ni
modificar envíos. El BFF es el único punto que debe exponer estas rutas al
frontend.

```text
Angular -> BFF -> report -> shipments
```

En una etapa posterior, Kafka puede alimentar una proyección propia de
reportería. La API pública no cambia: sólo se reemplaza el adaptador interno de
lectura.

## Endpoints

| Ruta | Descripción |
|---|---|
| `GET /api/report/kpis?range=last24h` | Totales, estados activos, entregados, cancelados y lead time promedio. |
| `GET /api/report/top-destinations?range=last7d&limit=5` | Destinos con más envíos. |
| `GET /actuator/health` | Salud del servicio. |

Los rangos permitidos son `last24h`, `last7d` y `last30d`. `limit` acepta de 1 a
20. Las métricas consideran la fecha de creación del envío; para envíos en
estado `ENTREGADO`, `fechaActualizacion` representa la hora de término usada por
el lead time.

## Ejecutar localmente

Requiere JDK 21 y que `ms-rutaexpress-envios` esté disponible en el puerto
5000 o en la URL indicada.

```powershell
$env:SHIPMENTS_URL = 'http://localhost:5000'
.\mvnw.cmd spring-boot:run
curl.exe 'http://localhost:8082/api/report/kpis?range=last24h'
```

## Pruebas y Docker

```powershell
.\mvnw.cmd clean verify
docker build -t rutaexpress-report:local .
docker run --rm -p 8082:8082 -e SHIPMENTS_URL=http://host.docker.internal:5000 rutaexpress-report:local
```

Las pruebas cubren cálculo de KPIs, filtrado por rango, lead time, orden de
destinos y validación de rangos. No se incluyen credenciales ni tokens en este
repositorio.
