nd se#!/bin/bash

# Gym CRM Backend Server Startup Script

echo "🏋️‍♂️ Starting Gym CRM Backend Server..."
echo "======================================"

# Set Java Home
export JAVA_HOME=/opt/homebrew/opt/openjdk@21

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 21."
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if MySQL is running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "⚠️  MySQL is not running. Starting MySQL..."
    brew services start mysql
    sleep 5
fi

echo "✅ MySQL is running"

# Start the Spring Boot application
echo "🚀 Starting Spring Boot application..."
echo "Server will be available at: http://localhost:8080"
echo "Press Ctrl+C to stop the server"
echo ""

./mvnw spring-boot:run
