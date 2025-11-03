#!/bin/bash

echo "🚀 Starting gateway..."

# Move into the project directory
pushd ../ParlaGateway > /dev/null

# Define paths
JAR_PATH="target/ParlaGateway-1.0-SNAPSHOT.jar"
LIB_PATH="lib/*"
CLASS_NAME="com.parlAquatics.gateway.ParlaGateway"

# Launch the gateway in the background
nohup java -cp "$JAR_PATH:$LIB_PATH" "$CLASS_NAME" > logs/gateway.out 2>&1 &

echo "🔗 Gateway is launching in the background..."
echo "🌐 Once ready, open your browser to http://127.0.0.1:8080"

# Return to original directory
popd > /dev/null