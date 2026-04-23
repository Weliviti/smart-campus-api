# Glossary

Short definitions for the terms you'll see in the code and the API.

**Room** — a physical space on campus (e.g. a library, a lab). Has an id,
a name, a capacity, and a list of the sensors inside it.

**Sensor** — a piece of hardware inside a room (temperature, CO2,
occupancy, etc). Has an id, a type, a roomId, a status, and its latest
currentValue.

**Reading** — a single measurement a sensor produces at a point in time.
Has an id (a UUID), a timestamp (epoch milliseconds), and a numeric
value.

**Status** — a sensor's state. One of:
- `ACTIVE` — working normally, accepts readings.
- `MAINTENANCE` — disabled for service, rejects readings.
- `OFFLINE` — unreachable, rejects readings.

**DataStore** — the in-memory singleton that holds all rooms, sensors,
and readings for the process lifetime. There is no database.

**Resource** — a JAX-RS class that handles requests for one URL prefix
(e.g. `SensorRoom` handles `/rooms`).

**Sub-resource locator** — a JAX-RS pattern where one resource method
returns another resource object that handles the next path segment.
`/sensors/{id}/readings` is served this way.

**Exception mapper** — a class that converts a thrown exception into an
HTTP response. Used here so domain exceptions such as
`RoomNotEmptyException` become 409 responses automatically.

**HATEOAS** — "Hypermedia As The Engine Of Application State". In this
API it just means the discovery endpoint at `/` returns links to the
top-level collections, so a client does not have to hard-code URLs.
