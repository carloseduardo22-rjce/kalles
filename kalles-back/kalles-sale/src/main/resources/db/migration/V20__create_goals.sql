CREATE TABLE goals (
    id           UUID            NOT NULL,
    version      BIGINT          NOT NULL DEFAULT 0,
    target_value NUMERIC(19, 2)  NOT NULL,
    periodicity  VARCHAR(10)     NOT NULL,
    start_date   DATE            NOT NULL,
    end_date     DATE            NOT NULL,
    status       VARCHAR(10)     NOT NULL DEFAULT 'DRAFT',

    CONSTRAINT pk_goals PRIMARY KEY (id),
    CONSTRAINT chk_goals_periodicity CHECK (periodicity IN ('WEEKLY', 'MONTHLY')),
    CONSTRAINT chk_goals_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED')),
    CONSTRAINT chk_goals_target_value CHECK (target_value > 0),
    CONSTRAINT chk_goals_period CHECK (end_date >= start_date)
);

CREATE INDEX idx_goals_periodicity_status ON goals (periodicity, status);
