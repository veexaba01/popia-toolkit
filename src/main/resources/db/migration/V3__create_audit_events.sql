CREATE TABLE audit_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID         NOT NULL REFERENCES organisations (id),
    actor_email     VARCHAR(254) NOT NULL,
    action          VARCHAR(50)  NOT NULL
        CHECK (action IN ('LOGIN_SUCCESS', 'LOGIN_FAILURE', 'STAFF_CREATED',
                          'INVENTORY_CREATED', 'INVENTORY_UPDATED', 'INVENTORY_DELETED')),
    detail          VARCHAR(500),
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_organisation_time ON audit_events (organisation_id, occurred_at DESC);