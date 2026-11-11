<#
.SYNOPSIS
    Starts the whole local dev stack: Docker Desktop, Postgres + Mongo (compose.yaml),
    the Spring Boot backend and the Angular frontend, each server in its own window.

.EXAMPLE
    .\start-all.ps1                 # everything
    .\start-all.ps1 -NoBackend      # run backend yourself from VS Code (debugger)
    .\start-all.ps1 -NoFront -NoBrowser
    .\start-all.ps1 -Lan            # front reachable from other devices: http://<this-PC-IP>:49191
#>
param(
    [string]$FrontDir = (Join-Path $PSScriptRoot '..\BaseServer_front'),
    [switch]$NoBackend,
    [switch]$NoFront,
    [switch]$NoBrowser,
    [switch]$Lan
)

$BackendDir   = $PSScriptRoot
$DockerExe    = 'C:\Program Files\Docker\Docker\Docker Desktop.exe'
$BackendPort  = 49192
$FrontPort    = 49191

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Fail($msg) { Write-Host "ERROR: $msg" -ForegroundColor Red; exit 1 }

# Checks IPv4 and IPv6: ng serve listens on ::1 only, Spring Boot on both.
function Test-Port($port) {
    foreach ($addr in '127.0.0.1', '::1') {
        $ip = [System.Net.IPAddress]::Parse($addr)
        $client = New-Object System.Net.Sockets.TcpClient($ip.AddressFamily)
        try { $client.Connect($ip, $port); return $true } catch { } finally { $client.Dispose() }
    }
    return $false
}

function Wait-Until($what, $timeoutSec, [scriptblock]$check) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    Write-Host "    waiting for $what " -NoNewline
    while ((Get-Date) -lt $deadline) {
        if (& $check) { Write-Host ' ok' -ForegroundColor Green; return $true }
        Write-Host '.' -NoNewline
        Start-Sleep -Seconds 2
    }
    Write-Host ' timed out' -ForegroundColor Yellow
    return $false
}

# Opens a new PowerShell window running $script. Encoded so quotes/newlines survive.
function Start-Window($title, $workDir, $script) {
    $full = "`$Host.UI.RawUI.WindowTitle = '$title'`nSet-Location -LiteralPath '$workDir'`n$script"
    $encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($full))
    Start-Process powershell -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $encoded
}

# --- 1. Docker Desktop -------------------------------------------------------
Write-Step 'Docker Desktop'
cmd /c "docker info >nul 2>&1"
if ($LASTEXITCODE -ne 0) {
    if (-not (Test-Path $DockerExe)) { Fail "Docker Desktop not found at $DockerExe" }
    Start-Process $DockerExe
    $ok = Wait-Until 'Docker engine' 180 { cmd /c "docker info >nul 2>&1"; $LASTEXITCODE -eq 0 }
    if (-not $ok) { Fail 'Docker engine did not start in 3 minutes. Open Docker Desktop and check it.' }
} else {
    Write-Host '    already running'
}

# --- 2. Postgres + Mongo -----------------------------------------------------
# Only the DB services: the "app" service in compose.yaml is the backend, which runs locally below.
Write-Step 'Postgres + Mongo (docker compose)'
# Stop containers from other projects holding our ports (e.g. another project's postgres on 5432).
$ours = @(docker ps --filter 'label=com.docker.compose.project=baseserver' --format '{{.Names}}')
foreach ($port in 5432, 37017) {
    docker ps --filter "publish=$port" --format '{{.Names}}' | Where-Object { $ours -notcontains $_ } | ForEach-Object {
        Write-Host "    stopping '$_' (uses port $port)" -ForegroundColor Yellow
        docker stop $_ | Out-Null
    }
}
Push-Location $BackendDir
docker compose up -d postgres mongo
$composeExit = $LASTEXITCODE
if ($composeExit -eq 0) {
    $ok = Wait-Until 'Postgres' 60 { cmd /c "docker compose exec -T postgres pg_isready -U myuser -d mydatabase >nul 2>&1"; $LASTEXITCODE -eq 0 }
}
Pop-Location
if ($composeExit -ne 0) { Fail 'docker compose failed (port 5432 or 37017 taken by another container?). See output above.' }
if (-not $ok) { Fail 'Postgres did not become ready. Check: docker compose logs postgres' }

# --- 3. Backend --------------------------------------------------------------
if (-not $NoBackend) {
    Write-Step "Backend (Spring Boot, port $BackendPort)"
    if (Test-Port $BackendPort) {
        Write-Host "    port $BackendPort already in use - backend probably running, skipping" -ForegroundColor Yellow
    } else {
        $mvn = 'mvn'
        if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { $mvn = '.\mvnw.cmd' }
        # Same env as .vscode/launch.json (ddl-auto=update keeps data between runs), plus Mongo on the host port published by compose.yaml.
        Start-Window "BaseServer backend :$BackendPort" $BackendDir @"
`$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/mydatabase'
`$env:SPRING_DATASOURCE_USERNAME = 'myuser'
`$env:SPRING_DATASOURCE_PASSWORD = 'secret'
`$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
`$env:SPRING_DATA_MONGODB_URI = 'mongodb://localhost:37017/no_sql_db'
$mvn spring-boot:run
"@
    }
}

# --- 4. Frontend -------------------------------------------------------------
if (-not $NoFront) {
    Write-Step "Frontend (Angular, port $FrontPort)"
    $FrontDir = [IO.Path]::GetFullPath($FrontDir)
    if (-not (Test-Path (Join-Path $FrontDir 'package.json'))) { Fail "Frontend not found at $FrontDir (pass -FrontDir)" }
    if (Test-Port $FrontPort) {
        Write-Host "    port $FrontPort already in use - frontend probably running, skipping" -ForegroundColor Yellow
    } else {
        $install = ''
        if (-not (Test-Path (Join-Path $FrontDir 'node_modules'))) { $install = "npm install`n" }
        # -Lan: binds 0.0.0.0 and proxies /api to the backend (see proxy.lan.json), so one port serves everything.
        $npmScript = if ($Lan) { 'npm run start:lan' } else { 'npm start' }
        Start-Window "BaseServer front :$FrontPort" $FrontDir "${install}$npmScript"
    }
}

# --- 5. Wait + open browser --------------------------------------------------
if (-not $NoBackend) { Wait-Until "backend on :$BackendPort" 240 { Test-Port $BackendPort } | Out-Null }
if (-not $NoFront) {
    $frontUp = Wait-Until "frontend on :$FrontPort" 240 { Test-Port $FrontPort }
    if ($frontUp -and -not $NoBrowser) { Start-Process "http://localhost:$FrontPort" }
}

Write-Host "`nDone. Backend http://localhost:$BackendPort  |  Front http://localhost:$FrontPort" -ForegroundColor Green
if ($Lan) {
    # Private-network IPv4s only; skips loopback, link-local and VPN/virtual adapters' public-profile IPs.
    $lanIps = Get-NetConnectionProfile | Where-Object NetworkCategory -eq 'Private' |
        ForEach-Object { Get-NetIPAddress -InterfaceIndex $_.InterfaceIndex -AddressFamily IPv4 -ErrorAction SilentlyContinue } |
        Select-Object -ExpandProperty IPAddress
    foreach ($ip in $lanIps) { Write-Host "LAN:  http://${ip}:$FrontPort" -ForegroundColor Green }
}
Write-Host 'Stop: close the backend/front windows, then run: docker compose stop'
