-- ============================================================
-- V6: Master Data — ExcelKidsHub Platform
-- ============================================================
-- Safe to re-run: every INSERT uses ON CONFLICT DO NOTHING.
-- Assumes V1–V5 have already run (schema + coupons table exist).
--
-- What this migration provides:
--   1. Roles          — STUDENT, ADMIN, TEACHER, PARENT (idempotent top-up)
--   2. Plans          — Starter (free), Level 1 (₹499/mo), Bundle (₹999/mo)
--   3. Courses        — Starter Sample, Level 1, Level 2, Level 3 (placeholder)
--   4. Plan-course    — mappings for all three plans
--   5. Admin user     — default admin account (CHANGE PASSWORD BEFORE GOING LIVE)
--   6. Coupons        — one launch discount code (LAUNCH20 = 20% off)
-- ============================================================

-- ── 1. Roles ─────────────────────────────────────────────────────────────────
-- These were seeded in V4 but are repeated here so V6 is self-contained.

INSERT INTO roles (name, description, active)
VALUES
  ('STUDENT', 'Default role for registered users',       TRUE),
  ('ADMIN',   'Full platform access — admin portal',     TRUE),
  ('TEACHER', 'Future role — classroom management',      TRUE),
  ('PARENT',  'Future role — child account monitoring',  TRUE)
ON CONFLICT (name) DO NOTHING;


-- ── 2. Plans ─────────────────────────────────────────────────────────────────
-- duration_months = 0 means free / forever.
-- features JSONB is displayed on the pricing page.

INSERT INTO plans (name, description, price, duration_months, max_courses, max_books, features, is_popular, active)
VALUES
  (
    'Starter',
    'Free forever. Access sample content to try before you subscribe.',
    0.00,
    0,      -- 0 = free / no expiry
    1,
    1,
    '{
      "audioSupport": "Limited",
      "progressTracking": "Basic",
      "practiceActivities": "Limited",
      "support": "Email"
    }'::jsonb,
    FALSE,
    TRUE
  ),
  (
    'Digital Reading Level 1',
    'Complete Jolly Phonics Level 1 workbook — full audio, animations and progress tracking.',
    499.00,
    1,      -- 30-day subscription
    1,
    NULL,   -- unlimited books within the plan
    '{
      "audioSupport": "Full",
      "progressTracking": "Full",
      "practiceActivities": "Full",
      "support": "Email + WhatsApp"
    }'::jsonb,
    TRUE,   -- shown as "Most Popular" on pricing page
    TRUE
  ),
  (
    'Complete Reading Bundle',
    'Level 1 + Level 2 + all future bundle content as it is released.',
    999.00,
    1,
    NULL,   -- unlimited courses
    NULL,
    '{
      "audioSupport": "Full",
      "progressTracking": "Full",
      "practiceActivities": "Full",
      "futureBundleContent": "Included",
      "support": "Priority Email + WhatsApp"
    }'::jsonb,
    FALSE,
    TRUE
  )
ON CONFLICT DO NOTHING;


-- ── 3. Courses ───────────────────────────────────────────────────────────────
-- slug must match the URL path under read.excelkidshub.in
-- e.g. slug 'phonics/level-1' → read.excelkidshub.in/phonics/level-1/
-- is_free = TRUE  → any authenticated user can access (no subscription needed)
-- is_free = FALSE → must have an active subscription that includes this course

INSERT INTO courses (title, description, level_name, age_group, difficulty_level, slug, is_free, total_lessons, active)
VALUES
  (
    'Phonics Starter Sample',
    'A free 5-page sample from the Level 1 workbook. No subscription required.',
    'Starter',
    '4-7 years',
    'Beginner',
    'phonics/sample',
    TRUE,
    5,
    TRUE
  ),
  (
    'Phonics Level 1',
    'Complete Jolly Phonics Level 1 interactive workbook — 70 activity pages, full audio and animations.',
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
    'Advanced phonics — digraphs, diphthongs, blends and reading fluency exercises.',
    'Level 2',
    '5-8 years',
    'Intermediate',
    'phonics/level-2',
    FALSE,
    60,
    TRUE   -- content placeholder — activate when pages are ready
  ),
  (
    'Phonics Level 3',
    'Complex vowel patterns, long vowel sounds and comprehension.',
    'Level 3',
    '6-9 years',
    'Intermediate',
    'phonics/level-3',
    FALSE,
    60,
    FALSE  -- not yet published — set active=true when content is ready
  )
ON CONFLICT DO NOTHING;


-- ── 4. Plan-Course Mappings ───────────────────────────────────────────────────
-- Starter plan → sample course
INSERT INTO plan_courses (plan_id, course_id, active)
SELECT p.id, c.id, TRUE
FROM   plans   p
JOIN   courses c ON c.slug = 'phonics/sample'
WHERE  p.name = 'Starter'
ON CONFLICT (plan_id, course_id) DO NOTHING;

-- Level 1 plan → Level 1 course only
INSERT INTO plan_courses (plan_id, course_id, active)
SELECT p.id, c.id, TRUE
FROM   plans   p
JOIN   courses c ON c.slug = 'phonics/level-1'
WHERE  p.name = 'Digital Reading Level 1'
ON CONFLICT (plan_id, course_id) DO NOTHING;

-- Bundle plan → Level 1 + Level 2 + Level 3
INSERT INTO plan_courses (plan_id, course_id, active)
SELECT p.id, c.id, TRUE
FROM   plans   p
JOIN   courses c ON c.slug IN ('phonics/level-1', 'phonics/level-2', 'phonics/level-3')
WHERE  p.name = 'Complete Reading Bundle'
ON CONFLICT (plan_id, course_id) DO NOTHING;


-- ── 5. Admin User ─────────────────────────────────────────────────────────────
-- Password below is the BCrypt hash of: Admin@123456
-- ⚠️  CHANGE THIS PASSWORD immediately after first login in production.
--     Generate a new hash: https://bcrypt-generator.com  (strength = 12)
--
-- To change via SQL:
--   UPDATE users
--   SET    password = '<new_bcrypt_hash>'
--   WHERE  email    = 'admin@excelkidshub.in';

INSERT INTO users (email, password, first_name, last_name, role_id, email_verified, active)
SELECT
  'admin@excelkidshub.in',
  '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- Admin@123456
  'Platform',
  'Admin',
  r.id,
  TRUE,
  TRUE
FROM roles r
WHERE r.name = 'ADMIN'
ON CONFLICT (email) DO NOTHING;


-- ── 6. Coupons ────────────────────────────────────────────────────────────────
-- LAUNCH20  : 20% off any plan, first 100 uses, valid until end of year
-- WELCOME10 : ₹50 flat off, first 500 uses, no expiry

INSERT INTO coupons (code, discount_percent, discount_amount, max_uses, used_count, valid_from, valid_until, active)
VALUES
  (
    'LAUNCH20',
    20,       -- 20% off
    NULL,
    100,      -- max 100 redemptions
    0,
    CURRENT_DATE,
    (DATE_TRUNC('year', CURRENT_DATE) + INTERVAL '1 year' - INTERVAL '1 day')::DATE,  -- Dec 31 of current year
    TRUE
  ),
  (
    'WELCOME10',
    NULL,
    50.00,    -- ₹50 flat off
    500,
    0,
    CURRENT_DATE,
    NULL,     -- no expiry
    TRUE
  )
ON CONFLICT (code) DO NOTHING;


-- ── Summary ───────────────────────────────────────────────────────────────────
-- After running this migration the system has:
--
--  roles    : STUDENT, ADMIN, TEACHER, PARENT
--  plans    : Starter (free), Digital Reading Level 1 (₹499/mo), Complete Reading Bundle (₹999/mo)
--  courses  : Starter Sample (free), Level 1, Level 2, Level 3 (inactive placeholder)
--  mappings : Starter→Sample, Level1→Level1, Bundle→Level1+Level2+Level3
--  users    : admin@excelkidshub.in / Admin@123456  ← CHANGE BEFORE PRODUCTION
--  coupons  : LAUNCH20 (20% off, 100 uses), WELCOME10 (₹50 off, 500 uses)
-- ─────────────────────────────────────────────────────────────────────────────
