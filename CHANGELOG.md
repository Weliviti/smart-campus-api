# Changelog

All notable changes to the Smart Campus API are documented in this file.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- JUnit 5 unit tests for model POJOs (`Room`, `Sensor`, `SensorReading`, `ApiError`).
- JUnit 5 unit tests for `DataStore` covering rooms, sensors, and readings.
- Test covering exception message payloads.
- `docs/architecture.md` describing request/response flow.
- `docs/error-codes.md` mapping domain exceptions to HTTP status codes.
- `docs/api-spec.md` with per-endpoint contract.
- GitHub Actions workflow `ci.yml` (Maven `verify` on push/PR).
- Helper PowerShell scripts: `run.ps1`, `stop.ps1`.
- `.editorconfig` + `.gitattributes` for consistent formatting/line-endings.
- `LICENSE` (MIT) and `CONTRIBUTING.md`.

## [0.1.0] - 2026-04-18

### Added
- Initial Smart Campus API scaffold.
- JAX-RS `Application` subclass at `/api/v1`.
- Resources: `/`, `/rooms`, `/sensors`, `/sensors/{id}/readings`.
- Sub-resource locator for readings.
- `@QueryParam("type")` filter on `/sensors`.
- Exception mappers for `409`, `422`, `403`, and generic `500` responses.
- `LoggingFilter` (request + response) and `logging.properties` config.
- Embedded Grizzly 2 server via Jersey 2.41.
- Maven `shade` configuration for a runnable fat JAR.
