#!/bin/bash

# Resolve paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_URL="https://github.com/samparlatore/ParlaGateway.git"
REPO_DIR="$ROOT_DIR/ParlaGateway"

# Check for Git
if ! command -v git &> /dev/null; then
  echo "❌ Git is not installed."
  echo "👉 Install Git: https://git-scm.com/downloads"
  exit 1
else
  echo "✅ Git is installed."
fi

# Check for Maven
if ! command -v mvn &> /dev/null; then
  echo "❌ Maven is not installed."
  echo "👉 Install Maven: https://maven.apache.org/install.html"
  exit 1
else
  echo "✅ Maven is installed."
fi

# Clone repo if not present
if [ ! -d "$REPO_DIR" ]; then
  echo "📦 Cloning ParlaGateway..."
  git clone "$REPO_URL" "$REPO_DIR"
else
  echo "📁 ParlaGateway already exists."
fi

# Compile if target not built
cd "$REPO_DIR" || exit 1
if [ ! -d "target" ]; then
  echo "🔨 Compiling ParlaGateway..."
  mvn clean install
else
  echo "✅ ParlaGateway already compiled."
fi

echo  "******************************************"
echo  "✅ ParlaGateway Setup complete. You access the gateway at http://127.0.0.1:8080 once started."
echo  "******************************************"

# Prompt to start test server
read -p "🚀 Start test server? (y/n): " start_test
if [[ "$start_test" =~ ^[Yy]$ ]]; then
  echo "🧪 Launching test server..."
  bash "$SCRIPT_DIR/start-test-server.sh"
fi

# Prompt to start gateway
read -p "🌐 Start gateway? (y/n): " start_gateway
if [[ "$start_gateway" =~ ^[Yy]$ ]]; then
  echo "🔌 Launching gateway..."
  bash "$SCRIPT_DIR/start-gateway.sh"
fi