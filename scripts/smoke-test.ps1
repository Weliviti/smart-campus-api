# End-to-end smoke test against a running Smart Campus API.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
#   powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1 -BaseUrl http://localhost:9090/api/v1

[CmdletBinding()]
param(
    [string]$BaseUrl = "http://localhost:8080/api/v1"
)

$ErrorActionPreference = 'Stop'
$script:Passed = 0
$script:Failed = 0

function It($name, $scriptblock) {
    try {
        & $scriptblock
        Write-Host ("  OK   {0}" -f $name) -ForegroundColor Green
        $script:Passed++
    } catch {
        Write-Host ("  FAIL {0} :: {1}" -f $name, $_.Exception.Message) -ForegroundColor Red
        $script:Failed++
    }
}

function Invoke-Api($method, $path, $body) {
    $uri = "$BaseUrl$path"
    if ($body) {
        return Invoke-RestMethod -Method $method -Uri $uri -ContentType 'application/json' `
                                 -Body ($body | ConvertTo-Json -Compress) -SkipHttpErrorCheck
    } else {
        return Invoke-RestMethod -Method $method -Uri $uri -SkipHttpErrorCheck
    }
}

Write-Host "==> Smoke testing $BaseUrl" -ForegroundColor Cyan

It 'GET /            (discovery returns links)' {
    $r = Invoke-Api 'GET' '/'
    if (-not $r.rooms -or -not $r.sensors) { throw 'missing rooms/sensors link' }
}

It 'GET /rooms       (seeded rooms present)' {
    $r = Invoke-Api 'GET' '/rooms'
    if ($r.Count -lt 2) { throw "expected >=2 rooms, got $($r.Count)" }
}

It 'POST /rooms      (create + 201)' {
    $r = Invoke-WebRequest "$BaseUrl/rooms" -Method POST -ContentType 'application/json' `
                           -Body '{"id":"SMOKE-R1","name":"Smoke test room","capacity":5}' `
                           -SkipHttpErrorCheck
    if ($r.StatusCode -ne 201) { throw "expected 201, got $($r.StatusCode)" }
}

It 'DELETE /rooms/SMOKE-R1 (clean up + 204)' {
    $r = Invoke-WebRequest "$BaseUrl/rooms/SMOKE-R1" -Method DELETE -SkipHttpErrorCheck
    if ($r.StatusCode -ne 204) { throw "expected 204, got $($r.StatusCode)" }
}

It 'POST /sensors with unknown roomId -> 422' {
    $r = Invoke-WebRequest "$BaseUrl/sensors" -Method POST -ContentType 'application/json' `
                           -Body '{"id":"SMOKE-S1","type":"Temperature","roomId":"NO-SUCH-ROOM"}' `
                           -SkipHttpErrorCheck
    if ($r.StatusCode -ne 422) { throw "expected 422, got $($r.StatusCode)" }
}

Write-Host ""
Write-Host ("==> Passed: {0}   Failed: {1}" -f $script:Passed, $script:Failed) `
    -ForegroundColor (if ($script:Failed -eq 0) { 'Green' } else { 'Red' })
if ($script:Failed -ne 0) { exit 1 }
