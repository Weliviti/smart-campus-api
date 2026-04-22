# Running in Docker

The project ships with a two-stage `Dockerfile` so nobody has to install
Maven or match a JDK version to run it.

## Build the image

```bash
docker build -t smart-campus-api:local .
```

The build stage uses `maven:3.9-eclipse-temurin-11` to produce the fat JAR,
and the runtime stage starts from `eclipse-temurin:11-jre` — about 220 MB
total, vs ~650 MB if we'd kept Maven around.

## Run

```bash
docker run --rm -p 8080:8080 smart-campus-api:local
```

Visit <http://localhost:8080/api/v1/> — you should see the HATEOAS
discovery document.

Override the port:

```bash
docker run --rm -p 9090:9090 \
    -e JAVA_OPTS="-Dserver.port=9090" \
    smart-campus-api:local
```

## docker-compose

For hands-off local dev:

```bash
docker compose up --build
```

The compose file includes a `wget`-based healthcheck that probes
`/api/v1/`, so `docker compose ps` will show `(healthy)` once the
discovery endpoint starts answering.

## Stop and clean up

```bash
docker compose down
docker image rm smart-campus-api:local      # optional
```

## Why `sh -c` in the entrypoint?

The entrypoint is `sh -c "java $JAVA_OPTS -jar /app/smart-campus-api.jar"`
rather than the pure JSON form. The shell form lets you inject extra JVM
flags via the `JAVA_OPTS` env var at runtime, without having to rebuild
the image. Pure JSON form would treat `$JAVA_OPTS` as a literal string.
