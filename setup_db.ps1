# PostgreSQL Database Setup Script
Write-Host "Start PostgreSQL and setup database" -ForegroundColor Cyan

$pgctl = "C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe"
$pgdata = "C:\Program Files\PostgreSQL\18\data"
$createdb = "C:\Program Files\PostgreSQL\18\bin\createdb.exe"
$psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"

# Step 1: Start PostgreSQL
Write-Host "`n[1/4] Starting PostgreSQL server..." -ForegroundColor Green

$proc = Get-Process postgres -ErrorAction SilentlyContinue
if ($proc) {
    Write-Host "PostgreSQL is already running" -ForegroundColor Green
} else {
    Write-Host "Starting PostgreSQL service..."
    & $pgctl -D $pgdata start
    Start-Sleep -Seconds 3
    Write-Host "PostgreSQL started" -ForegroundColor Green
}

# Step 2: Create database
Write-Host "`n[2/4] Creating mindguard database..." -ForegroundColor Green
$output = & $createdb -U postgres mindguard 2>&1
Write-Host "Database created" -ForegroundColor Green

# Step 3: Initialize schema
Write-Host "`n[3/4] Initializing database schema..." -ForegroundColor Green
$schemaPath = "C:\JAVA\mind-guard\backend\src\main\resources\scripts\init.sql"
& $psql -U postgres -d mindguard -f $schemaPath

# Step 4: Verify setup
Write-Host "`n[4/4] Verifying setup..." -ForegroundColor Green
Write-Host "`nDatabase Tables:" -ForegroundColor Cyan
& $psql -U postgres -d mindguard -c "\dt"

Write-Host "`nUsers in Database:" -ForegroundColor Cyan
& $psql -U postgres -d mindguard -c "SELECT COUNT(*) as user_count FROM users;"

Write-Host "`nDatabase setup completed!" -ForegroundColor Green
