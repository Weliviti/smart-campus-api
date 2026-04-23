#!/usr/bin/env bash
# Copy/paste these one at a time against a running server.
# Default base URL. Change if you started the API on a different port.
BASE="http://localhost:8080/api/v1"

# List every room
curl -s "$BASE/rooms"

# Get one room by its id
curl -s "$BASE/rooms/LIB-301"

# Create a new room
curl -s -X POST "$BASE/rooms" \
     -H "Content-Type: application/json" \
     -d '{"id":"MATH-201","name":"Maths Lecture 201","capacity":120}'

# Update a room (full replace)
curl -s -X PUT "$BASE/rooms/MATH-201" \
     -H "Content-Type: application/json" \
     -d '{"id":"MATH-201","name":"Maths Lecture 201 (renamed)","capacity":140}'

# Delete a room (must have no sensors attached, else 409)
curl -s -X DELETE "$BASE/rooms/MATH-201"
