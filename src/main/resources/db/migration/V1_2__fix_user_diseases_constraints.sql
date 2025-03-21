ALTER TABLE user_diseases
    DROP CONSTRAINT IF EXISTS uc_9842b53095f833576734fd0a1;

CREATE UNIQUE INDEX ux_user_diseases_user_disease_id
    ON user_diseases (account_id, disease_id)
    WHERE disease_id IS NOT NULL;

CREATE UNIQUE INDEX ux_user_diseases_user_custom_disease
    ON user_diseases (account_id, LOWER(custom_disease))
    WHERE custom_disease IS NOT NULL;