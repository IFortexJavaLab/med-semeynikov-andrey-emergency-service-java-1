CREATE TABLE cancellation_reason
(
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,
    description      TEXT        NOT NULL,
    requires_comment BOOLEAN     NOT NULL DEFAULT false
);

INSERT INTO cancellation_reason (code, description, requires_comment)
VALUES ('LOCATION_NOT_REACHABLE', 'Location not reachable', true),
       ('MUNICIPAL_SERVICES_PROVIDED', 'Municipal emergency services provided', false),
       ('ACCIDENT_ON_WAY', 'Accident on a way', false),
       ('BLOCKED_BY_OTHERS', 'The way to an emergency is blocked by actions of other persons', true);

ALTER TABLE emergency_assignment
    DROP CONSTRAINT IF EXISTS fk_cancellation_reason;

ALTER TABLE emergency_assignment
    ALTER COLUMN cancellation_reason_id TYPE BIGINT;

ALTER TABLE emergency_assignment
    ADD CONSTRAINT fk_cancellation_reason
        FOREIGN KEY (cancellation_reason_id) REFERENCES cancellation_reason (id);


CREATE TABLE emergency_resolution
(
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,
    description      TEXT        NOT NULL,
    requires_comment BOOLEAN     NOT NULL DEFAULT false
);

INSERT INTO emergency_resolution (code, description, requires_comment)
VALUES ('HOSPITALIZED_WITH_CONSENT', 'Hospitalized with consent', false),
       ('HOSPITALIZED_UNCONSCIOUS', 'Hospitalized unconscious', false),
       ('FIRST_AID_NO_HOSPITALIZATION', 'First aid provided, no hospitalization', false),
       ('NO_HELP_NEEDED', 'No help needed', true),
       ('FALSE_ALARM', 'False alarm', true);

ALTER TABLE emergency
    DROP CONSTRAINT IF EXISTS fk_emergency_resolution;

ALTER TABLE emergency
    ALTER COLUMN resolution_id TYPE BIGINT;

ALTER TABLE emergency
    ADD CONSTRAINT fk_emergency_resolution
        FOREIGN KEY (resolution_id) REFERENCES emergency_resolution (id);

