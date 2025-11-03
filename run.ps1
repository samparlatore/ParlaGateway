# run.ps1
$scriptDir = Join-Path $PSScriptRoot "scripts"
$repoBase = "https://raw.githubusercontent.com/samparlatore/ParlaGateway/main/scripts"

# Ensure scripts/ folder exists
if (-not (Test-Path $scriptDir)) {
    New-Item -ItemType Directory -Path $scriptDir | Out-Null
}

# List of required Windows scripts
$requiredScripts = @(
    "Setup-Gateway.ps1",
    "start-test-server.ps1",
    "start-gateway.ps1"
)

# Download missing scripts
foreach ($script in $requiredScripts) {
    $localPath = Join-Path $scriptDir $script
    if (-not (Test-Path $localPath)) {
        Write-Host "📥 Downloading $script..."
        Invoke-WebRequest "$repoBase/$script" -OutFile $localPath
    }
}

# Run setup
Write-Host "🧰 Detected Windows system"
& PowerShell -ExecutionPolicy Bypass -File (Join-Path $scriptDir "Setup-Gateway.ps1")