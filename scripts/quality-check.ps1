# MysticTreasures Professional Quality Check Script
# This script runs industry-standard checks via Maven plugins.

Write-Host "--- MysticTreasures Professional Quality Check ---" -ForegroundColor Cyan

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "[ERROR] Maven (mvn) is required but not found in PATH." -ForegroundColor Red
    exit 1
}

# 1. Run Unit Tests
Write-Host "`n[1/4] Running Unit & Integration Tests (MockBukkit)..." -ForegroundColor Yellow
mvn test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed! Fix issues before proceeding." -ForegroundColor Red
    exit $LASTEXITCODE
}

# 2. Style Check (Checkstyle)
Write-Host "`n[2/4] Running Style Analysis (Checkstyle)..." -ForegroundColor Yellow
mvn checkstyle:check
if ($LASTEXITCODE -ne 0) {
    Write-Host "Style violations found! Check target/checkstyle-result.xml" -ForegroundColor Red
} else {
    Write-Host "Code style is excellent!" -ForegroundColor Green
}

# 3. Bug Hunting (SpotBugs)
Write-Host "`n[3/4] Running Bug Analysis (SpotBugs)..." -ForegroundColor Yellow
mvn spotbugs:check
if ($LASTEXITCODE -ne 0) {
    Write-Host "Potential bugs detected! Check target/spotbugsXml.xml" -ForegroundColor Red
} else {
    Write-Host "No obvious bugs detected by SpotBugs." -ForegroundColor Green
}

# 4. Code Metrics & Scoring
Write-Host "`n[4/4] Calculating Code Score..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Recurse -Filter "*.java" -Path "src"
$todoCount = 0
foreach ($file in $javaFiles) {
    $todoCount += (Get-Content $file.FullName | Select-String "TODO").Count
}

$baseScore = 100
$deductions = ($todoCount * 2)
# Heuristic: deduclt points if spotbugs or checkstyle failed in previous steps
if ($LASTEXITCODE -ne 0) { $baseScore -= 10 }

$finalScore = [Math]::Max(0, $baseScore - $deductions)

Write-Host "--- Summary ---"
Write-Host "Total Source Files: $($javaFiles.Count)"
Write-Host "Pending TODOs: $todoCount"
Write-Host "Final Quality Score: $finalScore/100" -ForegroundColor Green

Write-Host "`nFull report available in the 'target' directory." -ForegroundColor Gray
