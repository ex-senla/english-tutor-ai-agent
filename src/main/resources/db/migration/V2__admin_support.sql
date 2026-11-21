-- teacher (модуль teacher)
ALTER TABLE teacher ADD COLUMN admin BOOLEAN NOT NULL DEFAULT FALSE;

-- support (модуль support); teacher_id — логическая ссылка без FK (конвенция V1)
CREATE TABLE support_thread (
    id              UUID PRIMARY KEY,
    teacher_id      UUID NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    last_message_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_support_thread_teacher_open
    ON support_thread (teacher_id) WHERE status = 'OPEN';

CREATE TABLE support_message (
    id         UUID PRIMARY KEY,
    thread_id  UUID NOT NULL REFERENCES support_thread (id) ON DELETE CASCADE,
    author     VARCHAR(16) NOT NULL,
    text       TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_support_message_thread ON support_message (thread_id, created_at);
