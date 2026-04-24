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

**Question 1 (Part 1.1):** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created for every request, or is it a singleton? How does this affect the way you manage in-memory data?

**Answer:** By default, JAX-RS creates a new instance of the resource class for every incoming HTTP request, meaning any data stored inside the class is lost after the response is sent. To keep rooms, sensors, and readings alive across requests, all data is stored in a single shared `DataStore` singleton. Since multiple requests can arrive at the same time, `ConcurrentHashMap` is used for safe concurrent reads and writes, and `synchronized` blocks protect operations that update more than one map at once.

---

**Question 2 (Part 1.2):** Why is HATEOAS considered a hallmark of advanced REST design? How does it benefit clients compared to static documentation?

**Answer:** HATEOAS means the server includes navigation links directly inside its responses, so clients do not need to hard-code any URLs. If a URL changes, clients automatically receive the updated link in the next response — no code change is needed on their side. Static documentation goes out of date; HATEOAS always reflects what the server currently supports.

---

**Question 3 (Part 2.1):** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects?

**Answer:** Returning full room objects gives the client everything it needs in a single request — name, capacity, and sensor list — which is ideal for a dashboard. The trade-off is higher bandwidth usage, since the response grows with every room. Returning only IDs keeps the response small, but the client then needs to make a separate request for each room it wants details on, which can cause a large number of follow-up requests if all rooms are needed.

---

**Question 4 (Part 2.2):** Is the DELETE operation idempotent in your implementation? What happens if the same DELETE request is sent multiple times?

**Answer:** Yes, DELETE is idempotent. The first call removes the room and returns 204 No Content. Every subsequent call returns 404 Not Found because the room is already gone. The response code changes, but the server state is the same after each call — the room does not exist — which is exactly what idempotency requires.

---

**Question 5 (Part 3.1):** What happens if a client sends data to POST /sensors with a Content-Type of `text/plain` or `application/xml` instead of `application/json`?

**Answer:** The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS to only accept requests with a `Content-Type` of `application/json`. If a different content type is sent, JAX-RS rejects the request before it even reaches the resource method and automatically returns HTTP 415 Unsupported Media Type. This guarantees the method only ever runs with a properly parsed JSON body.

---

**Question 6 (Part 3.2):** Why is using `@QueryParam` for filtering (e.g., `?type=CO2`) better than putting the filter in the URL path (e.g., `/sensors/type/CO2`)?

**Answer:** A URL path represents a resource hierarchy, so `/sensors/type/CO2` incorrectly implies that CO2 is a resource nested inside "type". A query parameter makes it clear that we are filtering the same `/sensors` collection, not navigating somewhere new. Query parameters are also optional by design — clients who want all sensors simply omit the parameter — and they combine naturally, for example `?type=CO2&status=ACTIVE`.

---

**Question 7 (Part 4.1):** What are the architectural benefits of the Sub-Resource Locator pattern compared to putting all nested paths in one large controller class?

**Answer:** The sub-resource locator pattern delegates nested paths to separate classes, so `SensorResource` handles sensors and `SensorReadingResource` handles readings — each class has one clear responsibility. The sensor ID is passed once through the constructor, so all reading methods can use it without re-reading the URL. The locator also validates that the sensor exists before the sub-resource is created, meaning the reading methods do not need to repeat that check.

---

**Question 8 (Part 5.2):** Why is HTTP 422 more semantically accurate than HTTP 404 when a client sends a valid JSON body referencing a room ID that does not exist?

**Answer:** HTTP 404 means the URL itself was not found, but the URL `/api/v1/sensors` is a valid endpoint — the problem is not the URL. The JSON body is also syntactically correct; the issue is that one field inside it (`roomId`) references a room that does not exist. HTTP 422 Unprocessable Entity is the correct response because it tells the client their URL and JSON are both fine, but the data inside the body is invalid.

---

**Question 9 (Part 5.4):** From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?

**Answer:** A stack trace reveals the exact versions of libraries being used (Jersey, Jackson, Tomcat), which an attacker can cross-reference against known security vulnerabilities. It also exposes internal class and package names, file paths, and line numbers, giving attackers a detailed map of the application's structure. Our `GlobalExceptionMapper` prevents this by logging the full trace on the server only and returning a generic JSON error message to the client.

---

**Question 10 (Part 5.5):** Why is it better to use JAX-RS filters for logging rather than adding `Logger.info()` calls inside every resource method?

**Answer:** Adding logging inside every resource method duplicates the same code across many files, and any developer who adds a new endpoint tomorrow could easily forget to include it. A JAX-RS filter runs automatically on every single request and response, regardless of which endpoint is called, so logging is guaranteed for the entire API in one place. It also captures requests that fail routing and never reach a resource method at all.


---

## 5. Video Demonstration

The Postman walkthrough of the endpoints (required by the brief) is attached
on the Blackboard submission link.

## 6. Licence

Academic submission — University of Westminster 2025/26.
