# Seeded data

When the server first starts, the `DataStore` populates itself with a
small amount of data so the API is immediately usable without needing
to POST anything.

## Rooms

| id        | name                   | capacity |
|-----------|------------------------|----------|
| LIB-301   | Library Quiet Study    | 40       |
| CS-LAB-1  | Computer Science Lab 1 | 25       |

## Sensors

| id        | type        | roomId    | initial currentValue |
|-----------|-------------|-----------|----------------------|
| TEMP-001  | Temperature | LIB-301   | 21.5                 |
| CO2-001   | CO2         | LIB-301   | 480.0                |
| OCC-001   | Occupancy   | CS-LAB-1  | 12                   |

All three sensors start in `ACTIVE` status.

## Readings

None are seeded. Every sensor starts with an empty reading history.
`POST /api/v1/sensors/{id}/readings` is how you populate it.

## Resetting the seed data

There is no API for this. The data lives in memory only, so stopping and
restarting the server gives you a fresh copy:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
```
