# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures (2025/26)
**Coursework:** JAX-RS RESTful Service — "Smart Campus" Sensor & Room Management API

REST API for the University of Westminster "Smart Campus" initiative, built with
**JAX-RS (Jersey 2.41)** running on an embedded **Apache Tomcat 9** servlet
container. All data is held in in-memory `ConcurrentHashMap` structures — no
database, no Spring, pure JAX-RS as per the brief.

A simple companion Java console client
(`com.smartcampus.client.SmartCampusClient`) is also included so the API can be
demonstrated end-to-end without external tools.

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
all Jersey + embedded Tomcat + Jackson dependencies.

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

### Run the console client

With the server running in one terminal, open a second and run:

```bash
java -cp target/smart-campus-api.jar com.smartcampus.client.SmartCampusClient
```

This brings up a numbered menu that exercises every endpoint (rooms, sensors,
readings, discovery). Point it at a different server with a positional URL
argument: `... SmartCampusClient http://otherhost:9090/api/v1`.

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

### 3.10 Trigger the 403 state-constraint mapper

```bash
# 1. Create a sensor in MAINTENANCE status.
curl -s -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"MAINT-001","type":"Temperature","status":"MAINTENANCE","roomId":"LIB-301"}'

# 2. Attempt to post a reading to it — returns 403 Forbidden.
curl -s -i -X POST http://localhost:8080/api/v1/sensors/MAINT-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":22.5}'
```

### 3.11 Trigger the 500 global safety-net mapper

```bash
# Forces an unhandled RuntimeException — proves no stack trace leaks to the client.
curl -s http://localhost:8080/api/v1/test-error
```

---

## 4. Conceptual Report — Answers to Coursework Questions

---

### Part 1.1

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created for every request, or is it a singleton? How does this affect the way you manage in-memory data?

**Answer:**

By default, JAX-RS creates a brand new instance of the resource class for every single HTTP request that comes in. This means any data stored directly inside the resource class would be lost as soon as the response is sent back.

Because rooms, sensors, and readings need to survive across many requests, we store all data in a separate singleton class called `DataStore`. There is only ever one `DataStore` in the whole application, and every resource instance shares it.

Since multiple requests can arrive at the same time (from different users), we need to make sure two requests do not corrupt the data at the same time. We handle this in two ways:

- We use `ConcurrentHashMap` for storing rooms, sensors, and readings. This allows multiple threads to read and write safely without locking each other out for simple operations.
- For more complex operations that touch more than one map at once (for example, adding a sensor updates both the sensors map and the room's sensor list), we use `synchronized` blocks to ensure only one thread runs that code at a time.

Without these precautions, one request could see half-finished data written by another request running at the same time.

---

### Part 1.2

**Question:** Why is HATEOAS considered a hallmark of advanced REST design? How does it benefit clients compared to static documentation?

**Answer:**

HATEOAS stands for "Hypermedia as the Engine of Application State." The idea is simple: instead of forcing the client to already know every URL in the API, the server includes those URLs directly in its responses.

For example, when a client calls `GET /api/v1`, the response includes:

```json
{
  "links": {
    "rooms":   "http://localhost:8080/api/v1/rooms",
    "sensors": "http://localhost:8080/api/v1/sensors"
  }
}
```

The client reads the link for "rooms" from the response rather than having it hard-coded in the application.

This has several benefits over static documentation:

- **If URLs change**, clients automatically get the new URL from the next response. No code change is needed on the client side.
- **New developers** can explore the API just by following links, the same way you browse a website.
- **The client is not tied** to a specific host or port. If the server moves, the links update automatically.

Static documentation is always a snapshot of the past. HATEOAS always reflects what the server currently supports.

---

### Part 2.1

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects?

**Answer:**

This API returns the full room object for every room in the list. This means in a single request, the client gets the id, name, capacity, and sensor list for every room. This is convenient — a dashboard or management tool can display everything without making any extra requests.

However, there is a trade-off:

- **Returning full objects** uses more bandwidth. If there are thousands of rooms, the response becomes very large. The client also has to process and store all of that data even if it only needed the IDs.

- **Returning only IDs** keeps the list response small and fast. The client then makes separate requests like `GET /rooms/LIB-301` only for the rooms it actually needs. This is called lazy loading. The downside is that if the client ends up needing every room's details anyway, it now has to make hundreds of extra requests — this is known as the N+1 problem.

For this project, the dataset is small so returning full objects is the right choice. In a large production system, a better approach would be to support both: return full objects by default, but allow clients to request `?fields=id,name` if they only need certain fields.

---

### Part 2.2

**Question:** Is the DELETE operation idempotent in your implementation? What happens if the same DELETE request is sent multiple times?

**Answer:**

Yes, DELETE is idempotent in this implementation.

Idempotent means: no matter how many times you send the same request, the final state of the server is the same as if you had sent it once.

Here is what happens step by step:

1. **First DELETE** on a room that exists — the room is removed and the server responds with **204 No Content**.
2. **Second DELETE** on the same room — the room is already gone, so the server responds with **404 Not Found**.
3. **Any further DELETE** — same 404 response.

The response code changes from 204 to 404, but the server state is identical each time: the room does not exist. This satisfies the idempotency requirement.

Returning 404 on the second attempt is actually more useful than silently returning 204 again, because it tells the client clearly that the room was not there — useful for detecting bugs or duplicate requests.

---

### Part 3.1

**Question:** What happens technically if a client sends data to the POST /sensors endpoint with a Content-Type of `text/plain` or `application/xml` instead of `application/json`?

**Answer:**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that this endpoint will only accept requests where the `Content-Type` header is set to `application/json`.

If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS checks the header and immediately sees the mismatch. It does not attempt to call the resource method at all. Instead, it automatically returns **HTTP 415 Unsupported Media Type** to the client.

This is a strong guarantee: the resource method is only ever called when the body has already been successfully parsed as JSON. There is no need to write extra defensive code inside the method to handle unexpected formats.

---

### Part 3.2

**Question:** Why is using `@QueryParam` for filtering (e.g., `?type=CO2`) better than putting the filter in the URL path (e.g., `/sensors/type/CO2`)?

**Answer:**

We chose to filter sensors using a query parameter: `GET /api/v1/sensors?type=CO2`.

The alternative would have been a path like `GET /api/v1/sensors/type/CO2`, but this approach has several problems:

- **It implies the wrong meaning.** A URL path represents a hierarchy of resources. `/sensors/type/CO2` suggests that `CO2` is a specific resource that lives inside `type`, which is not true. `/sensors` is the collection; `?type=CO2` is just a filter on that collection.
- **It is not optional.** A path segment must always have a value. With a query parameter, clients who want all sensors just call `/sensors` with no parameter at all.
- **It does not compose well.** If you want to filter by both type and status, a query parameter approach gives you `?type=CO2&status=ACTIVE` naturally. With path segments, you would need to design a new route for every combination.
- **It breaks conventions.** Every well-known REST API (GitHub, Stripe, Google) uses query parameters for filtering and searching. Developers expect it, and tools like Postman are designed around it.

Query parameters are the correct tool for narrowing down a collection. Path segments are the correct tool for navigating a hierarchy.

---

### Part 4.1

**Question:** What are the architectural benefits of the Sub-Resource Locator pattern compared to putting all nested paths in one large controller class?

**Answer:**

A sub-resource locator is a method in the parent resource that, instead of returning a response directly, returns a new resource object. JAX-RS then uses that new object to handle the rest of the request.

In this project, `SensorResource` has a locator for `/{sensorId}/readings` that returns a `SensorReadingResource` instance. All reading-related logic lives in that separate class.

The benefits of this approach over one large class are:

- **Each class has one job.** `SensorResource` manages sensors. `SensorReadingResource` manages readings. Neither class needs to know the internal details of the other.
- **Validation happens in one place.** The locator checks whether the sensor exists before the sub-resource is even created. The reading methods do not need to repeat this check — they can trust the sensor is valid.
- **The sensor ID is passed once.** The sub-resource receives the sensor ID in its constructor and all of its methods can use it directly without re-reading the URL.
- **It is easier to test.** `SensorReadingResource` can be created and tested in isolation with a fake sensor ID, without needing to start the full server.
- **It scales.** In a large API with many entities and nested collections, using sub-resources keeps every class small and focused. A single controller class handling everything would quickly become unmanageable.

---

### Part 5.2

**Question:** Why is HTTP 422 more semantically accurate than HTTP 404 when a client sends a valid JSON body that references a room ID that does not exist?

**Answer:**

HTTP 404 means "the URL you requested was not found on this server." But in this case, the URL `/api/v1/sensors` is perfectly valid — it exists and accepts POST requests. Returning 404 would mislead the client into thinking they got the URL wrong, when they did not.

The real problem is inside the request body. The JSON is valid, the Content-Type is correct, and the endpoint exists — but one of the fields in the body (`roomId`) references a room that does not exist in the system.

HTTP 422 Unprocessable Entity is the correct response here. It tells the client: "Your URL is fine, your JSON is syntactically correct, but the data inside it is invalid — specifically, the room you referenced does not exist."

This gives the client accurate information. A 404 would send them debugging their URL. A 422 sends them debugging their request body, which is the actual problem.

---

### Part 5.4

**Question:** From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?

**Answer:**

Exposing a Java stack trace in an API response gives an attacker detailed information about the internal workings of the server. Specifically:

- **Library versions are revealed.** The trace shows exactly which version of Jersey, Jackson, Tomcat, and the JDK is being used. An attacker can look up known security vulnerabilities for those exact versions.
- **Internal package and class names are visible.** Names like `com.smartcampus.store.DataStore` reveal the structure of the application and hint at what other classes and endpoints exist.
- **File paths and line numbers appear.** Traces include source file names and line numbers, which can reveal how the server is deployed and what operating system it runs on.
- **Logic flaws are exposed.** A NullPointerException at a specific line tells an attacker exactly which part of the code is vulnerable and what input caused it. They can use this to craft targeted attacks.
- **Attackers probe deliberately.** Malicious users will intentionally send bad requests to trigger errors, treating each stack trace as a free debugging session against the live server.

In this project, the `GlobalExceptionMapper<Throwable>` catches all unexpected errors, logs the full stack trace on the server for the developer to read, and returns only a generic JSON error message to the client. The attacker receives no useful information.

---

### Part 5.5

**Question:** Why is it better to use JAX-RS filters for logging rather than adding `Logger.info()` calls inside every resource method?

**Answer:**

Logging is needed on every single request and response, regardless of which endpoint is called. This makes it a cross-cutting concern — it has nothing to do with the business logic of any specific endpoint.

If we added `Logger.info()` manually inside every resource method:

- **It would be repeated everywhere.** The same logging code would appear in 10 or more methods. If the log format needed to change, every file would need to be updated.
- **New endpoints would not be logged automatically.** Any developer who adds a new endpoint tomorrow would need to remember to add the logging themselves. It is easy to forget.
- **Failed routing would not be logged.** If a request arrives but does not match any resource method (for example, wrong URL), it never reaches a resource method. A filter sees every request, even ones that fail routing.
- **Business logic and logging get mixed together.** Methods that should only care about their job (creating a room, posting a reading) would be cluttered with unrelated logging code, making them harder to read and test.

A `ContainerRequestFilter` runs automatically before every request, and a `ContainerResponseFilter` runs automatically after every response. They log the method, URI, and status code once, centrally, without touching any resource class. Add a new endpoint and it is logged for free.


---

## 5. Video Demonstration

The Postman walkthrough of the endpoints (required by the brief) is attached
on the Blackboard submission link.

## 6. Licence

Academic submission — University of Westminster 2025/26.
