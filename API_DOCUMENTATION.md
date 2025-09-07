# Gym CRM Backend API Documentation

## Base URL
```
http://localhost:8080
```

## Authentication
All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 🔐 Authentication APIs

### 1. Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@gym.com",
  "passwordHash": "admin123",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN"
}
```

### 2. Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

### 3. Refresh Token
```http
POST /auth/refresh?refreshToken=<refresh_token>
```

### 4. Logout
```http
POST /auth/logout?token=<access_token>
```

---

## 👤 Member Management APIs

### 1. Create Member
```http
POST /members
Authorization: Bearer <token>
Content-Type: application/json

{
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
}
```

### 2. Get All Members
```http
GET /members
Authorization: Bearer <token>
```

### 3. Get Member by ID
```http
GET /members/{id}
Authorization: Bearer <token>
```

### 4. Update Member
```http
PUT /members/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  // ... other fields
}
```

### 5. Delete Member
```http
DELETE /members/{id}
Authorization: Bearer <token>
```

### 6. Search Members
```http
GET /members/search?q={searchTerm}&page={page}&size={size}
Authorization: Bearer <token>
```

---

## 🏋️‍♂️ Trainer Management APIs

### 1. Create Trainer
```http
POST /trainers
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Mike",
  "lastName": "Johnson",
  "email": "mike.johnson@gym.com",
  "phone": "1111111111",
  "specialization": "Weight Training",
  "experienceYears": 5,
  "hourlyRate": 50.00,
  "bio": "Certified personal trainer",
  "location": "Main Gym",
  "certifications": ["CPT", "Nutrition Specialist"],
  "schedule": {
    "monday": "9:00-17:00",
    "tuesday": "9:00-17:00"
  }
}
```

### 2. Get All Trainers
```http
GET /trainers
Authorization: Bearer <token>
```

### 3. Get Trainer by ID
```http
GET /trainers/{id}
Authorization: Bearer <token>
```

### 4. Update Trainer
```http
PUT /trainers/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

### 5. Delete Trainer
```http
DELETE /trainers/{id}
Authorization: Bearer <token>
```

---

## 💳 Membership Plan APIs

### 1. Create Membership Plan
```http
POST /membership-plans
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Premium Plan",
  "description": "Full access to all facilities",
  "price": 99.99,
  "durationMonths": 12,
  "features": ["Gym Access", "Personal Training"],
  "isActive": true
}
```

### 2. Get All Membership Plans
```http
GET /membership-plans
Authorization: Bearer <token>
```

### 3. Get Membership Plan by ID
```http
GET /membership-plans/{id}
Authorization: Bearer <token>
```

### 4. Update Membership Plan
```http
PUT /membership-plans/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

### 5. Delete Membership Plan
```http
DELETE /membership-plans/{id}
Authorization: Bearer <token>
```

---

## ✅ Attendance Management APIs

### 1. Check In Member
```http
POST /attendance/checkin
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 1,
  "method": "MANUAL",
  "notes": "Regular check-in"
}
```

### 2. Check Out Member
```http
POST /attendance/checkout
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 1,
  "notes": "Regular check-out"
}
```

### 3. Get Attendance Records
```http
GET /attendance
Authorization: Bearer <token>
```

### 4. Get Member Attendance
```http
GET /attendance/member/{memberId}
Authorization: Bearer <token>
```

### 5. Get Attendance by Date Range
```http
GET /attendance/date-range?startDate={startDate}&endDate={endDate}
Authorization: Bearer <token>
```

---

## 💰 Payment Management APIs

### 1. Create Payment
```http
POST /payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 1,
  "amount": 99.99,
  "paymentMethod": "CARD",
  "notes": "Monthly membership payment"
}
```

### 2. Get All Payments
```http
GET /payments
Authorization: Bearer <token>
```

### 3. Get Payment by ID
```http
GET /payments/{id}
Authorization: Bearer <token>
```

### 4. Get Member Payments
```http
GET /payments/member/{memberId}
Authorization: Bearer <token>
```

### 5. Update Payment Status
```http
PUT /payments/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

---

## 📊 Progress Tracking APIs

### 1. Add Progress Record
```http
POST /progress
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 1,
  "measurementDate": "2025-09-06T10:00:00",
  "measurements": {
    "weight": 75.5,
    "height": 175.0,
    "bodyFat": 15.0,
    "muscleMass": 60.0
  },
  "notes": "Monthly progress check"
}
```

### 2. Get Progress Records
```http
GET /progress
Authorization: Bearer <token>
```

### 3. Get Member Progress
```http
GET /progress/member/{memberId}
Authorization: Bearer <token>
```

### 4. Update Progress Record
```http
PUT /progress/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

### 5. Delete Progress Record
```http
DELETE /progress/{id}
Authorization: Bearer <token>
```

---

## 🏃‍♂️ Workout Session APIs

### 1. Create Workout Session
```http
POST /workout-sessions
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 1,
  "trainerId": 1,
  "sessionDate": "2025-09-06",
  "startTime": "10:00",
  "endTime": "11:00",
  "workoutType": "Weight Training",
  "exercises": [
    {
      "name": "Bench Press",
      "sets": 3,
      "reps": 10,
      "weight": 135
    }
  ],
  "durationMinutes": 60,
  "caloriesBurned": 300,
  "notes": "Great session!"
}
```

### 2. Get Workout Sessions
```http
GET /workout-sessions
Authorization: Bearer <token>
```

### 3. Get Member Workout Sessions
```http
GET /workout-sessions/member/{memberId}
Authorization: Bearer <token>
```

---

## 📈 Dashboard & Analytics APIs

### 1. Get Dashboard Stats
```http
GET /dashboard/stats
Authorization: Bearer <token>
```

### 2. Get Member Analytics
```http
GET /analytics/members
Authorization: Bearer <token>
```

### 3. Get Revenue Analytics
```http
GET /analytics/revenue
Authorization: Bearer <token>
```

---

## 🔧 Error Responses

All APIs return consistent error responses:

```json
{
  "status": 400,
  "message": "Bad Request",
  "details": "Validation failed",
  "timestamp": "2025-09-06T22:44:03.556155"
}
```

## 📝 Notes for Frontend Integration

1. **Authentication**: Store the JWT token in localStorage or sessionStorage
2. **Token Refresh**: Implement automatic token refresh before expiration
3. **Error Handling**: Handle 401 (Unauthorized) responses by redirecting to login
4. **Loading States**: Show loading indicators for all API calls
5. **Form Validation**: Implement client-side validation before API calls
6. **Pagination**: Use page and size parameters for list endpoints
7. **Search**: Implement search functionality using the search endpoints
8. **Real-time Updates**: Consider implementing WebSocket for real-time notifications

## 🚀 Getting Started

1. Start the backend server:
   ```bash
   ./start-server.sh
   ```

2. Test all APIs:
   ```bash
   ./test-apis.sh
   ```

3. Use the API documentation above to integrate with your frontend

---

**Happy Coding! 🏋️‍♂️💪**
