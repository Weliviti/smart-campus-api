# Architecture

The Smart Campus API is a single-process Java application that embeds a
Grizzly 2 HTTP server and hosts a JAX-RS (Jersey 2.41) application at
`/api/v1`.

## Layering

```
 ┌────────────────────────┐
 │   HTTP (Grizzly 2)     │   ← embedded, bound to 0.0.0.0:8080
 └──────────┬─────────────┘
            │
 ┌──────────▼─────────────┐
 │  Jersey / JAX-RS       │
 │  ─ filters             │   LoggingFilter (request + response)
 │  ─ resources           │   Discovery, SensorRoom, SensorResource,
 │                        │   SensorReadingResource (sub-resource)
 │  ─ mappers             │   RoomNotEmpty, LinkedResourceNotFound,
 │                        │   SensorUnavailable, GlobalException
 └──────────┬─────────────┘
            │
 ┌──────────▼─────────────┐
 │  DataStore (singleton) │   ConcurrentHashMap per collection,
 │                        │   synchronized composite operations
 └────────────────────────┘
```

## Request lifecycle

1. Grizzly accepts the socket.
2. Jersey selects a matching resource method (path + method + media type).
3. `LoggingFilter.filter(ContainerRequestContext)` logs `>> METHOD URI`.
4. The resource method runs, delegating state changes to `DataStore`.
5. Any exception bubbles up and is caught by the matching
   `ExceptionMapper<T>`, which builds a `Response` carrying an `ApiError`
   body.
6. `LoggingFilter.filter(req, res)` logs `<< METHOD URI -> status`.
7. Jersey serialises the response entity to JSON (Jackson) and writes it.

## State

`DataStore` is the only mutable state and is a singleton
(`DataStore.get()`). It holds three maps:

- `rooms:     Map<String, Room>`
- `sensors:   Map<String, Sensor>`
- `readings:  Map<String, List<SensorReading>>` keyed by sensor id

All reads use the concurrent map directly. Composite operations (e.g.
"add sensor and back-reference it from its room") are wrapped in a
`synchronized (this)` block inside `DataStore`, so callers never see a
half-applied change.

## Why a sub-resource locator?

The `/sensors/{sensorId}/readings` path is a real sub-resource locator
(method returns an object instance rather than a response). This lets the
reading resource capture its parent `sensorId` in the constructor, so
downstream methods don't have to re-thread it through every handler and
can enforce the "sensor exists + is available" invariant in one place.
