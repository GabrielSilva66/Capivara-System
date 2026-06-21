CREATE TABLE IF NOT EXISTS cp_ticket (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    conversation_id  VARCHAR(255) NOT NULL,
    user_name        VARCHAR(255) NOT NULL,
    title            VARCHAR(500) NOT NULL,
    description      TEXT         NOT NULL,
    severity         VARCHAR(20)  NOT NULL,
    status_ticket    VARCHAR(30)  NOT NULL DEFAULT 'WAITING',
    dt_created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    dt_updated_at    TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_cp_ticket PRIMARY KEY (id),
    CONSTRAINT chk_severity     CHECK (severity IN ('BAIXA','MEDIA','ALTA','CRITICA','LOW','MID','HIGH','URGENT')),
    CONSTRAINT chk_status_ticket CHECK (status_ticket IN (
        'RESOLVED','WAITING','ESCALATED_GITHUB',
        'ACTIVE','CANCELED','FINISHED'
    ))
);
