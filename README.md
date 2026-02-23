# Price API - Prueba Tecnica Inditex

Servicio REST desarrollado con **Spring Boot 3.2.5** y **Java 17** que consulta el precio aplicable de un producto para una marca en una fecha determinada, aplicando desambiguacion por prioridad cuando existen tarifas solapadas.

## Tecnologias

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- H2 (base de datos en memoria)
- Flyway (migraciones de esquema y datos)
- Caffeine (cache en memoria con TTL configurable)
- Micrometer + Prometheus (metricas de negocio y cache)
- Springdoc OpenAPI 2.5.0 (Swagger UI)
- JUnit 5 + Mockito (tests)
- Maven

## Arquitectura Hexagonal

```
src/main/java/com/inditex/pricing/
├── domain/
│   ├── model/           # Objetos de dominio puros (records)
│   ├── exception/       # Excepciones de dominio
│   └── port/
│       └── in/          # Puertos de entrada (interfaces de casos de uso)
├── application/
│   ├── usecase/         # Implementacion de casos de uso (POJOs, sin @Service)
│   └── port/
│       └── out/         # Puertos de salida: PriceRepositoryPort, PriceMetricsPort, CacheMetricsRecorder
├── adapter/
│   ├── in/web/          # Controladores REST, DTOs de respuesta, @ControllerAdvice
│   └── out/
│       ├── persistence/ # Entidades JPA, repositorios Spring Data
│       ├── cache/       # Decorador Caffeine con TTL configurable
│       └── metrics/     # Adaptador Micrometer/Prometheus
└── config/              # Unico punto de wiring: instancia y conecta puertos e implementaciones
```

**Reglas de dependencia:**
- `domain` → no depende de nada (sin Spring, sin JPA)
- `application` → depende solo de `domain`
- `adapter` → depende de `domain` y `application` (via puertos)
- `config` → conecta todo

## Requisitos previos

- Java 17+
- Maven 3.8+ (o usar el wrapper `./mvnw` incluido)

## Ejecutar la aplicacion

```bash
./mvnw spring-boot:run
```

La aplicacion arranca en `http://localhost:8080`.

## Endpoint

```
GET /api/prices?applicationDate={fecha}&productId={id}&brandId={id}
```

### Parametros de entrada

| Parametro         | Tipo     | Formato                  | Ejemplo              |
|-------------------|----------|--------------------------|----------------------|
| `applicationDate` | DateTime | ISO 8601                 | 2020-06-14T10:00:00  |
| `productId`       | Long     | Identificador de producto| 35455                |
| `brandId`         | Long     | Identificador de marca   | 1                    |

### Respuesta exitosa (200)

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

### Respuestas de error

| Codigo | Descripcion                              |
|--------|------------------------------------------|
| 400    | Parametro ausente o formato invalido     |
| 404    | No existe precio aplicable               |

## Ejemplos de uso

```bash
# Test 1: 14/06 a las 10:00 → tarifa 1, 35.50 EUR
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1"

# Test 2: 14/06 a las 16:00 → tarifa 2, 25.45 EUR
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"

# Test 3: 14/06 a las 21:00 → tarifa 1, 35.50 EUR
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T21:00:00&productId=35455&brandId=1"

# Test 4: 15/06 a las 10:00 → tarifa 3, 30.50 EUR
curl "http://localhost:8080/api/prices?applicationDate=2020-06-15T10:00:00&productId=35455&brandId=1"

# Test 5: 16/06 a las 21:00 → tarifa 4, 38.95 EUR
curl "http://localhost:8080/api/prices?applicationDate=2020-06-16T21:00:00&productId=35455&brandId=1"
```

## Observabilidad y metricas

Con la aplicacion en marcha, Spring Boot Actuator expone los siguientes endpoints:

| Endpoint | Descripcion |
|----------|-------------|
| `GET /actuator/health` | Estado de la aplicacion (UP / DOWN) |
| `GET /actuator/metrics` | Listado de todas las metricas registradas |
| `GET /actuator/metrics/{nombre}` | Valor de una metrica concreta |
| `GET /actuator/prometheus` | Metricas en formato Prometheus (scrape endpoint) |

### Metricas de negocio y cache

| Metrica | Tipo | Tags | Descripcion |
|---------|------|------|-------------|
| `prices.priority_conflicts` | Counter | `productId`, `brandId`, `count` | Conflictos de prioridad detectados (invariante de datos violada) |
| `cache.gets` | Counter | `cache=prices.repository`, `result=hit\|miss` | Aciertos y fallos del cache de precios |

Ejemplos de consulta directa:

```bash
# Estado general
curl http://localhost:8080/actuator/health

# Hits y misses del cache
curl "http://localhost:8080/actuator/metrics/cache.gets"

# Scrape completo para Prometheus
curl http://localhost:8080/actuator/prometheus
```

La configuracion de cache es ajustable en `application.yml`:

```yaml
pricing:
  cache:
    prices:
      max-size: 1000   # entradas maximas
      ttl-hours: 1     # tiempo de vida (horas)
```

## Documentacion interactiva (Swagger UI)

Con la aplicacion en marcha, la documentacion interactiva del API esta disponible en:

| Interfaz | URL |
|----------|-----|
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **OpenAPI JSON** | `http://localhost:8080/v3/api-docs` |
| **OpenAPI YAML** | `http://localhost:8080/v3/api-docs.yaml` |

Swagger UI permite explorar el endpoint, ver los parametros, los esquemas de respuesta y ejecutar peticiones directamente desde el navegador sin necesidad de Postman ni curl.

## Ejecutar tests

```bash
./mvnw test
```

### Cobertura de tests (38 tests)

| Tipo                | Clase                                      | Tests | Descripcion                                                       |
|---------------------|--------------------------------------------|-------|-------------------------------------------------------------------|
| Unitarios           | `FindApplicablePriceServiceTest`           | 6     | Logica de negocio: prioridad, conflictos, delegacion              |
| Unitarios           | `CachingPriceRepositoryAdapterTest`        | 4     | Cache hit/miss, claves distintas, resultado vacio                 |
| Integracion         | `PricePersistenceAdapterIntegrationTest`   | 11    | Consultas JPA, mapeo entidad-dominio, frontera startDate/endDate  |
| Sistema (end-to-end)| `PriceControllerSystemTest`                | 17    | 5 escenarios requeridos, errores 400/404, frontera de fechas      |

## Base de datos

Se usa H2 en memoria con Flyway para inicializacion. La consola H2 esta disponible en `http://localhost:8080/h2-console` con:

- **JDBC URL:** `jdbc:h2:mem:pricesdb`
- **Usuario:** `sa`
- **Password:** *(vacio)*

### Datos iniciales (tabla PRICES)

| BRAND_ID | START_DATE          | END_DATE            | PRICE_LIST | PRODUCT_ID | PRIORITY | PRICE | CURR |
|----------|---------------------|---------------------|------------|------------|----------|-------|------|
| 1        | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1          | 35455      | 0        | 35.50 | EUR  |
| 1        | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2          | 35455      | 1        | 25.45 | EUR  |
| 1        | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3          | 35455      | 1        | 30.50 | EUR  |
| 1        | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4          | 35455      | 1        | 38.95 | EUR  |

## Postman

Se incluye el archivo `Inditex_Price_API.postman_collection.json` listo para importar en Postman con los 5 escenarios de prueba y casos de error, cada uno con scripts de validacion automatica.
