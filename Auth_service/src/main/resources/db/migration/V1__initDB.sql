CREATE TABLE users
(
    id                  BIGINT(16)    NOT NULL AUTO_INCREMENT,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(20)  NOT NULL,
    enabled             BOOLEAN      NOT NULL DEFAULT FALSE,
    is_password_changed BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at       DATETIME(3)   NULL,
    created_at          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                                           ON UPDATE CURRENT_TIMESTAMP(3),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'DOCTOR', 'PATIENT'))
) ENGINE = InnoDB
  AUTO_INCREMENT = 1001
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE refresh_tokens
(
    id         BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    token_hash CHAR(100) NOT NULL,
    expires_at DATETIME(3)  NOT NULL,
    revoked    BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);
