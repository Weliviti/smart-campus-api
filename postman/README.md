# Postman collection

This folder contains a Postman collection + environment you can import to
drive the Smart Campus API without curl.

## Import

1. Open Postman.
2. **File → Import** → drag in both:
   - `smart-campus.postman_collection.json`
   - `smart-campus.postman_environment.json`
3. In the environment dropdown (top-right) pick **Smart Campus Local**.

## Run the API first

From the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
```

The environment's `baseUrl` points at `http://localhost:8080/api/v1`. If
you started the server on a different port with `-Port 9090`, edit the
`baseUrl` in the environment.

## What's covered

- **Discovery** — `GET /`
- **Rooms** — list, single, create, delete
- **Sensors** — list, filter by `?type=`, create (valid + 422 bad roomId)
- **Readings** — list + post under `/sensors/{id}/readings`

## Useful request ordering

Open the collection and run these in order to see the full CRUD flow:

1. `Discovery > GET /` — confirm links
2. `Rooms > POST /rooms` — creates MATH-201
3. `Rooms > GET /rooms` — MATH-201 appears
4. `Rooms > DELETE /rooms/MATH-201` — clean up
5. `Sensors > POST /sensors (bad roomId -> 422)` — demonstrates the
   `LINKED_RESOURCE_NOT_FOUND` error body

## Variables

| Key        | Default                               | Used in                          |
|------------|---------------------------------------|----------------------------------|
| `baseUrl`  | `http://localhost:8080/api/v1`        | Every request                    |
| `roomId`   | `LIB-301`                             | Room-specific requests           |
| `sensorId` | `TEMP-001`                            | Sensor and reading paths         |
