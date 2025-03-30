DROP TABLE emergency_assignment;

CREATE TABLE emergency_assignment
(
    id                   UUID PRIMARY KEY,
    emergency_id         UUID                     NOT NULL,
    paramedic_id         UUID                     NOT NULL,

    assigned_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    canceled_at          TIMESTAMP WITH TIME ZONE,

    cancellation_reason  VARCHAR(64),
    cancellation_comment TEXT,

    CONSTRAINT fk_emergency FOREIGN KEY (emergency_id) REFERENCES emergency (id)
);
