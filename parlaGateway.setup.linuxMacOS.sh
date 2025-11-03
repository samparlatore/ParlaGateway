#!/bin/bash

# Define repo and directory
REPO_URL="https://github.com/samparlatore/ParlaGateway.git"
REPO_DIR="ParlaGateway"

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
  git clone "$REPO_URL"
else
  echo "📁 ParlaGateway already exists."
fi

# Compile if target not built
cd "$REPO_DIR" || exit
if [ ! -d "target" ]; then
  echo "🔨 Compiling ParlaGateway..."
  mvn clean install
else
  echo "✅ ParlaGateway already compiled."
fi

# Prompt to start test server
read -p "🚀 Start test server? (y/n): " start_test
if [[ "$start_test" =~ ^[Yy]$ ]]; then
  echo "🧪 Launching test server..."
  # Replace with actual test server command
  ./start-test-server.sh
fi

# Prompt to start gateway
read -p "🌐 Start gateway? (y/n): " start_gateway
if [[ "$start_gateway" =~ ^[Yy]$ ]]; then
  echo "🔌 Launching gateway..."
  # Replace with actual gateway command
  ./start-gateway.sh
fi