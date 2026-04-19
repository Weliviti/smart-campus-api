# API Spec

Base URI: `http://localhost:8080/api/v1`

All requests and responses use `application/json; charset=utf-8`.

## Discovery

### GET `/`

Returns HATEOAS-style links to the top-level collections.

**200 OK**
```json
{
  "self":    "/api/v1",
  "rooms":   "/api/v1/rooms",
  "sensors": "/api/v1/sensors"
}
```

---

## Rooms

### GET `/rooms`
List all rooms.

### GET `/rooms/{id}`
Get one room.
- `404 NOT_FOUND` if the id doesn't exist.

### POST `/rooms`
Create a room.

**Request**
```json
{ "id": "LIB-301", "name": "Main Library Room 301", "capacity": 40 }
```

- `201 Created` with the persisted body and `Location` header.
- `400 BAD_REQUEST` if `id`, `name`, or `capacity` is missing/invalid.
- `409` if `id` already exists.

### PUT `/rooms/{id}`
Full replacement of a room's fields.

### DELETE `/rooms/{id}`
- `204 No Content` on success.
- `409 ROOM_NOT_EMPTY` if any sensor still references this room.
- `404 NOT_FOUND` if the id doesn't exist.

---

## Sensors

### GET `/sensors`
List sensors. Optional filter:

| Param  | Type   | Effect                                   |
|--------|--------|------------------------------------------|
| `type` | string | Return only sensors with matching `type` |

Example: `GET /sensors?type=temperature`.

### GET `/sensors/{id}`
Get one sensor (including its current status + currentValue).

### POST `/sensors`
Create a sensor.

**Request**
```json
{
  "id": "TEMP-007",
  "type": "temperature",
  "unit": "C",
  "roomId": "LIB-301",
  "status": "ACTIVE"
}
```

- `201 Created` with `Location: /api/v1/sensors/TEMP-007`.
- `422 LINKED_RESOURCE_NOT_FOUND` if `roomId` isn't a real room.

### PUT `/sensors/{id}`
Replace a sensor's properties.

### DELETE `/sensors/{id}`
- `204 No Content`.
- Removes the sensor's id from its room's `sensorIds` list.

---

## Readings (sub-resource of a sensor)

### GET `/sensors/{sensorId}/readings`
Return the full reading history for a sensor, newest first.

### POST `/sensors/{sensorId}/readings`
Record a new reading.

**Request**
```json
{ "value": 23.7 }
```

Server generates `id` (UUID) and `timestamp` (epoch ms).

- `201 Created` with the full reading body.
- `403 SENSOR_UNAVAILABLE` if the sensor's status is not `ACTIVE`.
- `404 NOT_FOUND` if `{sensorId}` doesn't exist.
- Side effect: updates the sensor's `currentValue` to the new reading.

---

## Status summary

| Code | When                                                            |
|------|-----------------------------------------------------------------|
| 200  | Successful read                                                 |
| 201  | Successful create                                               |
| 204  | Successful delete                                               |
| 400  | Validation failure on body                                      |
| 403  | Writing to a sensor that is not `ACTIVE`                        |
| 404  | Addressing an id that doesn't exist                             |
| 409  | Deleting a room that still has sensors                          |
| 422  | Creating a sensor with a non-existent `roomId`                  |
| 500  | Unhandled server error (safety net)                             |
