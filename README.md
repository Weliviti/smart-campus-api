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

### Part 1: Service Architecture & Setup

**Question 1 (Part 1.1):** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created for every request, or is it a singleton? How does this affect the way you manage in-memory data?

**Answer:**

- **Per-request lifecycle:** By default, JAX-RS creates a brand new instance of the resource class for every single HTTP request. This means any data stored inside the class disappears the moment the response is sent.
- **Shared data store:** Because rooms, sensors, and readings must survive across many requests, all data is kept in a single shared class called `DataStore` (a singleton). Every resource instance reads from and writes to this one shared store.
- **Race conditions:** Since many users can send requests at the exact same time, two threads could try to update the same data simultaneously. This causes a race condition — a bug where data gets corrupted because two workers clashed.
- **How we fix it:** We use `ConcurrentHashMap` to allow safe reads and writes from multiple threads at once. For operations that touch more than one map at the same time (like adding a sensor to both the sensor list and the room's sensor list), we use `synchronized` blocks so only one thread runs that code at a time.

---

**Question 2 (Part 1.2):** Why is HATEOAS considered a hallmark of advanced REST design? How does it benefit clients compared to static documentation?

**Answer:**

- **What HATEOAS means:** HATEOAS (Hypermedia as the Engine of Application State) means the server includes clickable links inside its responses, so the client does not need to memorise or hardcode any URLs.
- **The benefit:** Instead of a developer reading a PDF document to find out what the URLs are, the API tells them directly in the response — for example, the discovery endpoint returns `"rooms": "/api/v1/rooms"`. The client just follows that link.
- **Why it beats static docs:** If a URL changes on the server side, the client automatically receives the new link in the next response — nothing breaks and no code needs to change. Static documentation always goes out of date; HATEOAS is always accurate.

---

### Part 2: Room Management

**Question 3 (Part 2.1):** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects?

**Answer:**

- **Returning full objects (our choice):** The client gets everything in one request — the room name, capacity, and sensor list. This is ideal for a dashboard that needs to display all room details at once.
- **The downside:** If there are thousands of rooms, the response becomes very large. The client has to download and process all that data even if it only needed the IDs.
- **Returning only IDs:** The response stays small and fast, but the client then has to make a separate request for every single room it wants to see — this is known as the N+1 problem and can make the app feel very slow.
- **Conclusion:** For this project the dataset is small, so returning full objects is the right choice. In a large real-world system, you would support both options.

---

**Question 4 (Part 2.2):** Is the DELETE operation idempotent in your implementation? What happens if the same DELETE request is sent multiple times?

**Answer:**

- **Idempotent:** An operation is idempotent if repeating it many times produces the same final result as doing it once.
- **First DELETE:** If the room exists, it is removed and the server returns **204 No Content**.
- **Second DELETE (and beyond):** The room is already gone, so the server returns **404 Not Found**.
- **Why it is still idempotent:** The HTTP response code changes, but the server state is identical each time — the room does not exist. That is what idempotency actually requires. The 404 is also helpful because it clearly tells the client the room is gone, rather than pretending the delete happened again.

---

### Part 3: Sensor Operations & Linking

**Question 5 (Part 3.1):** What happens if a client sends data to POST /sensors with a Content-Type of `text/plain` or `application/xml` instead of `application/json`?

**Answer:**

- **What `@Consumes` does:** The `@Consumes(MediaType.APPLICATION_JSON)` annotation acts like a bouncer at the door. It tells JAX-RS: "Only let this request through if it arrives as JSON."
- **What happens on a mismatch:** If a client sends `text/plain` or `application/xml`, JAX-RS checks the Content-Type header, sees it does not match, and blocks the request immediately — the resource method is never even called.
- **The response:** The client automatically receives **HTTP 415 Unsupported Media Type**.
- **The benefit:** We do not need to write any manual checking code inside the method. JAX-RS handles the entire validation for us, keeping the code clean.

---

**Question 6 (Part 3.2):** Why is using `@QueryParam` for filtering (e.g., `?type=CO2`) better than putting the filter in the URL path (e.g., `/sensors/type/CO2`)?

**Answer:**

- **URL Path approach:** Putting the filter in the path (e.g., `/sensors/type/CO2`) treats CO2 as if it were a specific resource that lives inside a folder called "type" — which is incorrect. URL paths are meant to represent a hierarchy of resources, not a search filter.
- **Query Parameter approach:** `?type=CO2` makes it clear we are filtering the `/sensors` collection, not navigating somewhere new.
- **It is optional:** Clients who want all sensors just call `/sensors` with no parameter. A path-based design would always require a value.
- **It is stackable:** You can combine multiple filters easily, for example `?type=CO2&status=ACTIVE`. Doing this with path segments would require a completely new URL design for every combination.

---

### Part 4: Deep Nesting with Sub-Resources

**Question 7 (Part 4.1):** What are the architectural benefits of the Sub-Resource Locator pattern compared to putting all nested paths in one large controller class?

**Answer:**

- **What it is:** A sub-resource locator is a method in the parent class that, instead of returning a response, hands the request off to a separate dedicated class. In this project, `SensorResource` passes reading requests to `SensorReadingResource`.
- **Single Responsibility:** Each class has one job. `SensorResource` manages sensors. `SensorReadingResource` manages readings. Neither needs to know the internal details of the other.
- **Validation happens once:** The locator checks that the sensor exists before creating the sub-resource. The reading methods do not need to repeat this check — they can trust the sensor is valid.
- **Avoids a monolith:** Without this pattern, one class would handle every endpoint. In a large API that grows over time, this becomes a massive, unreadable file that is very hard to maintain or debug.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Question 8 (Part 5.2):** Why is HTTP 422 more semantically accurate than HTTP 404 when a client sends a valid JSON body referencing a room ID that does not exist?

**Answer:**

- **What 404 means:** HTTP 404 means "the URL you requested does not exist on this server." But the URL `/api/v1/sensors` is perfectly valid — it exists and accepts POST requests. Using 404 here would mislead the client into thinking they typed the wrong URL.
- **The real problem:** The URL is fine. The JSON format is fine. The problem is that one field inside the body — `roomId` — points to a room that does not exist in the system.
- **Why 422 is correct:** HTTP 422 Unprocessable Entity means "your URL is valid, your JSON is valid, but the data inside cannot be processed." That is exactly what happened here.
- **The benefit:** The client is told to fix their data, not their URL — which is the correct direction to debug.

---

**Question 9 (Part 5.4):** From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?

**Answer:**

- **What a stack trace is:** It is the wall of red error text Java produces when something crashes, showing every method call that led to the error.
- **Information leakage:** Showing this to external users reveals the exact versions of every library being used (Jersey, Jackson, Tomcat, JDK). Attackers search these versions against CVEs (Common Vulnerabilities and Exposures) — a public list of known security holes — and find ready-made exploits.
- **Internal structure exposed:** The trace also shows internal class names, package names, file paths, and line numbers. This gives an attacker a detailed map of how the application is built.
- **How we prevent it:** The `GlobalExceptionMapper<Throwable>` catches every unexpected error, logs the full trace on the server side only, and returns a plain generic message to the client. The attacker sees nothing useful.

---

**Question 10 (Part 5.5):** Why is it better to use JAX-RS filters for logging rather than adding `Logger.info()` calls inside every resource method?

**Answer:**

- **The problem with manual logging:** If you add `Logger.info()` inside every resource method, the same boilerplate code is repeated across 10 or more files. If the log format ever needs to change, every file must be updated.
- **Easy to forget:** Any developer who adds a new endpoint tomorrow would need to remember to add the logging themselves. It is very easy to forget, leaving some endpoints unmonitored.
- **Requests that fail routing are missed:** If a request arrives but does not match any endpoint (wrong URL), it never reaches a resource method — so inline logging would never run. A filter sees every request, even ones that fail before reaching any method.
- **The filter solution:** A `ContainerRequestFilter` and `ContainerResponseFilter` run automatically on every request and response, regardless of which endpoint was called. Logging is guaranteed across the entire API from a single class, with zero impact on the resource methods themselves.


---

## 5. Video Demonstration

The Postman walkthrough of the endpoints (required by the brief) is attached
on the Blackboard submission link.

## 6. Licence

Academic submission — University of Westminster 2025/26.
