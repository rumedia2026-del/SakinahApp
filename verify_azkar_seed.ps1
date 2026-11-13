# PowerShell Verification Script for Azkar Seed Integrity
$ErrorActionPreference = "Stop"

$seedPath = "app/src/main/res/raw/azkar_seed.json"
if (-not (Test-Path $seedPath)) {
    Write-Error "azkar_seed.json not found!"
    exit 1
}

$rawJson = Get-Content -Path $seedPath -Raw -Encoding UTF8
$data = $rawJson | ConvertFrom-Json

Write-Host "=== 1. Checking Sleeping Azkar (Category 4) ===" -ForegroundColor Cyan
$sleeping = $data.azkar | Where-Object { $_.categoryId -eq 4 }
Write-Host "Sleeping Azkar Count: $($sleeping.Count)"

if ($sleeping.Count -lt 12) {
    Write-Error "FAIL: Sleeping Azkar count is $($sleeping.Count) (expected at least 12)"
    exit 1
}

$hasIstikhara = $false
foreach ($z in $sleeping) {
    if ($z.textPlain -like "*الاستخارة*" -or $z.textPlain -like "*أستخيرك*") {
        $hasIstikhara = $true
        Write-Error "FAIL: Sleeping Azkar contains Istikhara in ID: $($z.id)"
        exit 1
    }
}
if (-not $hasIstikhara) {
    Write-Host "PASS: Sleeping Azkar contains no Istikhara text." -ForegroundColor Green
}

Write-Host "`n=== 2. Checking Category Emptiness ===" -ForegroundColor Cyan
foreach ($cat in $data.categories) {
    $count = ($data.azkar | Where-Object { $_.categoryId -eq $cat.id }).Count
    if ($count -eq 0) {
        Write-Error "FAIL: Category '$($cat.titleAr)' (ID $($cat.id)) is empty!"
        exit 1
    } else {
        Write-Host "PASS: Category $($cat.id) [$($cat.titleAr)]: $count azkar" -ForegroundColor Green
    }
}

Write-Host "`n=== 3. Checking Emotional Tags ===" -ForegroundColor Cyan
$tags = @("قلق", "حزن", "خوف", "هم", "رزق", "شكر", "استخارة", "مرض", "غضب")
foreach ($t in $tags) {
    $c = ($data.azkar | Where-Object { $_.emotionalTags -and $_.emotionalTags -like "*$t*" }).Count
    if ($c -lt 10) {
        Write-Error "FAIL: Tag '$t' has only $c azkar (expected >= 10)"
        exit 1
    } else {
        Write-Host "PASS: Tag '$t': $c azkar" -ForegroundColor Green
    }
}

Write-Host "`n=== 4. Testing 5 Random Audio URLs (HEAD Request) ===" -ForegroundColor Cyan
$audioList = $data.azkar | Where-Object { $_.audioUrl -and $_.audioUrl.StartsWith("https://") }
$sample = $audioList | Get-Random -Count 5

foreach ($item in $sample) {
    $url = $item.audioUrl
    try {
        $resp = Invoke-WebRequest -Uri $url -Method Head -TimeoutSec 10 -UseBasicParsing
        if ($resp.StatusCode -eq 200) {
            Write-Host "PASS [200 OK]: $url" -ForegroundColor Green
        } else {
            Write-Warning "WARN: Status $($resp.StatusCode) for $url"
        }
    } catch {
        Write-Warning "WARN: Could not reach $url ($($_.Exception.Message))"
    }
}

Write-Host "`nAll Pre-Push Data Audits Passed Successfully!" -ForegroundColor Green
