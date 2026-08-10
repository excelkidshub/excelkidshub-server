-- Add BaseEntity columns to activity_progress table
-- These columns are required by BaseEntity but were missing in V7

ALTER TABLE activity_progress 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
