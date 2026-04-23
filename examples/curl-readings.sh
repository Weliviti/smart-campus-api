#!/usr/bin/env bash
# Copy/paste these one at a time against a running server.
BASE="http://localhost:8080/api/v1"

# List every reading for one sensor
curl -s "$BASE/sensors/TEMP-001/readings"

# Add a reading (updates the sensor's currentValue too)
curl -s -X POST "$BASE/sensors/TEMP-001/readings" \
     -H "Content-Type: application/json" \
     -d '{"value": 22.4}'

# Add another reading
curl -s -X POST "$BASE/sensors/TEMP-001/readings" \
     -H "Content-Type: application/json" \
     -d '{"value": 23.1}'

# Check the sensor to see the updated currentValue
curl -s "$BASE/sensors/TEMP-001"

# List all readings again — should now include the two you added
curl -s "$BASE/sensors/TEMP-001/readings"
