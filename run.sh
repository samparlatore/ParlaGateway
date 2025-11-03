#!/bin/bash

SCRIPT_DIR="./scripts"
#REPO_RAW_BASE="https://raw.githubusercontent.com/samparlatore/ParlaGateway/main/scripts"
$repoBase = "https://raw.githubusercontent.com/samparlatore/ParlaGateway/refs/heads/master/scripts"

# Ensure scripts/ folder exists
mkdir -p "$SCRIPT_DIR"

# List of required Unix scripts
REQUIRED_SCRIPTS=(
  "Setup-Gateway.sh"
  "start-test-server.sh"
  "start-gateway.sh"
)

# Download missing scripts
for script in "${REQUIRED_SCRIPTS[@]}"; do
  local_path="$SCRIPT_DIR/$script"
  remote_url="$REPO_RAW_BASE/$script"
  if [ ! -f "$local_path" ]; then
    echo "📥 Downloading $script..."
    curl -s -L "$remote_url" -o "$local_path"
    chmod +x "$local_path"
  fi
done

# Run setup
echo "🧰 Detected Unix-like system"
bash "$SCRIPT_DIR/Setup-Gateway.sh"