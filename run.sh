#!/bin/bash

if [[ "$OSTYPE" == "linux-gnu"* || "$OSTYPE" == "darwin"* ]]; then
  echo "🧰 Detected Unix-like system"
  bash scripts/Setup-Gateway.sh
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
  echo "🧰 Detected Windows system"
  powershell -ExecutionPolicy Bypass -File scripts/Setup-Gateway.ps1
else
  echo "❌ Unsupported OS: $OSTYPE"
  exit 1
fi