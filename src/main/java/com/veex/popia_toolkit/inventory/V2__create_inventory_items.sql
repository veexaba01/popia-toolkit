CREATE TABLE inventory_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID         NOT NULL REFERENCES organisations (id),
    name                VARCHAR(200) NOT NULL,
    purpose             VARCHAR(500) NOT NULL,
    storage_location    VARCHAR(200) NOT NULL,
    special_information BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_inventory_items_organisation ON inventory_items (organisation_id);