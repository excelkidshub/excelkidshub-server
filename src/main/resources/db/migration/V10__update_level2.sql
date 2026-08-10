-- Update Level 2 course with correct total lessons
-- Based on preliminary curriculum analysis: 60 pages
-- Note: This should be verified against the actual source book before production deployment

UPDATE courses 
SET total_lessons = 60
WHERE slug = 'phonics/level-2';

-- Verify the update was applied
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM courses 
        WHERE slug = 'phonics/level-2' AND total_lessons = 60
    ) THEN
        RAISE EXCEPTION 'Failed to update phonics/level-2 total_lessons to 60';
    END IF;
END $$;
