# Stop any java process that is listening on the Smart Campus API port.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1
#   powershell -ExecutionPolicy Bypass -File .\scripts\stop.ps1 -Port 9090

[CmdletBinding()]
param(
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'

$owners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if (-not $owners) {
    Write-Host ("Nothing listening on port {0}." -f $Port) -ForegroundColor Yellow
    exit 0
}

foreach ($c in $owners) {
    $proc = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
    if ($proc) {
        Write-Host ("==> Stopping PID {0} ({1}) on port {2}" -f $proc.Id, $proc.ProcessName, $Port) -ForegroundColor Cyan
        Stop-Process -Id $proc.Id -Force
    }
}

Write-Host "Stopped." -ForegroundColor Green
