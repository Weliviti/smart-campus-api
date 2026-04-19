# Error codes

All non-2xx responses return a uniform `ApiError` JSON body:

```json
{
  "status": 409,
  "error": "ROOM_NOT_EMPTY",
  "message": "Room 'LIB-301' still has 3 sensors attached. Detach them first.",
  "timestamp": 1745152345123
}
```

| HTTP | `error` code                  | Trigger                                                          | Mapper class                           |
|------|-------------------------------|------------------------------------------------------------------|----------------------------------------|
| 400  | `BAD_REQUEST`                 | Missing required body field, malformed JSON, bad enum value      | handled by Jersey + `GlobalExceptionMapper` |
| 403  | `SENSOR_UNAVAILABLE`          | Writing a reading to a sensor whose status is not `ACTIVE`       | `SensorUnavailableExceptionMapper`     |
| 404  | `NOT_FOUND`                   | Addressing an id that doesn't exist                              | Jersey default (via `WebApplicationException`) |
| 405  | `METHOD_NOT_ALLOWED`          | e.g. `PUT /sensors` (collection URIs don't accept PUT)           | Jersey default                         |
| 409  | `ROOM_NOT_EMPTY`              | `DELETE /rooms/{id}` while the room still references sensors     | `RoomNotEmptyExceptionMapper`          |
| 415  | `UNSUPPORTED_MEDIA_TYPE`      | Body present without `Content-Type: application/json`            | Jersey default                         |
| 422  | `LINKED_RESOURCE_NOT_FOUND`   | `POST /sensors` with a `roomId` that does not exist              | `LinkedResourceNotFoundExceptionMapper`|
| 500  | `INTERNAL_ERROR`              | Uncaught exception (safety net)                                  | `GlobalExceptionMapper`                |

## Why 422 and not 404 for "linked resource missing"?

A `POST /sensors` with `roomId=does-not-exist` is syntactically valid (the
URL `/sensors` is addressable and the body parses) but semantically
incoherent. RFC 4918 and common REST practice use **422 Unprocessable
Entity** for this — the server understood the request but the payload
fails a business rule. Returning 404 would be wrong because the **sensor**
collection was found and the request is about creating a resource inside
it, not fetching one.

## Why 403 and not 409 for "sensor in maintenance"?

409 is for state conflicts you could fix yourself (rename the clashing id,
remove the dependents). Writing to a sensor in `MAINTENANCE` or `OFFLINE`
is a permission-style block — the caller isn't allowed, even though the
request is otherwise valid. 403 maps best.

## How 500 is handled

`GlobalExceptionMapper<Throwable>` is the last-chance mapper. It:

1. Passes through anything that is already a `WebApplicationException`
   (preserving the original status and body).
2. For everything else, logs the full stack trace server-side.
3. Returns a generic `ApiError` with `status: 500, error: "INTERNAL_ERROR"`
   and no internal details — callers don't see stack traces.
