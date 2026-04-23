#!/usr/bin/env bash
# Copy/paste these one at a time against a running server.
BASE="http://localhost:8080/api/v1"

# List every sensor
curl -s "$BASE/sensors"

# Filter by type
curl -s "$BASE/sensors?type=Temperature"

# Get one sensor by id
curl -s "$BASE/sensors/TEMP-001"

# Create a new sensor in LIB-301
curl -s -X POST "$BASE/sensors" \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-LIB-301-2","type":"Temperature","roomId":"LIB-301"}'

# Update a sensor (full replace, keeps it ACTIVE)
curl -s -X PUT "$BASE/sensors/TEMP-LIB-301-2" \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-LIB-301-2","type":"Temperature","roomId":"LIB-301","status":"ACTIVE"}'

# Delete the sensor (also removes its id from the parent room)
curl -s -X DELETE "$BASE/sensors/TEMP-LIB-301-2"
