# Build (if needed) and run the Smart Campus API locally.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
#   powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 -Port 9090
#   powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 -Rebuild

[CmdletBinding()]
param(
    [int]$Port = 8080,
    [switch]$Rebuild
)

$ErrorActionPreference = 'Stop'

$jar = Join-Path (Get-Location) 'target\smart-campus-api.jar'

if ($Rebuild -or -not (Test-Path $jar)) {
    Write-Host "==> mvn clean package -DskipTests" -ForegroundColor Cyan
    mvn -q clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Maven build failed." -ForegroundColor Red
        exit 1
    }
}

Write-Host ("==> Starting Smart Campus API on port {0}" -f $Port) -ForegroundColor Green
Write-Host  "==> API will be available at http://localhost:$Port/api/v1"
Write-Host  "==> Ctrl+C to stop."
java "-Dserver.port=$Port" -jar $jar
