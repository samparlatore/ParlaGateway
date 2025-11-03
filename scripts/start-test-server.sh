#!/bin/bash

echo "🚀 Starting test server..."

# Move into the project directory
pushd ../ParlaGateway > /dev/null

# Define paths
JAR_PATH="target/ParlaGateway-1.0-SNAPSHOT.jar"
LIB_PATH="lib/*"
CLASS_NAME="com.parlAquatics.exchange.QuickFIXServer"

# Launch the test server in the background
nohup java -cp "$JAR_PATH:$LIB_PATH" "$CLASS_NAME" > logs/test-server.out 2>&1 &

echo "🧪 Test server is launching in the background..."

# Return to original directory
popd > /dev/null