-- Translated from: backend/alembic/versions/0001_initial_schema.py
-- PostgreSQL enum types

CREATE TYPE user_role AS ENUM ('analyst', 'manager');
CREATE TYPE task_status AS ENUM ('pending', 'in_progress', 'submitted', 'approved', 'rejected', 'completed');
CREATE TYPE task_priority AS ENUM ('low', 'medium', 'high');
CREATE TYPE approval_decision AS ENUM ('approved', 'rejected');
CREATE TYPE audit_action AS ENUM (
    'login', 'create_task', 'update_task', 'delete_task',
    'submit_task', 'approve_task', 'reject_task', 'complete_task'
);

-- Users

CREATE TABLE users (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(120)                  NOT NULL,
    email            VARCHAR(255)                  NOT NULL UNIQUE,
    hashed_password  VARCHAR(255)                  NOT NULL,
    role             user_role                     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE      NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITH TIME ZONE      NOT NULL DEFAULT now()
);

CREATE INDEX ix_users_email ON users (email);
CREATE INDEX ix_users_role  ON users (role);

-- Tasks

CREATE TABLE tasks (
    id                   BIGSERIAL PRIMARY KEY,
    title                VARCHAR(200)              NOT NULL,
    description          TEXT                      NOT NULL DEFAULT '',
    status               task_status               NOT NULL DEFAULT 'pending',
    priority             task_priority             NOT NULL DEFAULT 'medium',
    assigned_to_user_id  BIGINT                   NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    created_by_user_id   BIGINT                   NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    created_at           TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now()
);

CREATE INDEX ix_tasks_status              ON tasks (status);
CREATE INDEX ix_tasks_priority            ON tasks (priority);
CREATE INDEX ix_tasks_assigned_to_user_id ON tasks (assigned_to_user_id);
CREATE INDEX ix_tasks_created_by_user_id  ON tasks (created_by_user_id);
CREATE INDEX ix_tasks_status_priority     ON tasks (status, priority);

-- Workflow events

CREATE TABLE workflow_events (
    id                   BIGSERIAL PRIMARY KEY,
    task_id              BIGINT                   NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    from_status          task_status,
    to_status            task_status               NOT NULL,
    changed_by_user_id   BIGINT                   NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    note                 VARCHAR(500),
    created_at           TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now()
);

CREATE INDEX ix_workflow_events_task_id            ON workflow_events (task_id);
CREATE INDEX ix_workflow_events_changed_by_user_id ON workflow_events (changed_by_user_id);

-- Approvals

CREATE TABLE approvals (
    id                   BIGSERIAL PRIMARY KEY,
    task_id              BIGINT                   NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    approved_by_user_id  BIGINT                   NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    decision             approval_decision         NOT NULL,
    comment              VARCHAR(500),
    created_at           TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now()
);

CREATE INDEX ix_approvals_task_id             ON approvals (task_id);
CREATE INDEX ix_approvals_approved_by_user_id ON approvals (approved_by_user_id);
CREATE INDEX ix_approvals_decision            ON approvals (decision);

-- Audit logs

CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT                   REFERENCES users (id) ON DELETE SET NULL,
    action       audit_action              NOT NULL,
    entity_type  VARCHAR(50)               NOT NULL,
    entity_id    BIGINT,
    details      TEXT,
    created_at   TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now()
);

CREATE INDEX ix_audit_logs_user_id     ON audit_logs (user_id);
CREATE INDEX ix_audit_logs_action      ON audit_logs (action);
CREATE INDEX ix_audit_logs_entity_type ON audit_logs (entity_type);
CREATE INDEX ix_audit_logs_entity_id   ON audit_logs (entity_id);
CREATE INDEX ix_audit_logs_created_at  ON audit_logs (created_at);
