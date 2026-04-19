# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures (2025/26)
**Coursework:** JAX-RS RESTful Service — "Smart Campus" Sensor & Room Management API

REST API for the University of Westminster "Smart Campus" initiative, built with
**JAX-RS (Jersey 2.41)** running on an embedded **Grizzly 2** HTTP server. All
data is held in in-memory `ConcurrentHashMap` structures — no database,
no Spring, pure JAX-RS as per the brief.

---

## 1. API Overview

The API exposes three primary resource collections, all under the versioned
base path `/api/v1`:

| Method | Path                                            | Purpose                                              |
|--------|-------------------------------------------------|------------------------------------------------------|
| GET    | `/api/v1`                                       | Discovery document (HATEOAS links)                   |
| GET    | `/api/v1/rooms`                                 | List every room                                      |
| POST   | `/api/v1/rooms`                                 | Create a room                                        |
| GET    | `/api/v1/rooms/{roomId}`                        | Get a single room                                    |
| DELETE | `/api/v1/rooms/{roomId}`                        | Delete a room (blocked if it has sensors)            |
| GET    | `/api/v1/sensors`                               | List sensors — `?type=CO2` filters by type           |
| POST   | `/api/v1/sensors`                               | Register a sensor (roomId must exist)                |
| GET    | `/api/v1/sensors/{sensorId}`                    | Get a single sensor                                  |
| DELETE | `/api/v1/sensors/{sensorId}`                    | Delete a sensor                                      |
| GET    | `/api/v1/sensors/{sensorId}/readings`           | History of readings for one sensor                   |
| POST   | `/api/v1/sensors/{sensorId}/readings`           | Append a reading (also updates `sensor.currentValue`)|

### HTTP error codes used

| Code | Scenario                                                                              |
|------|---------------------------------------------------------------------------------------|
| 400  | Malformed request body / missing required field                                       |
| 403  | POST reading against a sensor whose status is `MAINTENANCE` or `OFFLINE`              |
| 404  | Room or sensor id not found                                                           |
| 409  | Attempting to DELETE a room that still has sensors assigned                           |
| 415  | Request body sent as a content type other than `application/json`                     |
| 422  | POST sensor with a `roomId` that doesn't exist (valid JSON, invalid reference)        |
| 500  | Uncaught server error — stack trace never leaks; a generic JSON body is returned      |

---

## 2. Build & Run

### Prerequisites

- **JDK 11+** (`java -version`)
- **Maven 3.6+** (`mvn -version`)

On Windows, install Maven via Chocolatey (`choco install maven`) or download
the binary zip from <https://maven.apache.org/download.cgi> and add its `bin/`
to your `PATH`.

### Build the fat JAR

```bash
mvn clean package
```

This produces `target/smart-campus-api.jar` — a single runnable JAR containing
all Jersey + Grizzly + Jackson dependencies.

### Run the server

```bash
java -jar target/smart-campus-api.jar
```

The server listens on `http://localhost:8080/api/v1` by default. Override with
system properties:

```bash
java -Dserver.port=9090 -jar target/smart-campus-api.jar
```

### Smoke test

```bash
curl -s http://localhost:8080/api/v1 | jq .
```

You should see the discovery document with links to `rooms` and `sensors`.

---

## 3. Sample `curl` Commands

All examples assume the server is running on `localhost:8080`.

### 3.1 Discovery (HATEOAS)

```bash
curl -s http://localhost:8080/api/v1
```

### 3.2 Create a room

```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"ENG-204","name":"Engineering Lecture Hall","capacity":120}'
```

### 3.3 List rooms

```bash
curl -s http://localhost:8080/api/v1/rooms
```

### 3.4 Register a sensor in that room

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"CO2-ENG-1","type":"CO2","status":"ACTIVE","roomId":"ENG-204"}'
```

### 3.5 Filter sensors by type

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 3.6 Post a new reading (updates `sensor.currentValue`)

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/CO2-ENG-1/readings \
     -H "Content-Type: application/json" \
     -d '{"value":712.4}'
```

### 3.7 Fetch the full reading history for a sensor

```bash
curl -s http://localhost:8080/api/v1/sensors/CO2-ENG-1/readings
```

### 3.8 Trigger the 409 safety rule

```bash
# The seeded LIB-301 has sensors attached — DELETE is blocked.
curl -s -i -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 3.9 Trigger the 422 dependency-validation mapper

```bash
# Sensor POST referencing a room that doesn't exist.
curl -s -i -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"GHOST-1","type":"CO2","status":"ACTIVE","roomId":"DOES-NOT-EXIST"}'
```

---

## 4. Conceptual Report — Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource lifecycle & in-memory state

By default, JAX-RS creates a **fresh instance of the resource class for every
incoming HTTP request** (per-request lifecycle). This keeps request handling
stateless at the class level, which is good for correctness but means that any
state stored as an instance field would vanish the moment the response is
flushed.

Because our rooms, sensors and readings have to live longer than one request,
we keep them in a **singleton** `DataStore` (`DataStore.getInstance()`). That
singleton is *shared* across all the per-request resource instances, so
concurrency has to be handled explicitly:

- `ConcurrentHashMap` for the `rooms`, `sensors` and `readingsBySensor`
  collections — gives us lock-free reads and atomic writes.
- `synchronized` on the composite operations (`addSensor`, `removeSensor`,
  `addReading`) because those touch **two** maps and a shared `Room` list
  together; without the monitor, one thread could see half an update.

This is the minimum needed to avoid race conditions like a sensor being added
to the `sensors` map but not to the parent room's `sensorIds` list, or a
reading being appended while the sensor is simultaneously being deleted.

### Part 1.2 — Why HATEOAS?

HATEOAS ("Hypermedia as the Engine of Application State") embeds navigation
links **in the response itself** rather than asking clients to hard-code
URIs. Our `GET /api/v1` returns:

```json
{
  "links": {
    "self":    "http://host/api/v1",
    "rooms":   "http://host/api/v1/rooms",
    "sensors": "http://host/api/v1/sensors"
  }
}
```

Client benefits:

- **Evolvability.** If the `rooms` path moves to `/rooms/v2`, the client just
  follows the advertised link — no code change and no re-reading static docs.
- **Discoverability.** A new developer can explore the API by calling the root
  and clicking through, instead of memorising every URL template.
- **Loose coupling.** Clients depend on *link relations* ("rooms", "self") not
  on absolute URLs, so moving the service to a different host or port is
  transparent.

Static documentation describes *what existed* at a point in time; hypermedia
describes *what is true now*, which is far more resilient as the API evolves.

### Part 2.1 — IDs vs full objects when listing rooms

Returning full `Room` objects (our choice) means one round-trip gives clients
everything they need: name, capacity, and the list of sensor ids. That's ideal
for a dashboard UI or facilities manager.

The trade-off:

- **Bandwidth.** A `GET /rooms` response scales with the number of rooms *and*
  the size of each room. For thousands of rooms, that payload grows quickly.
- **Client CPU / memory.** The client has to parse and retain the full object
  graph even if it only needs the ids.

Returning **only ids** would give a thin index — cheap to transfer — and the
client would make follow-up `GET /rooms/{id}` calls on demand ("lazy" fetch).
That keeps bandwidth low for the list call but risks the classic N+1 problem
if the UI always wants every room's details anyway.

For this coursework the dataset is small so full objects are fine; in a real
deployment you'd expose both (e.g. `GET /rooms?fields=id,name`) and let the
client pick.

### Part 2.2 — Is DELETE idempotent?

Yes, in the HTTP sense. Idempotency for `DELETE` means that sending the same
request N times leaves the server in the same final state as sending it once.

Our flow:

1. First `DELETE /rooms/ABC` — the room exists, we remove it, return **204 No
   Content**.
2. Second, third, Nth `DELETE /rooms/ABC` — the room is already gone, we
   return **404 Not Found**.

The *response* changes (204 → 404) but the *server state* is identical after
each call (the room is absent), which is what idempotency requires. Returning
404 on the repeat is actually helpful: the client learns the resource is gone
rather than being silently reassured it was "deleted" again.

### Part 3.1 — `@Consumes(MediaType.APPLICATION_JSON)` mismatch

`@Consumes` tells JAX-RS *"I will only bind a request body to my parameter if
the `Content-Type` matches"*. If a client sends a `POST /sensors` with a body
typed `text/plain` or `application/xml`:

- JAX-RS inspects the `Content-Type` header, fails to find a
  `MessageBodyReader` for that type on a method that only declares JSON, and
  never invokes our resource method.
- The client is returned **HTTP 415 Unsupported Media Type** automatically.

This is a strong contract: the resource method is guaranteed to only ever run
with a parsed, JSON-deserialised `Sensor` object, so we don't have to write
defensive parsing code.

### Part 3.2 — `@QueryParam` vs path segments for filtering

We filter sensors via `GET /sensors?type=CO2` (a query parameter). The
alternative would be `GET /sensors/type/CO2` — a new path segment.

Why query parameters win for filtering:

1. **Resource identity stays stable.** `/sensors` *is* the collection, full
   stop. `?type=CO2` modifies *how much* of that collection we return. A path
   like `/sensors/type/CO2` implies "CO2" is a child resource of "type", which
   it isn't.
2. **Composable.** Query parameters are naturally combinable:
   `?type=CO2&status=ACTIVE`. Doing the same with path segments bakes a rigid
   order into every URL and explodes your routing table.
3. **Optional by design.** `?type=` can be omitted; clients that want every
   sensor just hit `/sensors`. A required path segment forces a value — a
   wildcard would leak through the URL.
4. **Caching.** HTTP caches already key on the full URL including query
   string, so cacheability is identical.
5. **Convention.** Every mature REST API on the web (GitHub, Stripe, Spotify)
   uses query strings for filtering/searching — clients and tooling expect it.

Path segments are the right choice for *hierarchy* (`/rooms/{id}/sensors`);
query parameters are the right choice for *filtering the same collection*.

### Part 4.1 — Sub-resource locator benefits

A sub-resource locator is a method that returns **another resource object**
instead of a `Response`:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
    if (!store.sensorExists(sensorId)) {
        throw new NotFoundException(...);
    }
    return new SensorReadingResource(sensorId);
}
```

Why this beats a single god-class with every nested path hard-coded:

- **Separation of concerns.** `SensorResource` knows about sensors;
  `SensorReadingResource` knows about readings. Neither has to understand the
  other's storage or validation rules.
- **Context travels with the instance.** The constructor captures `sensorId`
  once; every method on the sub-resource can use it without re-parsing path
  parameters or re-fetching the parent.
- **Pre-condition checks happen once.** The locator verifies the sensor exists
  before the sub-resource ever runs. The reading methods don't have to repeat
  that check.
- **Testability.** `SensorReadingResource` can be instantiated directly in a
  unit test with a fake sensor id — no need to spin up the whole routing layer.
- **Scale.** Imagine 15 entities each with 3 nested collections. A
  sub-resource per nested collection keeps each class small; a monolithic
  controller would become a multi-thousand-line switch statement.

### Part 5.2 — 422 vs 404 for a missing linked reference

HTTP 404 means *"the URL you requested does not identify a resource on this
server"*. When a client POSTs:

```json
{ "id":"TEMP-9", "type":"Temperature", "roomId":"DOES-NOT-EXIST" }
```

to `/api/v1/sensors`, the URL `/sensors` **is** a valid endpoint — it exists
and accepts POSTs. A 404 would be semantically wrong because the request
target was fine.

The actual problem is *inside* the request body: the JSON is syntactically
valid (`@Consumes` accepted it, Jackson parsed it), but one of its fields
references a resource the server can't find. **RFC 4918 §11.2** defines
**HTTP 422 Unprocessable Entity** for exactly this case: "the server
understands the content type, the syntax is correct, but it was unable to
process the contained instructions". That maps precisely to "you sent me a
Sensor pointing at a non-existent Room".

Using 422 tells the client "your URL and your JSON are both fine — it's the
*data* that's wrong". Using 404 would mislead them into debugging their URL
instead of their payload.

### Part 5.4 — Why leaking stack traces is dangerous

An uncaught Java stack trace embedded in a response body is a small
intelligence gift to an attacker:

- **Library inventory.** Exact versions of Jersey, Grizzly, Jackson, JDK, etc.
  become visible — feeding straight into public CVE lookups.
- **Package / class layout.** Internal package names
  (`com.smartcampus.store.DataStore`) reveal the domain model and hint at what
  other endpoints likely exist.
- **Absolute file paths.** Traces include source files and line numbers
  (`DataStore.java:87`), which can leak deployment paths or OS hints.
- **Code-logic fingerprints.** An `NPE` at a particular call site tells the
  attacker exactly which user-supplied input reached which unchecked code
  path — prime material for crafting a targeted fuzz payload.
- **Injection probes.** Attackers deliberately send malformed input hoping to
  trigger traces; each leaked trace is essentially a free debugging session
  against the production server.

Our `GlobalExceptionMapper<Throwable>` logs the full trace **server-side**
(for our observability) and returns a generic `INTERNAL_ERROR` JSON body to
the client. The attacker gets nothing; we get our forensic data.

### Part 5.5 — Why JAX-RS filters instead of `Logger.info()` in every method

Logging is a **cross-cutting concern** — every endpoint needs it, but logging
code has nothing to do with a handler's business logic. Inlining
`Logger.info()` calls in every resource method would:

1. **Duplicate the same boilerplate** in 10+ methods. Change the log format
   and you edit 10 files.
2. **Be forgettable.** A new endpoint added tomorrow won't have logging unless
   the developer remembers to add it.
3. **Not cover framework errors.** A request that fails routing never reaches
   a resource method, so it never gets logged — but a filter does see it.
4. **Mix concerns.** Mixing persistence, validation, and logging in the same
   method hurts readability and testability.

A `ContainerRequestFilter` + `ContainerResponseFilter` runs once per request
in the JAX-RS pipeline regardless of which endpoint matched, captures the
method / URI / status, and stays completely decoupled from the handlers.
Add a new endpoint tomorrow — it's logged automatically. Change the format —
you edit one class.

---

## 5. Video Demonstration

The Postman walkthrough of the endpoints (required by the brief) is attached
on the Blackboard submission link.

## 6. Licence

Academic submission — University of Westminster 2025/26.
