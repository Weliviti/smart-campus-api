# Local development

## First-time setup

1. Install JDK 11 (any distribution, Temurin is a safe pick).
2. Install Maven 3.8+.
3. Clone the repo.
4. From the repo root:

   ```powershell
   mvn -q clean package
   ```

## Import into an IDE

### IntelliJ IDEA

`File -> Open...` and pick the `pom.xml`. IntelliJ will index the
project and pull down the Maven dependencies on its own.

Set the run configuration to:
- Main class: `com.smartcampus.app.Main`
- Working directory: the repo root

### VS Code

Install the `Extension Pack for Java`. Open the repo folder. VS Code
picks up the `pom.xml` and builds with Java's Maven integration.

## Day-to-day workflow

| Task                     | Command                                  |
|--------------------------|------------------------------------------|
| Compile only             | `mvn -q compile`                         |
| Run unit tests           | `mvn -q test`                            |
| Full build + tests       | `mvn -q verify`                          |
| Skip tests               | `mvn -q package -DskipTests`             |
| Clean everything         | `mvn -q clean`                           |
| Run the server locally   | `.\scripts\run.ps1`                      |
| Stop it                  | `.\scripts\stop.ps1`                     |
| Smoke test the API       | `.\scripts\smoke-test.ps1`               |

## Port collisions

The server listens on `8080` by default. If something else already owns
that port, either stop the other process or pick a different port:

```powershell
.\scripts\run.ps1 -Port 9090
```
