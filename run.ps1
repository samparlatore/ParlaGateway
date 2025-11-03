# run.ps1
$scriptDir = Join-Path $PSScriptRoot "scripts"
$repoBase = "https://raw.githubusercontent.com/samparlatore/ParlaGateway/refs/heads/master/scripts"

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
    $remoteUrl = "$repoBase/$script"  # Correct interpolation
    if (-not (Test-Path $localPath)) {
        Write-Host "📥 Downloading $script..."
        Invoke-WebRequest -Uri $remoteUrl -OutFile $localPath
    }
}

# Run setup
Write-Host "🧰 Detected Windows system"
$setupScript = Join-Path $scriptDir "Setup-Gateway.ps1"
if (Test-Path $setupScript) {
    & PowerShell -ExecutionPolicy Bypass -File $setupScript
} else {
    Write-Host "❌ Setup-Gateway.ps1 not found at $setupScript"
}