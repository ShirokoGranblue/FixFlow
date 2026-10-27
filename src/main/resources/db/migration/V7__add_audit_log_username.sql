ALTER TABLE audit_log
    ADD COLUMN username VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL AFTER user_id;
