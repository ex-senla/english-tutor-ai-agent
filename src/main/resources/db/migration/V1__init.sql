-- dictionary (модуль dictionary)
CREATE TABLE dictionary (
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE word_info (
    id                  UUID PRIMARY KEY,
    dictionary_id       UUID NOT NULL REFERENCES dictionary (id) ON DELETE CASCADE,
    word_value          VARCHAR(255) NOT NULL,
    part_of_speech      VARCHAR(32)  NOT NULL,
    target_repetitions  INT NOT NULL,
    current_repetitions INT NOT NULL DEFAULT 0,
    reset_count         INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_word_info_dictionary_word
    ON word_info (dictionary_id, lower(word_value));

CREATE TABLE word_info_translation (
    word_info_id UUID NOT NULL REFERENCES word_info (id) ON DELETE CASCADE,
    translation  VARCHAR(255) NOT NULL,
    PRIMARY KEY (word_info_id, translation)
);

-- student (модуль student)
CREATE TABLE student (
    id            UUID PRIMARY KEY,
    dictionary_id UUID NOT NULL,
    name          VARCHAR(255) NOT NULL,
    cefr_level    VARCHAR(8)   NOT NULL,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE lesson (
    id         UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES student (id),
    name       VARCHAR(255) NOT NULL,
    status     VARCHAR(16)  NOT NULL,
    started_at TIMESTAMPTZ  NOT NULL,
    ended_at   TIMESTAMPTZ
);

CREATE TABLE lesson_word_info (
    lesson_id    UUID NOT NULL REFERENCES lesson (id) ON DELETE CASCADE,
    word_info_id UUID NOT NULL,
    PRIMARY KEY (lesson_id, word_info_id)
);

-- teacher (модуль teacher)
CREATE TABLE teacher (
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE teacher_identifier (
    teacher_id       UUID NOT NULL REFERENCES teacher (id) ON DELETE CASCADE,
    identifier_type  VARCHAR(32)  NOT NULL,
    identifier_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, identifier_type)
);

CREATE UNIQUE INDEX uq_teacher_identifier_value
    ON teacher_identifier (identifier_type, identifier_value);

CREATE TABLE teacher_student (
    teacher_id UUID NOT NULL REFERENCES teacher (id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    PRIMARY KEY (teacher_id, student_id)
);

-- exercise (модуль exercise)
CREATE TABLE exercise (
    id              UUID PRIMARY KEY,
    exercise_type   VARCHAR(32)  NOT NULL,
    topic           VARCHAR(255) NOT NULL,
    thematic_topic  VARCHAR(255),
    status          VARCHAR(16)  NOT NULL,
    content         TEXT,
    expected_answers JSONB NOT NULL DEFAULT '[]'::jsonb,
    items            JSONB NOT NULL DEFAULT '[]'::jsonb
);

CREATE TABLE exercise_word_info (
    exercise_id  UUID NOT NULL REFERENCES exercise (id) ON DELETE CASCADE,
    word_info_id UUID NOT NULL,
    PRIMARY KEY (exercise_id, word_info_id)
);

-- chatbot (модуль chatbot)
CREATE TABLE chat (
    chat_id BIGINT PRIMARY KEY,
    state   VARCHAR(64) NOT NULL,
    context JSONB NOT NULL DEFAULT '{}'::jsonb
);
