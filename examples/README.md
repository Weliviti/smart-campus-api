# Examples

Plain curl scripts. Nothing fancy — copy, paste, run.

## Files

| File               | What it covers                                     |
|--------------------|----------------------------------------------------|
| `curl-rooms.sh`    | List, get, create, update, delete rooms            |
| `curl-sensors.sh`  | List, filter, get, create, update, delete sensors  |
| `curl-readings.sh` | Read and add sensor readings                       |
| `curl-errors.sh`   | Deliberately trigger 404, 422, 409, 415 responses  |

## How to run

Start the API first:

```bash
java -jar target/smart-campus-api.jar
```

Then in a second terminal, run whichever script you want:

```bash
bash examples/curl-rooms.sh
```

Or open the file and copy one command at a time into your own shell.
That is usually nicer while exploring, because you can see each response.

## Changing the base URL

The scripts default to `http://localhost:8080/api/v1`. Edit the `BASE`
variable at the top of any script if your server runs on a different
port.
