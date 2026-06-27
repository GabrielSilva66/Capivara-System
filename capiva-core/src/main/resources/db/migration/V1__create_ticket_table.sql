-- Sequence numérica usada internamente pela função geradora de IDs
CREATE SEQUENCE IF NOT EXISTS ticket_conversation_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;

-- Função que formata o próximo valor da sequence como 'TICKET_000001'
CREATE OR REPLACE FUNCTION generate_ticket_id()
RETURNS TEXT AS $$
BEGIN
    RETURN 'TICKET_' || LPAD(nextval('ticket_conversation_seq')::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;

-- Tabela principal de tickets de suporte
CREATE TABLE IF NOT EXISTS cp_ticket (
    ticket_id     VARCHAR(50)  NOT NULL DEFAULT generate_ticket_id(),
    user_name     VARCHAR(255) NOT NULL,
    title         VARCHAR(500) NOT NULL,
    description   TEXT         NOT NULL,
    severity      VARCHAR(20)  NOT NULL,
    status_ticket VARCHAR(30)  NOT NULL DEFAULT 'WAITING',
    dt_created_at TIMESTAMP    NOT NULL DEFAULT now(),
    dt_updated_at TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_cp_ticket PRIMARY KEY (ticket_id),
    CONSTRAINT chk_severity CHECK (severity IN ('BAIXA','MEDIA','ALTA','CRITICA','LOW','MID','HIGH','URGENT')),
    CONSTRAINT chk_status_ticket CHECK (status_ticket IN (
        'RESOLVED','WAITING','ESCALATED_GITHUB',
        'ACTIVE','CANCELED','FINISHED'
    ))
);
