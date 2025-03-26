CREATE TABLE IF NOT EXISTS emergency
(
    id                     UUID PRIMARY KEY,
    client_id              UUID                                      NOT NULL,
    paramedic_id           UUID,
    status                 VARCHAR(50),
    resolution             VARCHAR(50),
    resolution_explanation TEXT,
    created_at             TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    updated_at             TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS emergency_feedback
(
    id           UUID PRIMARY KEY,
    emergency_id UUID                                      NOT NULL UNIQUE,
    grade        INTEGER                                   NOT NULL CHECK (grade >= 1 AND grade <= 5),
    comment      TEXT,
    created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    CONSTRAINT fk_feedback_emergency FOREIGN KEY (emergency_id)
        REFERENCES emergency (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS emergency_location
(
    id            UUID PRIMARY KEY,
    emergency_id  UUID                                      NOT NULL,
    location_type VARCHAR(50)                               NOT NULL,
    latitude      NUMERIC(10, 6)                            NOT NULL,
    longitude     NUMERIC(10, 6)                            NOT NULL,
    timestamp     TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    CONSTRAINT fk_location_emergency FOREIGN KEY (emergency_id)
        REFERENCES emergency (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS emergency_symptom
(
    id           UUID PRIMARY KEY,
    emergency_id UUID                                      NOT NULL,
    symptom_id   UUID                                      NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    CONSTRAINT fk_symptom_emergency FOREIGN KEY (emergency_id)
        REFERENCES emergency (id) ON DELETE CASCADE,
    CONSTRAINT fk_symptom_reference FOREIGN KEY (symptom_id)
        REFERENCES symptoms (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS emergency_assignment
(
    id           UUID PRIMARY KEY,
    emergency_id UUID                                      NOT NULL,
    paramedic_id UUID                                      NOT NULL,
    assigned_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    CONSTRAINT fk_assignment_emergency FOREIGN KEY (emergency_id)
        REFERENCES emergency (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS emergency_cancellation
(
    id           UUID PRIMARY KEY,
    emergency_id UUID                                      NOT NULL,
    paramedic_id UUID                                      NOT NULL,
    reason       TEXT,
    canceled_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL,
    CONSTRAINT fk_cancellation_emergency FOREIGN KEY (emergency_id)
        REFERENCES emergency (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS paramedic_location
(
    paramedic_id UUID PRIMARY KEY,
    latitude     NUMERIC(10, 6)                            NOT NULL,
    longitude    NUMERIC(10, 6)                            NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT now() NOT NULL
);
