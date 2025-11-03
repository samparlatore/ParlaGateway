#!/bin/bash

SCRIPT_DIR="./scripts"
SETUP_SCRIPT="$SCRIPT_DIR/Setup-Gateway.sh"

# Ensure setup script exists
if [ ! -f "$SETUP_SCRIPT" ]; then
  echo "❌ Setup-Gateway.sh not found at $SETUP_SCRIPT"
  echo "👉 Make sure you've cloned the repo or downloaded the scripts folder."
  exit 1
fi

# Run setup
echo "🧰 Detected Unix-like system"
bash "$SETUP_SCRIPT"