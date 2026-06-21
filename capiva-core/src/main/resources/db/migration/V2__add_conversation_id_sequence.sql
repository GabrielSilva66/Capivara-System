-- Migration V2: sequence para geração de IDs de conversa amigáveis ao usuário
CREATE SEQUENCE IF NOT EXISTS ticket_conversation_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE;
