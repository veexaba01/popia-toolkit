ALTER TABLE audit_events DROP CONSTRAINT audit_events_action_check;

ALTER TABLE audit_events ADD CONSTRAINT audit_events_action_check
    CHECK (action IN ('LOGIN_SUCCESS', 'LOGIN_FAILURE', 'STAFF_CREATED',
                      'INVENTORY_CREATED', 'INVENTORY_UPDATED', 'INVENTORY_DELETED',
                      'PASSWORD_CHANGED'));