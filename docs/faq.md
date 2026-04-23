# FAQ

## Why Jersey 2 and not 3?

Jersey 3 moved to the `jakarta.*` package namespace. Jersey 2 still uses
`javax.*`. The coursework brief uses `javax.*` imports, so staying on
Jersey 2.41 keeps the code matching the brief exactly.

## Why Grizzly and not Tomcat or Jetty?

Grizzly has the smallest embedded footprint of the three and is the
server Jersey's own quick-start templates use. It is also the easiest
to boot from plain `main()` code without any XML configuration.

## Why is the data store in memory?

The brief does not require persistence. Adding a database would mean
extra dependencies, migrations, and test plumbing without showing any
more REST-side skills than the current design.

## Where is the Application Assembly Descriptor or web.xml?

There isn't one. `SmartCampusApplication` subclasses
`javax.ws.rs.core.Application` and uses `@ApplicationPath` plus an
override of `getClasses()` to register components. That is the
annotation-driven equivalent.

## Why do exceptions carry contextual fields?

Each domain exception (`RoomNotEmptyException`, `LinkedResourceNotFoundException`,
`SensorUnavailableException`) carries the ids involved in the failure
so the matching `ExceptionMapper` can include them in the response body.
This makes errors much easier to debug on the client side.

## Why 422 instead of 404 for an unknown roomId?

The `/sensors` path is fine. The referenced `roomId` is what is wrong.
`422 Unprocessable Entity` is the standard code for a syntactically
valid request whose content fails a business rule.

## Why 403 instead of 409 for a sensor in maintenance?

`409 Conflict` usually means "you can fix this by changing your request".
A caller cannot fix a sensor being in `MAINTENANCE` by retrying — it is
a permission-style block. `403 Forbidden` expresses that better.

## Can two requests stomp on each other?

Reads are safe because the store is a `ConcurrentHashMap`. Multi-step
operations (add a sensor and back-reference it from its room) are
protected with a `synchronized` block so they are atomic.

## Is there authentication?

No. The brief does not require it and adding it would only add noise.
See `SECURITY.md` for caveats.
