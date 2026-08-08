-- V4: Seed reference data — roles, plans, courses, plan-course mappings.
-- Safe to re-run: uses INSERT ... ON CONFLICT DO NOTHING.

-- ── Roles ────────────────────────────────────────────────────────────────────
INSERT INTO roles (name, description, active)
VALUES
  ('STUDENT', 'Default role for registered users',        TRUE),
  ('ADMIN',   'Full platform access',                     TRUE),
  ('TEACHER', 'Future role — classroom management',       TRUE),
  ('PARENT',  'Future role — child account monitoring',   TRUE)
ON CONFLICT (name) DO NOTHING;

-- ── Plans ────────────────────────────────────────────────────────────────────
INSERT INTO plans (name, description, price, duration_months, max_courses, max_books, features, is_popular, active)
VALUES
  (
    'Starter',
    'Free forever plan with sample content.',
    0.00,
    0,
    1,
    1,
    '{"audioSupport": "limited", "progressTracking": "basic", "practiceActivities": "limited"}',
    FALSE,
    TRUE
  ),
  (
    'Digital Reading Level 1',
    'Complete Level 1 phonics workbook with full audio and progress tracking.',
    499.00,
    1,
    1,
    NULL,
    '{"audioSupport": "full", "progressTracking": "full", "practiceActivities": "full"}',
    TRUE,
    TRUE
  ),
  (
    'Complete Reading Bundle',
    'Level 1, Level 2, Stories and all future bundle content.',
    999.00,
    1,
    NULL,
    NULL,
    '{"audioSupport": "full", "progressTracking": "full", "practiceActivities": "full", "futureBundleContent": true}',
    FALSE,
    TRUE
  )
ON CONFLICT DO NOTHING;

-- ── Courses ──────────────────────────────────────────────────────────────────
INSERT INTO courses (title, description, level_name, age_group, difficulty_level, slug, is_free, total_lessons, active)
VALUES
  (
    'Phonics Level 1 — Starter Sample',
    'Free sample of the Level 1 phonics workbook.',
    'Level 1',
    '4-7 years',
    'Beginner',
    'phonics/sample',
    TRUE,
    5,
    TRUE
  ),
  (
    'Phonics Level 1',
    'Complete Jolly Phonics Level 1 interactive workbook — 70 pages, audio, activities.',
    'Level 1',
    '4-7 years',
    'Beginner',
    'phonics/level-1',
    FALSE,
    70,
    TRUE
  ),
  (
    'Phonics Level 2',
    'Advanced phonics — digraphs, diphthongs and reading fluency.',
    'Level 2',
    '5-8 years',
    'Intermediate',
    'phonics/level-2',
    FALSE,
    60,
    TRUE
  )
ON CONFLICT DO NOTHING;

-- ── Plan-Course Mappings ──────────────────────────────────────────────────────
-- Starter plan → sample course only
INSERT INTO plan_courses (plan_id, course_id, active)
SELECT p.id, c.id, TRUE
FROM   plans p, courses c
WHERE  p.name = 'Starter'
  AND  c.slug = 'phonics/sample'
ON CONFLICT (plan_id, course_id) DO NOTHING;

-- Level 1 plan → Level 1 course
INSERT INTO plan_courses (plan_id, course_id, active)
SELECT p.id, c.id, TRUE
FROM   plans p, courses c
WHERE  p.name = 'Digital Reading Level 1'
  AND  c.slug = 'phonics/level-1'
ON CONFLICT (plan_id, course_id) DO NOTHING;

-- Bundle plan → Level 1 + Level 2
INSERT INTO plan_courses (plan_id, course_id, active)
SELECT p.id, c.id, TRUE
FROM   plans p, courses c
WHERE  p.name = 'Complete Reading Bundle'
  AND  c.slug IN ('phonics/level-1', 'phonics/level-2')
ON CONFLICT (plan_id, course_id) DO NOTHING;
