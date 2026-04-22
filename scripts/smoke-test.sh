#!/usr/bin/env bash
# End-to-end smoke test against a running Smart Campus API.
#
# Usage:
#   ./scripts/smoke-test.sh
#   BASE_URL=http://localhost:9090/api/v1 ./scripts/smoke-test.sh
set -u

BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"
PASSED=0
FAILED=0

it() {
    local name="$1"; shift
    if "$@" >/dev/null 2>&1; then
        echo "  OK   $name"
        PASSED=$((PASSED + 1))
    else
        echo "  FAIL $name"
        FAILED=$((FAILED + 1))
    fi
}

http_status() {
    curl -s -o /dev/null -w '%{http_code}' "$@"
}

echo "==> Smoke testing $BASE_URL"

it "GET /            (200)" bash -c "[[ \$(http_status -X GET '$BASE_URL/') = '200' ]]"
it "GET /rooms       (200)" bash -c "[[ \$(http_status -X GET '$BASE_URL/rooms') = '200' ]]"

it "POST /rooms      (201)" bash -c "[[ \$(http_status -X POST -H 'Content-Type: application/json' \
    -d '{\"id\":\"SMOKE-R1\",\"name\":\"Smoke test room\",\"capacity\":5}' \
    '$BASE_URL/rooms') = '201' ]]"

it "DELETE /rooms/SMOKE-R1 (204)" bash -c "[[ \$(http_status -X DELETE '$BASE_URL/rooms/SMOKE-R1') = '204' ]]"

it "POST /sensors bad roomId (422)" bash -c "[[ \$(http_status -X POST -H 'Content-Type: application/json' \
    -d '{\"id\":\"SMOKE-S1\",\"type\":\"Temperature\",\"roomId\":\"NO-SUCH-ROOM\"}' \
    '$BASE_URL/sensors') = '422' ]]"

echo ""
echo "==> Passed: $PASSED   Failed: $FAILED"
[[ $FAILED -eq 0 ]] || exit 1
