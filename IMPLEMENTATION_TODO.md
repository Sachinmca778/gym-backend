# Database Constraints & Code Validation Implementation

## Task Overview
Implement database constraints and code-level validations for the gym management system.

## Changes Summary

### 1️⃣ Users Table - is_active NOT NULL with DEFAULT
- [x] Alter is_active column to TINYINT(1) NOT NULL DEFAULT 1
- [x] Add code-level validation for gym_id based on role

### 2️⃣ Members Table - Critical Constraints
- [x] Add UNIQUE constraint on user_id
- [x] Add UNIQUE constraint on (email, gym_id)
- [x] Add code validation for email uniqueness per gym
- [x] Add code validation for 1-1 relationship with user_id

### 3️⃣ Attendance Table - Index for Performance & Safety
- [x] Create INDEX on (gym_id, member_id)

### 4️⃣ Payments Table - Business Critical
- [x] Add UNIQUE constraint on (transaction_id, gym_id)
- [x] Create INDEX on (gym_id, member_id)

### 5️⃣ Member Memberships - Core Logic
- [x] Create UNIQUE INDEX on (member_id, gym_id, status)
- [x] Add code validation to prevent multiple ACTIVE memberships

### 6️⃣ Trainers Table - Login Safety
- [x] Add UNIQUE constraint on user_id
- [x] Add code validation for gym match

### 7️⃣ Foreign Keys - Data Integrity
- [x] Add FK for members → gyms
- [x] Add FK for other relationships as needed

---

## Files to Create/Modify

### New Files
1. [x] `gym-backend/src/main/resources/db/migration/V2__add_constraints_and_indexes.sql`
2. [x] `gym-backend/src/main/java/com/example/gym/backend/service/UserService.java`
3. [x] `gym-backend/src/main/java/com/example/gym/backend/exception/ValidationException.java`
4. [x] `gym-backend/src/main/java/com/example/gym/backend/exception/GlobalExceptionHandler.java`
5. [x] `gym-backend/src/main/java/com/example/gym/backend/dto/UserDto.java`

### Files to Modify
1. [x] `gym-backend/src/main/java/com/example/gym/backend/service/MemberService.java`
2. [x] `gym-backend/src/main/java/com/example/gym/backend/service/MemberMembershipService.java`
3. [x] `gym-backend/src/main/java/com/example/gym/backend/service/TrainerService.java`
4. [x] `gym-backend/src/main/java/com/example/gym/backend/entity/User.java`
5. [x] `gym-backend/src/main/java/com/example/gym/backend/entity/Member.java`
6. [x] `gym-backend/src/main/java/com/example/gym/backend/entity/Trainer.java`
7. [x] `gym-backend/src/main/java/com/example/gym/backend/repository/MemberRepository.java`
8. [x] `gym-backend/src/main/java/com/example/gym/backend/repository/MemberMembershipRepository.java`
9. [x] `gym-backend/src/main/java/com/example/gym/backend/repository/TrainerRepository.java`

---

## Implementation Steps

### Step 1: Create SQL Migration File
- [x] Create V2__add_constraints_and_indexes.sql with all constraints

### Step 2: Create Exception Classes
- [x] ValidationException.java
- [x] GlobalExceptionHandler.java

### Step 3: Create UserService
- [x] Validate gym_id based on role (ADMIN = NULL, others = NOT NULL)

### Step 4: Update MemberService
- [x] Add email uniqueness validation per gym
- [x] Add user_id uniqueness validation
- [x] Validate gym_id is set for non-ADMIN roles

### Step 5: Update MemberMembershipService
- [x] Add check to prevent multiple ACTIVE memberships
- [x] Validate member's gym matches membership's gym

### Step 6: Update TrainerService
- [x] Add unique user_id validation
- [x] Add gym match validation

### Step 7: Update Entity Classes
- [x] Add @ColumnDefault annotations where needed
- [x] Update JPA annotations for constraints

### Step 8: Update Repository Classes
- [x] Add validation query methods to MemberRepository
- [x] Add validation query methods to MemberMembershipRepository
- [x] Add validation query methods to TrainerRepository

---

## ✅ ALL COMPLETED

### Database Changes (SQL)
All constraints and indexes have been added to:
- `V2__add_constraints_and_indexes.sql`

### Code-Level Validations Implemented:

#### 1. UserService - Role-based gym_id validation:
- ADMIN → gym_id must be NULL
- MANAGER, RECEPTIONIST, TRAINER, MEMBER → gym_id must NOT be NULL

#### 2. MemberService:
- Email uniqueness per gym
- User_id uniqueness (1-1 relationship)
- Gym validation

#### 3. MemberMembershipService:
- Prevent multiple ACTIVE memberships per member per gym
- Validate member's gym matches membership's gym

#### 4. TrainerService:
- User_id uniqueness
- User role validation (must be TRAINER)
- Gym match validation

---

## How to Apply Database Changes

### Option 1: Run SQL directly in MySQL
```sql
-- Run the contents of V2__add_constraints_and_indexes.sql
source /path/to/gym-backend/src/main/resources/db/migration/V2__add_constraints_and_indexes.sql
```

### Option 2: If using Flyway/Liquibase
The migration file will be automatically applied on next startup.

---

## Next Steps for Mobile App Changes

After applying database changes, update mobile app to:
1. Handle validation errors from API
2. Add role-based UI changes
3. Update forms to require gym_id for non-ADMIN users

**Created:** Current Session
**Last Updated:** Current Session

