CREATE TABLE appointments
(
    id           BIGINT  PRIMARY KEY AUTO_INCREMENT,
    patient_id   BIGINT  NOT NULL,
    doctor_id    BIGINT  NOT NULL,
    start_time   DATETIME(3) NOT NULL,
    end_time     DATETIME(3) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason       VARCHAR(500),
    created_by   BIGINT  NOT NULL,
    version      INT         NOT NULL DEFAULT 0,
    created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT chk_time CHECK (end_time > start_time),
    active_start DATETIME(3) GENERATED ALWAYS AS (IF(status = 'SCHEDULED', start_time, NULL)) STORED,
    UNIQUE KEY uq_doctor_active_slot (doctor_id, active_start),
    KEY          idx_patient (patient_id, start_time),
    KEY          idx_doctor (doctor_id, start_time)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1001
  DEFAULT CHARSET = utf8mb4;