# Manual test plan

A checklist you can walk through end-to-end to convince yourself the
service is behaving. Tick each box as you go.

## Before you start

- [ ] Build the fat JAR: `mvn -q clean package`
- [ ] Start the server: `java -jar target/smart-campus-api.jar`

## Happy path

- [ ] `GET /api/v1/` returns three links (self, rooms, sensors).
- [ ] `GET /api/v1/rooms` returns the two seeded rooms.
- [ ] `GET /api/v1/rooms/LIB-301` returns capacity 40.
- [ ] `GET /api/v1/sensors` returns three seeded sensors.
- [ ] `GET /api/v1/sensors?type=Temperature` returns only `TEMP-001`.
- [ ] `POST /api/v1/rooms` with `MATH-201` returns `201 Created`.
- [ ] `GET /api/v1/rooms/MATH-201` returns the room you just made.
- [ ] `POST /api/v1/sensors/TEMP-001/readings` with `{"value": 22.4}` returns `201`.
- [ ] `GET /api/v1/sensors/TEMP-001` now shows `currentValue: 22.4`.
- [ ] `GET /api/v1/sensors/TEMP-001/readings` returns your reading.

## Error paths

- [ ] `GET /api/v1/rooms/DOES-NOT-EXIST` -> `404`.
- [ ] `POST /api/v1/sensors` with `"roomId":"NO-SUCH-ROOM"` -> `422`.
- [ ] `DELETE /api/v1/rooms/LIB-301` (still has sensors) -> `409`.
- [ ] `POST /api/v1/rooms` without `Content-Type: application/json` -> `415`.
- [ ] Set a sensor to `MAINTENANCE` via PUT, then `POST` a reading -> `403`.

## Clean-up

- [ ] `DELETE /api/v1/rooms/MATH-201` -> `204`.
- [ ] `GET /api/v1/rooms/MATH-201` -> `404`.

## Shut down

- [ ] Ctrl+C the server, or run `.\scripts\stop.ps1`.
