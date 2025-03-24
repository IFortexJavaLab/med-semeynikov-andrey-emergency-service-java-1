CREATE TABLE allergies
(
    id         UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_allergies PRIMARY KEY (id)
);

CREATE TABLE diseases
(
    id         UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_diseases PRIMARY KEY (id)
);

CREATE TABLE symptoms
(
    id            UUID                        NOT NULL,
    name          VARCHAR(255)                NOT NULL,
    type          VARCHAR(255)                NOT NULL,
    advice        TEXT,
    animation_key VARCHAR(255),
    parent_id     UUID,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_symptoms PRIMARY KEY (id)
);

CREATE TABLE user_allergies
(
    id             UUID                        NOT NULL,
    user_id        UUID,
    allergy_id     UUID,
    custom_allergy VARCHAR(255),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user_allergies PRIMARY KEY (id)
);

CREATE TABLE user_diseases
(
    id             UUID                        NOT NULL,
    user_id        UUID,
    disease_id     UUID,
    custom_disease VARCHAR(255),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user_diseases PRIMARY KEY (id)
);

ALTER TABLE allergies
    ADD CONSTRAINT uc_allergies_name UNIQUE (name);

ALTER TABLE diseases
    ADD CONSTRAINT uc_diseases_name UNIQUE (name);

ALTER TABLE symptoms
    ADD CONSTRAINT uc_symptoms_name UNIQUE (name);

ALTER TABLE symptoms
    ADD CONSTRAINT FK_SYMPTOMS_ON_PARENT FOREIGN KEY (parent_id) REFERENCES symptoms (id);

ALTER TABLE user_allergies
    ADD CONSTRAINT FK_USER_ALLERGIES_ON_ALLERGY FOREIGN KEY (allergy_id) REFERENCES allergies (id);

ALTER TABLE user_diseases
    ADD CONSTRAINT FK_USER_DISEASES_ON_DISEASE FOREIGN KEY (disease_id) REFERENCES diseases (id);

CREATE UNIQUE INDEX ux_user_allergies_user_allergy_id
    ON user_allergies (user_id, allergy_id)
    WHERE allergy_id IS NOT NULL;

CREATE UNIQUE INDEX ux_user_allergies_user_custom_allergy
    ON user_allergies (user_id, LOWER(custom_allergy))
    WHERE custom_allergy IS NOT NULL;

CREATE UNIQUE INDEX ux_user_diseases_user_disease_id
    ON user_diseases (user_id, disease_id)
    WHERE disease_id IS NOT NULL;

CREATE UNIQUE INDEX ux_user_diseases_user_custom_disease
    ON user_diseases (user_id, LOWER(custom_disease))
    WHERE custom_disease IS NOT NULL;