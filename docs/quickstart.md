# Quick start

Five minutes from clone to first HTTP 200.

## Prerequisites

- JDK 11 or newer (`java -version`)
- Maven 3.8+ (`mvn -v`)

If you'd rather use Docker, skip to [docker.md](docker.md).

## Build

```bash
mvn -q clean package
```

Output: `target/smart-campus-api.jar` (fat JAR, ~15 MB).

## Run

```bash
java -jar target/smart-campus-api.jar
```

Or use the PowerShell helper:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
```

You should see a log line like:

```
Smart Campus API running at http://0.0.0.0:8080/api/v1
```

## Verify it's alive

```bash
curl -s http://localhost:8080/api/v1/
```

Expected:

```json
{
  "self":    "/api/v1",
  "rooms":   "/api/v1/rooms",
  "sensors": "/api/v1/sensors"
}
```

## Poke around with seeded data

```bash
curl -s http://localhost:8080/api/v1/rooms | jq
curl -s http://localhost:8080/api/v1/sensors | jq
curl -s 'http://localhost:8080/api/v1/sensors?type=Temperature' | jq
```

## Add data

```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
     -H 'Content-Type: application/json' \
     -d '{"id":"MATH-201","name":"Maths Lecture 201","capacity":120}'
```

## Record a reading

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H 'Content-Type: application/json' \
     -d '{"value": 22.7}'
```

## Stop the server

`Ctrl+C` in the terminal, or:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1
```

## Smoke test

Once it's running, verify the core flows at once:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

(Or `./scripts/smoke-test.sh` on macOS/Linux.)
