# HTTP methods used in this API

A quick reminder of which verb does what, and which endpoints use each.

## GET

Read-only. Never changes data on the server.

- `GET /api/v1/` — list the top-level links.
- `GET /rooms` — list rooms.
- `GET /rooms/{id}` — one room.
- `GET /sensors` — list sensors.
- `GET /sensors?type=Temperature` — filtered list.
- `GET /sensors/{id}` — one sensor.
- `GET /sensors/{id}/readings` — all readings for one sensor.

## POST

Create a new resource. The server usually replies with `201 Created`.

- `POST /rooms`
- `POST /sensors`
- `POST /sensors/{id}/readings`

## PUT

Replace a resource in full. All fields must be present in the body.

- `PUT /rooms/{id}`
- `PUT /sensors/{id}`

## DELETE

Remove a resource. Responds with `204 No Content` on success.

- `DELETE /rooms/{id}` (fails with `409` if the room still has sensors).
- `DELETE /sensors/{id}` (also removes the sensor id from its room).

## Why no PATCH?

PUT is intentionally full-replace here because every endpoint takes a
small, well-defined body. Adding PATCH would duplicate functionality for
no real gain on a project of this size.
