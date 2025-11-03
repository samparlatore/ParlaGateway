# scripts/Setup-Gateway.ps1

# Define repo and directory
$repoUrl = "https://github.com/samparlatore/ParlaGateway.git"
$repoDir = "ParlaGateway"

# Resolve root path (one level up from scripts/)
$rootPath = Resolve-Path "$PSScriptRoot\.."
$scriptsPath = "$PSScriptRoot"

# Check for Git
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Git is not installed."
    Write-Host "👉 Install Git: https://git-scm.com/downloads"
    exit 1
} else {
    Write-Host "✅ Git is installed."
}

# Check for Maven
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Maven is not installed."
    Write-Host "👉 Install Maven: https://maven.apache.org/install.html"
    exit 1
} else {
    Write-Host "✅ Maven is installed."
}

# Move to root directory
Set-Location $rootPath

# Clone repo if not present
if (-not (Test-Path $repoDir)) {
    Write-Host "📦 Cloning ParlaGateway..."
    git clone $repoUrl
} else {
    Write-Host "📁 ParlaGateway already exists."
}

# Compile if target not built
Set-Location "$rootPath\$repoDir"
if (-not (Test-Path "target")) {
    Write-Host "🔨 Compiling ParlaGateway..."
    mvn clean install
} else {
    Write-Host "✅ ParlaGateway already compiled."
}

# Prompt to start test server
$startTest = Read-Host "🚀 Start test server? (y/n)"
if ($startTest -match '^[Yy]$') {
    Write-Host "🧪 Launching test server..."
    & "$scriptsPath\start-test-server.ps1"
}

# Prompt to start gateway
$startGateway = Read-Host "🌐 Start gateway? (y/n)"
if ($startGateway -match '^[Yy]$') {
    Write-Host "🔌 Launching gateway..."
    & "$scriptsPath\start-gateway.ps1"
}