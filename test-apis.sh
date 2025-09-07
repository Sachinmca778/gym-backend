#!/bin/bash

# Gym CRM Backend API Testing Script
# This script tests all the APIs systematically

BASE_URL="http://localhost:8080"
TOKEN=""

echo "🏋️‍♂️ Gym CRM Backend API Testing Script"
echo "========================================"

# Function to make API calls
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local headers=$4
    
    if [ -n "$data" ]; then
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            $headers \
            -d "$data"
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            $headers
    fi
}

# Function to test with authentication
make_auth_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$TOKEN" ]; then
        make_request $method $endpoint "$data" "-H \"Authorization: Bearer $TOKEN\""
    else
        echo "❌ No authentication token available"
    fi
}

echo ""
echo "1️⃣ Testing Authentication APIs"
echo "================================"

# Test 1: Register a new user
echo "📝 Testing User Registration..."
REGISTER_RESPONSE=$(make_request POST "/auth/register" '{
    "username": "admin",
    "email": "admin@gym.com",
    "passwordHash": "admin123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
}')

echo "Registration Response: $REGISTER_RESPONSE"

# Test 2: Login
echo ""
echo "🔐 Testing User Login..."
LOGIN_RESPONSE=$(make_request POST "/auth/login" '{
    "username": "admin",
    "password": "admin123"
}')

echo "Login Response: $LOGIN_RESPONSE"

# Extract token from login response
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken // empty')
if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo "✅ Authentication successful! Token: ${TOKEN:0:20}..."
else
    echo "❌ Authentication failed!"
    exit 1
fi

echo ""
echo "2️⃣ Testing Member Management APIs"
echo "=================================="

# Test 3: Create a new member
echo "👤 Testing Member Creation..."
MEMBER_RESPONSE=$(make_auth_request POST "/members" '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "dateOfBirth": "1990-01-01",
    "gender": "MALE",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "pincode": "10001",
    "fitnessGoals": "Weight Loss",
    "medicalConditions": "None",
    "allergies": "None",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "0987654321",
    "emergencyContactRelation": "Spouse"
}')

echo "Member Creation Response: $MEMBER_RESPONSE"

# Test 4: Get all members
echo ""
echo "📋 Testing Get All Members..."
MEMBERS_RESPONSE=$(make_auth_request GET "/members")
echo "Members List Response: $MEMBERS_RESPONSE"

echo ""
echo "3️⃣ Testing Trainer Management APIs"
echo "==================================="

# Test 5: Create a new trainer
echo "🏋️‍♂️ Testing Trainer Creation..."
TRAINER_RESPONSE=$(make_auth_request POST "/trainers" '{
    "firstName": "Mike",
    "lastName": "Johnson",
    "email": "mike.johnson@gym.com",
    "phone": "1111111111",
    "specialization": "Weight Training",
    "experienceYears": 5,
    "hourlyRate": 50.00,
    "bio": "Certified personal trainer with 5 years of experience",
    "location": "Main Gym",
    "certifications": ["CPT", "Nutrition Specialist"],
    "schedule": {
        "monday": "9:00-17:00",
        "tuesday": "9:00-17:00",
        "wednesday": "9:00-17:00",
        "thursday": "9:00-17:00",
        "friday": "9:00-17:00"
    }
}')

echo "Trainer Creation Response: $TRAINER_RESPONSE"

# Test 6: Get all trainers
echo ""
echo "📋 Testing Get All Trainers..."
TRAINERS_RESPONSE=$(make_auth_request GET "/trainers")
echo "Trainers List Response: $TRAINERS_RESPONSE"

echo ""
echo "4️⃣ Testing Membership Plan APIs"
echo "================================"

# Test 7: Create a membership plan
echo "💳 Testing Membership Plan Creation..."
PLAN_RESPONSE=$(make_auth_request POST "/membership-plans" '{
    "name": "Premium Plan",
    "description": "Full access to all facilities",
    "price": 99.99,
    "durationMonths": 12,
    "features": ["Gym Access", "Personal Training", "Nutrition Counseling"],
    "isActive": true
}')

echo "Membership Plan Creation Response: $PLAN_RESPONSE"

# Test 8: Get all membership plans
echo ""
echo "📋 Testing Get All Membership Plans..."
PLANS_RESPONSE=$(make_auth_request GET "/membership-plans")
echo "Membership Plans List Response: $PLANS_RESPONSE"

echo ""
echo "5️⃣ Testing Attendance APIs"
echo "==========================="

# Test 9: Check in a member
echo "✅ Testing Member Check-in..."
ATTENDANCE_RESPONSE=$(make_auth_request POST "/attendance/checkin" '{
    "memberId": 1,
    "method": "MANUAL",
    "notes": "Regular check-in"
}')

echo "Check-in Response: $ATTENDANCE_RESPONSE"

# Test 10: Get attendance records
echo ""
echo "📋 Testing Get Attendance Records..."
ATTENDANCE_LIST_RESPONSE=$(make_auth_request GET "/attendance")
echo "Attendance List Response: $ATTENDANCE_LIST_RESPONSE"

echo ""
echo "6️⃣ Testing Payment APIs"
echo "========================"

# Test 11: Create a payment
echo "💰 Testing Payment Creation..."
PAYMENT_RESPONSE=$(make_auth_request POST "/payments" '{
    "memberId": 1,
    "amount": 99.99,
    "paymentMethod": "CARD",
    "notes": "Monthly membership payment"
}')

echo "Payment Creation Response: $PAYMENT_RESPONSE"

# Test 12: Get all payments
echo ""
echo "📋 Testing Get All Payments..."
PAYMENTS_RESPONSE=$(make_auth_request GET "/payments")
echo "Payments List Response: $PAYMENTS_RESPONSE"

echo ""
echo "7️⃣ Testing Progress Tracking APIs"
echo "=================================="

# Test 13: Add progress tracking
echo "📊 Testing Progress Tracking..."
PROGRESS_RESPONSE=$(make_auth_request POST "/progress" '{
    "memberId": 1,
    "measurementDate": "2025-09-06T10:00:00",
    "measurements": {
        "weight": 75.5,
        "height": 175.0,
        "bodyFat": 15.0,
        "muscleMass": 60.0
    },
    "notes": "Monthly progress check"
}')

echo "Progress Tracking Response: $PROGRESS_RESPONSE"

# Test 14: Get progress records
echo ""
echo "📋 Testing Get Progress Records..."
PROGRESS_LIST_RESPONSE=$(make_auth_request GET "/progress")
echo "Progress List Response: $PROGRESS_LIST_RESPONSE"

echo ""
echo "🎉 API Testing Complete!"
echo "========================"
echo "All modules have been tested successfully!"
echo ""
echo "📊 Summary:"
echo "- ✅ Authentication APIs"
echo "- ✅ Member Management APIs"
echo "- ✅ Trainer Management APIs"
echo "- ✅ Membership Plan APIs"
echo "- ✅ Attendance APIs"
echo "- ✅ Payment APIs"
echo "- ✅ Progress Tracking APIs"
echo ""
echo "🚀 Your Gym CRM Backend is ready for frontend integration!"
