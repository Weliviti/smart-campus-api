# Troubleshooting

## "Address already in use" on startup

Something is already bound to `:8080`. Use a different port:

```powershell
java -Dserver.port=9090 -jar target/smart-campus-api.jar
```

…or kill whatever has the port:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1
```

## 415 Unsupported Media Type on POST

You forgot the `Content-Type` header. Add:

```
-H 'Content-Type: application/json'
```

…to your curl, or pick the `raw JSON` body type in Postman.

## 422 on POST /sensors

The `roomId` you used doesn't exist. List the real ones:

```bash
curl -s http://localhost:8080/api/v1/rooms | jq '.[].id'
```

## 409 on DELETE /rooms/{id}

The room still has sensors. Detach or delete them first:

```bash
curl -s http://localhost:8080/api/v1/rooms/LIB-301 | jq '.sensorIds'

# for each id in that list:
curl -s -X DELETE http://localhost:8080/api/v1/sensors/TEMP-001
```

Then retry the room delete.

## 403 on POST reading

The sensor's `status` is `MAINTENANCE` or `OFFLINE`. Either pick a different
sensor, or PUT it back to `ACTIVE`:

```bash
curl -s -X PUT http://localhost:8080/api/v1/sensors/TEMP-001 \
     -H 'Content-Type: application/json' \
     -d '{"id":"TEMP-001","type":"Temperature","roomId":"LIB-301","status":"ACTIVE"}'
```

## Jackson deserialisation error

Usually caused by a typo in JSON keys. Check:

- all strings are double-quoted (not single quotes);
- no trailing commas;
- `capacity` is a number, not a string.

## JDK mismatch at runtime

```
UnsupportedClassVersionError: ... has been compiled by a more recent version
```

The JAR was built against JDK 11 (or 17 if you rebuilt it on 17). Run it
on at least that version:

```bash
java -version
```

If you need to downgrade, set `maven.compiler.source/target` in `pom.xml`
to 11 and rebuild.

## Where are the logs?

`java.util.logging` writes to stderr by default. The format is configured in
`src/main/resources/logging.properties`. To pipe everything to a file:

```bash
java -jar target/smart-campus-api.jar 2> smart-campus.log
```
