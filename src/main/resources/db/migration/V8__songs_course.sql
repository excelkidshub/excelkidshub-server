-- Add Jolly Phonics Songs course
INSERT INTO courses (title, description, level_name, slug, is_free, total_lessons, active, created_at, updated_at)
VALUES (
  'Jolly Phonics Songs',
  'Official Jolly Phonics song videos — Groups 1 to 7. Learn the sounds with songs and actions!',
  'Songs',
  'phonics/songs',
  true,
  7,
  true,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
