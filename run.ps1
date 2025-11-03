# run.ps1
$scriptDir = Join-Path $PSScriptRoot "scripts"
$setupScript = Join-Path $scriptDir "Setup-Gateway.ps1"

# Detect platform
if ($IsWindows) {
    Write-Host "🧰 Detected Windows system"
    if (Test-Path $setupScript) {
        Write-Host "📂 Running Setup-Gateway.ps1..."
        & PowerShell -ExecutionPolicy Bypass -File $setupScript
    } else {
        Write-Host "❌ Setup-Gateway.ps1 not found at $setupScript"
    }
}
elseif ($IsLinux -or $IsMacOS) {
    Write-Host "🧰 Detected Unix-like system"
    $bashScript = Join-Path $scriptDir "Setup-Gateway.sh"
    if (Test-Path $bashScript) {
        Write-Host "📂 Running Setup-Gateway.sh..."
        bash $bashScript
    } else {
        Write-Host "❌ Setup-Gateway.sh not found at $bashScript"
    }
}
else {
    Write-Host "❌ Unsupported OS"
    exit 1
}