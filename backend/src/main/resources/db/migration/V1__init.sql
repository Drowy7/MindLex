-- MindLex: начальная схема базы данных.
-- Миграция выполняется Flyway как отдельный административный процесс (фактор XII).

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(120) NOT NULL,
    role          VARCHAR(32)  NOT NULL DEFAULT 'ROLE_USER',
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE decks (
    id          UUID PRIMARY KEY,
    owner_id    UUID REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(160) NOT NULL,
    language    VARCHAR(8)   NOT NULL DEFAULT 'en',
    description VARCHAR(500),
    is_public   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_decks_owner ON decks (owner_id);

CREATE TABLE words (
    id             UUID PRIMARY KEY,
    deck_id        UUID         NOT NULL REFERENCES decks (id) ON DELETE CASCADE,
    term           VARCHAR(160) NOT NULL,
    translation    VARCHAR(255) NOT NULL,
    transcription  VARCHAR(160),
    part_of_speech VARCHAR(32),
    example        VARCHAR(500),
    alternatives   TEXT,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_words_deck ON words (deck_id);

CREATE TABLE cloze_exercises (
    id         UUID PRIMARY KEY,
    word_id    UUID         NOT NULL REFERENCES words (id) ON DELETE CASCADE,
    sentence   VARCHAR(600) NOT NULL,
    answer     VARCHAR(160) NOT NULL,
    hint       VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_cloze_word ON cloze_exercises (word_id);

CREATE TABLE synonym_chains (
    id         UUID PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    language   VARCHAR(8)   NOT NULL DEFAULT 'en',
    category   VARCHAR(80),
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL
);

CREATE TABLE synonym_chain_steps (
    id                 UUID PRIMARY KEY,
    chain_id           UUID         NOT NULL REFERENCES synonym_chains (id) ON DELETE CASCADE,
    step_number        INT          NOT NULL,
    prompt_word        VARCHAR(160) NOT NULL,
    accepted_synonyms  TEXT         NOT NULL,
    hint               VARCHAR(255),
    CONSTRAINT uq_chain_step UNIQUE (chain_id, step_number)
);

CREATE TABLE word_progress (
    id                UUID PRIMARY KEY,
    user_id           UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    word_id           UUID    NOT NULL REFERENCES words (id) ON DELETE CASCADE,
    status            VARCHAR(16) NOT NULL,
    level             INT     NOT NULL DEFAULT 0,
    ease_factor       DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days     INT     NOT NULL DEFAULT 0,
    next_review_at    TIMESTAMPTZ NOT NULL,
    last_reviewed_at  TIMESTAMPTZ,
    correct_count     INT     NOT NULL DEFAULT 0,
    incorrect_count   INT     NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_progress_user_word UNIQUE (user_id, word_id)
);
CREATE INDEX idx_progress_due ON word_progress (user_id, next_review_at);
CREATE INDEX idx_progress_status ON word_progress (user_id, status);

CREATE TABLE study_sessions (
    id               UUID PRIMARY KEY,
    user_id          UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    deck_id          UUID        REFERENCES decks (id) ON DELETE SET NULL,
    mechanic         VARCHAR(24) NOT NULL,
    status           VARCHAR(16) NOT NULL,
    duration_seconds INT         NOT NULL,
    -- 0 — количество вопросов не ограничено (используется в Timed Rush)
    question_limit   INT         NOT NULL DEFAULT 0,
    score            INT         NOT NULL DEFAULT 0,
    combo_streak     INT         NOT NULL DEFAULT 0,
    max_combo        INT         NOT NULL DEFAULT 0,
    correct_count    INT         NOT NULL DEFAULT 0,
    incorrect_count  INT         NOT NULL DEFAULT 0,
    skip_count       INT         NOT NULL DEFAULT 0,
    started_at       TIMESTAMPTZ NOT NULL,
    finished_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_sessions_user ON study_sessions (user_id, started_at DESC);

-- Журнал ответов: не только аналитика, но и отдельная CRUD-сущность домена.
CREATE TABLE review_logs (
    id               UUID PRIMARY KEY,
    user_id          UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    word_id          UUID        REFERENCES words (id) ON DELETE SET NULL,
    -- идентификатор заданного вопроса: слово, упражнение cloze или шаг цепочки
    question_ref     UUID,
    session_id       UUID        REFERENCES study_sessions (id) ON DELETE SET NULL,
    mechanic         VARCHAR(24) NOT NULL,
    quality          INT         NOT NULL,
    answer           TEXT,
    expected_answer  VARCHAR(255),
    correct          BOOLEAN     NOT NULL,
    response_time_ms BIGINT      NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_review_logs_user ON review_logs (user_id, created_at DESC);

CREATE TABLE user_settings (
    user_id               UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    daily_goal            INT         NOT NULL DEFAULT 15,
    language              VARCHAR(8)  NOT NULL DEFAULT 'en',
    selected_deck_id      UUID        REFERENCES decks (id) ON DELETE SET NULL,
    preferred_time        TIME        NOT NULL DEFAULT '19:00',
    rush_seconds          INT         NOT NULL DEFAULT 90,
    cloze_seconds         INT         NOT NULL DEFAULT 35,
    notifications_enabled BOOLEAN     NOT NULL DEFAULT TRUE,
    current_streak        INT         NOT NULL DEFAULT 0,
    last_study_date       DATE,
    best_rush_score       INT         NOT NULL DEFAULT 0,
    max_combo_record      INT         NOT NULL DEFAULT 0,
    timezone              VARCHAR(64) NOT NULL DEFAULT 'UTC',
    updated_at            TIMESTAMPTZ NOT NULL
);

CREATE TABLE study_reminders (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    planned_at TIMESTAMPTZ  NOT NULL,
    title      VARCHAR(160) NOT NULL,
    body       VARCHAR(400),
    sent       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_reminders_user_time ON study_reminders (user_id, planned_at);
