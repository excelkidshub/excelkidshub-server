-- Activity Progress Tracking
-- Records completion of practice activities, games, and assessments
-- These do not have page numbers - they are tracked by activity type and ID

CREATE TABLE IF NOT EXISTS activity_progress (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    course_id           BIGINT,
    activity_type       VARCHAR(50) NOT NULL,   -- PRACTICE, GAME, ASSESSMENT, SONGS
    activity_id         VARCHAR(100) NOT NULL,  -- e.g. "sound-match-group-1", "word-builder-s"
    score               INTEGER,               -- 0-100, nullable
    completed           BOOLEAN DEFAULT FALSE,
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_activity_user ON activity_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_course ON activity_progress(course_id);
CREATE INDEX IF NOT EXISTS idx_activity_type ON activity_progress(activity_type);
CREATE INDEX IF NOT EXISTS idx_activity_user_type ON activity_progress(user_id, activity_type);
CREATE INDEX IF NOT EXISTS idx_activity_completed ON activity_progress(completed);

-- Add updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_activity_progress_updated_at
    BEFORE UPDATE ON activity_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
