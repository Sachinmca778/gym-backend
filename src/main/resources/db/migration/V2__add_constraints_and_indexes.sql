-- ==============================================================================
-- Database Constraints and Indexes Migration
-- V2__add_constraints_and_indexes.sql
-- ==============================================================================

-- ==============================================================================
-- 1️⃣ USERS TABLE - is_active NOT NULL with DEFAULT
-- ==============================================================================
ALTER TABLE users 
MODIFY is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1 = active, 0 = inactive';

-- ==============================================================================
-- 2️⃣ MEMBERS TABLE - Critical Constraints
-- ==============================================================================

-- A) Add UNIQUE constraint on user_id (1-1 relationship for member login)
ALTER TABLE members 
ADD CONSTRAINT uq_members_user UNIQUE (user_id);

-- B) Add UNIQUE constraint on (email, gym_id) - email unique per gym
ALTER TABLE members 
ADD CONSTRAINT uq_member_email_per_gym UNIQUE (email, gym_id);

-- ==============================================================================
-- 3️⃣ ATTENDANCE TABLE - Index for Performance & Safety
-- ==============================================================================
CREATE INDEX idx_attendance_gym_member 
ON attendance (gym_id, member_id);

-- ==============================================================================
-- 4️⃣ PAYMENTS TABLE - Business Critical
-- ==============================================================================

-- A) Transaction ID unique per gym (prevent duplicate transactions)
ALTER TABLE payments 
ADD CONSTRAINT uq_payment_txn UNIQUE (transaction_id, gym_id);

-- B) Index for performance + safety
CREATE INDEX idx_payment_gym_member 
ON payments (gym_id, member_id);

-- ==============================================================================
-- 5️⃣ MEMBER MEMBERSHIPS - Core Logic (Prevent multiple ACTIVE plans)
-- ==============================================================================
-- Note: MySQL doesn't support partial indexes, so we use unique index + code validation
CREATE UNIQUE INDEX uq_active_membership 
ON member_memberships (member_id, gym_id, status);

-- ==============================================================================
-- 6️⃣ TRAINERS TABLE - Login Safety
-- ==============================================================================
ALTER TABLE trainers 
ADD CONSTRAINT uq_trainer_user UNIQUE (user_id);

-- ==============================================================================
-- 7️⃣ FOREIGN KEYS - Data Integrity
-- ==============================================================================

-- Members → Gyms
ALTER TABLE members 
ADD CONSTRAINT fk_members_gym 
FOREIGN KEY (gym_id) REFERENCES gyms(id);

-- Users → Gyms
ALTER TABLE users 
ADD CONSTRAINT fk_users_gym 
FOREIGN KEY (gym_id) REFERENCES gyms(id);

-- Attendance → Gyms
ALTER TABLE attendance 
ADD CONSTRAINT fk_attendance_gym 
FOREIGN KEY (gym_id) REFERENCES gyms(id);

-- Attendance → Members
ALTER TABLE attendance 
ADD CONSTRAINT fk_attendance_member 
FOREIGN KEY (member_id) REFERENCES members(id);

-- Payments → Gyms
ALTER TABLE payments 
ADD CONSTRAINT fk_payments_gym 
FOREIGN KEY (gym_id) REFERENCES gyms(id);

-- Payments → Members
ALTER TABLE payments 
ADD CONSTRAINT fk_payments_member 
FOREIGN KEY (member_id) REFERENCES members(id);

-- Member Memberships → Gyms
ALTER TABLE member_memberships 
ADD CONSTRAINT fk_memberships_gym 
FOREIGN KEY (gym_id) REFERENCES gyms(id);

-- Member Memberships → Members
ALTER TABLE member_memberships 
ADD CONSTRAINT fk_memberships_member 
FOREIGN KEY (member_id) REFERENCES members(id);

-- Member Memberships → Membership Plans
ALTER TABLE member_memberships 
ADD CONSTRAINT fk_memberships_plan 
FOREIGN KEY (plan_id) REFERENCES membership_plans(id);

-- Trainers → Gyms
ALTER TABLE trainers 
ADD CONSTRAINT fk_trainers_gym 
FOREIGN KEY (gym_id) REFERENCES gyms(id);

-- Trainers → Users
ALTER TABLE trainers 
ADD CONSTRAINT fk_trainers_user 
FOREIGN KEY (user_id) REFERENCES users(id);

-- ==============================================================================
-- ADDITIONAL INDEXES FOR PERFORMANCE
-- ==============================================================================

-- Users: Index for role-based queries
CREATE INDEX idx_users_role ON users (role);

-- Users: Index for gym + role queries
CREATE INDEX idx_users_gym_role ON users (gym_id, role);

-- Members: Index for status queries
CREATE INDEX idx_members_status ON members (status);

-- Members: Index for gym + status queries
CREATE INDEX idx_members_gym_status ON members (gym_id, status);

-- Payments: Index for status queries
CREATE INDEX idx_payments_status ON payments (status);

-- Member Memberships: Index for expiry queries
CREATE INDEX idx_memberships_end_date ON member_memberships (end_date);

-- Member Memberships: Index for status queries
CREATE INDEX idx_memberships_status ON member_memberships (status);

-- ==============================================================================
-- NOTES:
-- ==============================================================================
-- 1. All FK constraints assume parent tables exist. Run in order.
-- 2. Code-level validation is still required for business rules:
--    - User role determines if gym_id should be NULL (ADMIN) or NOT NULL
--    - Only ONE ACTIVE membership per member per gym
--    - Email uniqueness per gym (handled by unique constraint + code validation)
-- 3. If any FK fails due to existing data, fix data first before adding constraint.
-- ==============================================================================

