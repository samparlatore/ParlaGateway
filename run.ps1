# run.ps1
$scriptDir = Join-Path $PSScriptRoot "scripts"

# Detect platform
if ($IsWindows) {
    Write-Host "🧰 Detected Windows system"
    & "$scriptDir\Setup-Gateway.ps1"
}
elseif ($IsLinux -or $IsMacOS) {
    Write-Host "🧰 Detected Unix-like system"
    bash "$scriptDir/Setup-Gateway.sh"
}
else {
    Write-Host "❌ Unsupported OS"
    exit 1
}