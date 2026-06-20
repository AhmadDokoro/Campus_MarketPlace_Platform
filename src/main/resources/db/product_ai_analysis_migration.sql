-- Product AI Analysis migration
-- ddl-auto=update will create this automatically; this script documents the schema and lets you
-- apply it manually when running with ddl-auto=validate.
--
-- Stores the supporting metadata for the AI scam-detection verdict. The verdict itself
-- (VERIFIED / SUSPICIOUS / UNKNOWN) stays exclusively in products.flagged_status — this table
-- holds ONLY the confidence score and the list of reasons. No status, risk level, or
-- recommendation is duplicated here. One product maps to at most one analysis row.

CREATE TABLE IF NOT EXISTS product_ai_analysis (
    analysis_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    confidence_score INT NULL,                 -- 0-100 percentage
    reasons          TEXT NULL,                -- JSON array of strings, e.g. ["Price too low", "..."]
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_ai_analysis_product UNIQUE (product_id),
    CONSTRAINT fk_ai_analysis_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);
