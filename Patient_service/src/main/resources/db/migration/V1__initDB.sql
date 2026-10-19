CREATE TABLE patients
(
    id         BIGINT  PRIMARY KEY AUTO_INCREMENT,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    name       VARCHAR(150) NOT NULL,
    dob        DATE         NOT NULL,
    contact    VARCHAR(30),
    address    TEXT,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                             ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1001
  DEFAULT CHARSET = utf8mb4;
