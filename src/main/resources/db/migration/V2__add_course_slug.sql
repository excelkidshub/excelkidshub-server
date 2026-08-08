-- V2: Add slug column to courses table
-- Slug maps a course URL path (e.g. "phonics/level-1") to a database course record.
-- Required for reading studio auth-guard to verify subscription by URL.

ALTER TABLE courses ADD COLUMN slug VARCHAR(255);
ALTER TABLE courses ADD CONSTRAINT uk_courses_slug UNIQUE (slug);
CREATE INDEX idx_courses_slug ON courses(slug);
