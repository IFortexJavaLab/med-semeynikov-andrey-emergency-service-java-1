ALTER TABLE user_allergies
    DROP CONSTRAINT IF EXISTS uc_b7d7601c4de2e2356e1ef1b16;

CREATE UNIQUE INDEX ux_user_allergies_user_allergy_id
    ON user_allergies (user_id, allergy_id)
    WHERE allergy_id IS NOT NULL;

CREATE UNIQUE INDEX ux_user_allergies_user_custom_allergy
    ON user_allergies (user_id, LOWER(custom_allergy))
    WHERE custom_allergy IS NOT NULL;