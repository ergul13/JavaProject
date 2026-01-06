#!/bin/bash
# Quick Test Script for Traffic Light Control System

echo "======================================"
echo "Traffic Light Control System - Test"
echo "======================================"
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

echo "✓ Step 1: Cleaning previous builds..."
./mvnw clean > /dev/null 2>&1

echo "✓ Step 2: Compiling project..."
./mvnw compile > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "✅ Compilation: SUCCESS"
else
    echo "❌ Compilation: FAILED"
    exit 1
fi

echo "✓ Step 3: Running application..."
echo ""
echo "🚀 Launching JavaFX Application..."
echo "   (Application window should open)"
echo ""
echo "📝 Quick Test Instructions:"
echo "   1. Enter vehicle counts (e.g., 20, 15, 10, 25)"
echo "   2. Click 'Start Simulation'"
echo "   3. Watch vehicles move and lights change"
echo "   4. Verify countdown timers work"
echo "   5. Try 'Pause' and 'Reset' buttons"
echo "   6. Try 'Generate Random Density'"
echo ""
echo "Press Ctrl+C to exit when done testing"
echo ""
echo "======================================"

./mvnw javafx:run

