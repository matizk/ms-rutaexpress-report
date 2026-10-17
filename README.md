# ms-rutaexpress-report

> Ramas del equipo: `matizk` para trabajo personal, `desarrollo` para integración y `main` para la entrega estable. Ver [CONTRIBUTING.md](CONTRIBUTING.md) y la [guía EV2](https://github.com/matizk/ms-rutaexpress-shipments/blob/desarrollo/CONTINUAR_EV2_EQUIPO.md). Este ajuste no cambia el código funcional de Reportes ni certifica el despliegue cloud.

Microservicio Spring Boot de reportería operacional para RutaExpress.

## Responsabilidad

Expone consultas de solo lectura. En Entrega 1 obtiene snapshots desde
`ms-rutaexpress-shipments` por una URL interna y calcula KPIs sin escribir ni
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

Requiere JDK 21 y que `ms-rutaexpress-shipments` esté disponible en el puerto
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
