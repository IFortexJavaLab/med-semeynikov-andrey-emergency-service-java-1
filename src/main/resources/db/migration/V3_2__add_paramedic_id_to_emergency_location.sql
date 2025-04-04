ALTER TABLE paramedic_emergency_location
    ADD COLUMN paramedic_id UUID;

ALTER TABLE paramedic_emergency_location
    ALTER COLUMN paramedic_id SET NOT NULL;