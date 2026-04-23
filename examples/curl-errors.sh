#!/usr/bin/env bash
# Each call below is meant to fail. These are useful for testing that the
# exception mappers return the right HTTP status codes.
BASE="http://localhost:8080/api/v1"

# 404 — ask for a room that doesn't exist
curl -s -o /dev/null -w "GET /rooms/DOES-NOT-EXIST -> %{http_code}\n" \
     "$BASE/rooms/DOES-NOT-EXIST"

# 422 — create a sensor with a roomId that doesn't exist
curl -s -o /dev/null -w "POST /sensors (bad roomId) -> %{http_code}\n" \
     -X POST "$BASE/sensors" \
     -H "Content-Type: application/json" \
     -d '{"id":"ORPHAN-1","type":"CO2","roomId":"NO-SUCH-ROOM"}'

# 409 — delete a room that still has sensors
curl -s -o /dev/null -w "DELETE /rooms/LIB-301 -> %{http_code}\n" \
     -X DELETE "$BASE/rooms/LIB-301"

# 415 — post JSON without the Content-Type header
curl -s -o /dev/null -w "POST /rooms without Content-Type -> %{http_code}\n" \
     -X POST "$BASE/rooms" \
     -d '{"id":"X","name":"X","capacity":1}'
