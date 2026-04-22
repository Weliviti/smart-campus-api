# Deployment

The service is a single self-contained fat JAR. Nothing to provision
beyond a JRE (or the Docker image). Three realistic deployment shapes:

## 1. A plain JVM on a VM

```bash
scp target/smart-campus-api.jar deploy@api-host:/srv/smart-campus/
ssh deploy@api-host
sudo systemctl restart smart-campus-api
```

Systemd unit (`/etc/systemd/system/smart-campus-api.service`):

```ini
[Unit]
Description=Smart Campus API
After=network-online.target

[Service]
Type=simple
User=deploy
WorkingDirectory=/srv/smart-campus
Environment=JAVA_OPTS=-Xms128m -Xmx512m -Dserver.port=8080
ExecStart=/usr/bin/java $JAVA_OPTS -jar /srv/smart-campus/smart-campus-api.jar
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
```

Logs go to `journalctl -u smart-campus-api`.

## 2. Docker / Compose

See [docker.md](docker.md). The image is ~220 MB, boots in <2 s on a
modern host.

## 3. Kubernetes (sketch)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: smart-campus-api
spec:
  replicas: 2
  selector:
    matchLabels: { app: smart-campus-api }
  template:
    metadata:
      labels: { app: smart-campus-api }
    spec:
      containers:
        - name: api
          image: smart-campus-api:local
          ports: [{ containerPort: 8080 }]
          readinessProbe:
            httpGet: { path: /api/v1/, port: 8080 }
            periodSeconds: 5
          livenessProbe:
            httpGet: { path: /api/v1/, port: 8080 }
            periodSeconds: 15
          resources:
            requests: { cpu: "100m", memory: "128Mi" }
            limits:   { cpu: "500m", memory: "512Mi" }
```

> ⚠️ **Caveat:** the API is stateful (in-memory `DataStore`). Running >1
> replica means each pod has its own state. Before going multi-replica,
> swap `DataStore` for a shared backend (Postgres, Redis, etc.) — see
> [architecture.md](architecture.md) for where the substitution should
> happen.

## Configuration knobs

| Setting       | System property      | Default   |
|---------------|----------------------|-----------|
| Listen host   | `server.host`        | `0.0.0.0` |
| Listen port   | `server.port`        | `8080`    |

Example override:

```bash
java -Dserver.host=127.0.0.1 -Dserver.port=9090 -jar smart-campus-api.jar
```
