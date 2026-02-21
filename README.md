# Price API - Prueba Tecnica Inditex

Servicio REST desarrollado con **Spring Boot 3.2.5** y **Java 17** que consulta el precio aplicable de un producto para una marca en una fecha determinada, aplicando desambiguacion por prioridad cuando existen tarifas solapadas.

## Tecnologias

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- H2 (base de datos en memoria)
- Flyway (migraciones de esquema y datos)
- JUnit 5 + Mockito (tests)
- Maven

## Arquitectura Hexagonal

```
src/main/java/com/inditex/pricing/
├── domain/
│   ├── model/           # Objetos de dominio puros (records)
│   └── port/
│       ├── in/          # Puertos de entrada (interfaces de casos de uso)
│       └── out/         # Puertos de salida (interfaces de repositorio)
├── application/
│   └── usecase/         # Implementacion de casos de uso
├── adapter/
│   ├── in/web/          # Controladores REST, DTOs de respuesta
│   └── out/persistence/ # Entidades JPA, repositorios Spring Data
└── config/              # Configuracion de beans y wiring
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

## Ejecutar tests

```bash
./mvnw test
```

### Cobertura de tests (22 tests)

| Tipo                | Clase                                      | Descripcion                                          |
|---------------------|--------------------------------------------|------------------------------------------------------|
| Unitarios           | `FindApplicablePriceServiceTest`           | Logica de negocio y desambiguacion por prioridad     |
| Integracion         | `PricePersistenceAdapterIntegrationTest`   | Consultas JPA, mapeo entidad-dominio                 |
| Sistema (end-to-end)| `PriceControllerSystemTest`                | 5 escenarios requeridos + casos de error (400, 404)  |

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
