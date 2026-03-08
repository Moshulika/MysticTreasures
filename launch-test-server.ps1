param (
    [Parameter(Mandatory=$true)]
    [string]$ServerPath
)

Write-Host "--- MysticTreasures Mock Server Test ---" -ForegroundColor Cyan

# 1. Build the plugin
Write-Host "Building plugin..." -ForegroundColor Yellow
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn clean package -DskipTests
} else {
    Write-Host "Maven not found! Attempting to find existing JAR in target..." -ForegroundColor Red
}

$jar = Get-ChildItem "target/MysticTreasures.jar" | Select-Object -First 1
if (-not $jar) {
    Write-Host "Could not find MysticTreasures.jar. Build failed?" -ForegroundColor Red
    exit 1
}

# 2. Deploy to server
if (-not (Test-Path "$ServerPath/plugins")) {
    New-Item -ItemType Directory -Path "$ServerPath/plugins" -Force
}

Write-Host "Deploying $($jar.Name) to $ServerPath/plugins..." -ForegroundColor Yellow
Copy-Item $jar.FullName -Destination "$ServerPath/plugins/" -Force

# 3. Check for server jar
$serverJar = Get-ChildItem "$ServerPath/*.jar" | Where-Object { $_.Name -match "paper|spigot|purpur" } | Select-Object -First 1
if (-not $serverJar) {
    Write-Host "No server JAR (paper/spigot) found in $ServerPath!" -ForegroundColor Red
    exit 1
}

# 4. Launch and Monitor
Write-Host "Launching server: $($serverJar.Name)..." -ForegroundColor Yellow
Write-Host "Monitoring logs for enablement. This will timeout after 60 seconds." -ForegroundColor Gray

# Start the process
$process = Start-Process java -ArgumentList "-Xmx1G -Xms1G -jar $($serverJar.Name) nogui" -WorkingDirectory $ServerPath -PassThru -WindowStyle Hidden

$startTime = Get-Date
$enabled = $false
$logPath = "$ServerPath/logs/latest.log"

while ((Get-Date) -lt $startTime.AddSeconds(60)) {
    if (Test-Path $logPath) {
        $logs = Get-Content $logPath -Tail 20
        if ($logs -match "\[MysticTreasures\] Enabling MysticTreasures") {
            Write-Host "`n[SUCCESS] MysticTreasures enabled correctly!" -ForegroundColor Green
            $enabled = $true
            break
        }
        if ($logs -match "\[MysticTreasures\] Error" -or $logs -match "Could not load 'plugins") {
            Write-Host "`n[ERROR] Errors detected in server logs!" -ForegroundColor Red
            break
        }
    }
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 2
}

# 5. Cleanup
Write-Host "`nShutting down test server..." -ForegroundColor Gray
$process | Stop-Process -Force

if (-not $enabled) {
    Write-Host "Plugin did not enable within 60 seconds. Check server logs." -ForegroundColor Red
    exit 1
}

Write-Host "Mock test complete!" -ForegroundColor Green
