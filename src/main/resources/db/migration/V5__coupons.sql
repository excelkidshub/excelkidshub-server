-- V5: Coupons table for discount codes on subscription plans.
-- discount_percent and discount_amount are mutually exclusive — use one.

CREATE TABLE coupons (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(50)    NOT NULL UNIQUE,
    discount_percent  INTEGER,
    discount_amount   DECIMAL(10,2),
    max_uses          INTEGER,
    used_count        INTEGER        NOT NULL DEFAULT 0,
    valid_from        DATE,
    valid_until       DATE,
    active            BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_coupons_code   ON coupons(code);
CREATE INDEX idx_coupons_active ON coupons(active);

CREATE TRIGGER update_coupons_updated_at BEFORE UPDATE ON coupons
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
