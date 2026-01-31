-- Add gym_id column to membership_plans table
ALTER TABLE membership_plans
ADD COLUMN gym_id BIGINT;

-- Add foreign key constraint
ALTER TABLE membership_plans
ADD CONSTRAINT fk_membership_plans_gym
FOREIGN KEY (gym_id) REFERENCES gyms(id)
ON DELETE SET NULL;

-- Create index for faster queries
CREATE INDEX idx_membership_plans_gym_id ON membership_plans(gym_id);

