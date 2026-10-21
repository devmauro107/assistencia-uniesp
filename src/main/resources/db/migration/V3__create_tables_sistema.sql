-- 1. Técnicos
CREATE TABLE tecnicos
(
    id            BIGSERIAL    NOT NULL,
    nome          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    telefone      VARCHAR(20),
    especialidade VARCHAR(100),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_tecnicos PRIMARY KEY (id),
    CONSTRAINT uk_tecnicos_email UNIQUE (email)
);

-- 2. Peças
CREATE TABLE pecas
(
    id                 BIGSERIAL      NOT NULL,
    nome               VARCHAR(150)   NOT NULL,
    codigo             VARCHAR(50)    NOT NULL,
    preco              DECIMAL(10, 2) NOT NULL,
    quantidade_estoque INTEGER        NOT NULL DEFAULT 0,
    ativo              BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_pecas PRIMARY KEY (id),
    CONSTRAINT uk_pecas_codigo UNIQUE (codigo)
);

-- 3. Usuários
CREATE TABLE usuarios
(
    id           BIGSERIAL    NOT NULL,
    nome         VARCHAR(150) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    senha        VARCHAR(255) NOT NULL,
    perfil       VARCHAR(20)  NOT NULL,
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);

-- 4. Ordens de Serviço
CREATE TABLE ordens_servico
(
    id                 BIGSERIAL   NOT NULL,
    numero_protocolo   VARCHAR(30) NOT NULL,
    cliente_id         BIGINT      NOT NULL,
    equipamento_id     BIGINT      NOT NULL,
    tecnico_id         BIGINT,
    status             VARCHAR(30) NOT NULL,
    descricao_problema TEXT        NOT NULL,
    data_abertura      TIMESTAMP   NOT NULL,
    data_finalizacao   TIMESTAMP,
    data_entrega       TIMESTAMP,

    CONSTRAINT pk_ordens_servico PRIMARY KEY (id),
    CONSTRAINT uk_ordens_servico_protocolo UNIQUE (numero_protocolo),
    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_os_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamentos (id),
    CONSTRAINT fk_os_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnicos (id)
);

-- 5. Diagnósticos
CREATE TABLE diagnosticos
(
    id                BIGSERIAL NOT NULL,
    ordem_servico_id  BIGINT    NOT NULL,
    tecnico_id        BIGINT    NOT NULL,
    descricao_tecnica TEXT      NOT NULL,
    solucao_proposta  TEXT      NOT NULL,
    data_registro     TIMESTAMP NOT NULL,

    CONSTRAINT pk_diagnosticos PRIMARY KEY (id),
    CONSTRAINT uk_diagnosticos_os UNIQUE (ordem_servico_id),
    CONSTRAINT fk_diagnosticos_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id),
    CONSTRAINT fk_diagnosticos_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnicos (id)
);

-- 6. Orçamentos
CREATE TABLE orcamentos
(
    id                BIGSERIAL      NOT NULL,
    ordem_servico_id  BIGINT         NOT NULL,
    valor_mao_de_obra DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    valor_pecas       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    valor_total       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    data_criacao      TIMESTAMP      NOT NULL,
    data_expiracao    TIMESTAMP      NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    data_resposta     TIMESTAMP,

    CONSTRAINT pk_orcamentos PRIMARY KEY (id),
    CONSTRAINT uk_orcamentos_os UNIQUE (ordem_servico_id),
    CONSTRAINT fk_orcamentos_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);

-- 7. Itens do Orçamento
CREATE TABLE itens_orcamento
(
    id               BIGSERIAL      NOT NULL,
    orcamento_id     BIGINT         NOT NULL,
    peca_id          BIGINT         NOT NULL,
    quantidade       INTEGER        NOT NULL,
    preco_unitario   DECIMAL(10, 2) NOT NULL,
    valor_total_item DECIMAL(10, 2) NOT NULL,

    CONSTRAINT pk_itens_orcamento PRIMARY KEY (id),
    CONSTRAINT fk_itens_orcamento_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamentos (id),
    CONSTRAINT fk_itens_orcamento_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);

-- 8. Histórico de Status
CREATE TABLE historico_status
(
    id               BIGSERIAL    NOT NULL,
    ordem_servico_id BIGINT       NOT NULL,
    status_anterior  VARCHAR(30),
    novo_status      VARCHAR(30)  NOT NULL,
    data_alteracao   TIMESTAMP    NOT NULL,
    responsavel      VARCHAR(150) NOT NULL,
    observacao       VARCHAR(500),

    CONSTRAINT pk_historico_status PRIMARY KEY (id),
    CONSTRAINT fk_historico_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);

-- 9. Peças Utilizadas na OS
CREATE TABLE os_pecas
(
    id                       BIGSERIAL      NOT NULL,
    ordem_servico_id         BIGINT         NOT NULL,
    peca_id                  BIGINT         NOT NULL,
    quantidade               INTEGER        NOT NULL,
    preco_unitario_historico DECIMAL(10, 2) NOT NULL,
    data_registro            TIMESTAMP      NOT NULL,

    CONSTRAINT pk_os_pecas PRIMARY KEY (id),
    CONSTRAINT fk_os_pecas_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id),
    CONSTRAINT fk_os_pecas_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);

-- 10. Anexos
CREATE TABLE anexos
(
    id                   BIGSERIAL    NOT NULL,
    ordem_servico_id     BIGINT       NOT NULL,
    nome_original        VARCHAR(255) NOT NULL,
    nome_arquivo_storage VARCHAR(255) NOT NULL,
    tipo_conteudo        VARCHAR(100) NOT NULL,
    tamanho_bytes        BIGINT       NOT NULL,
    caminho_storage      VARCHAR(500) NOT NULL,
    data_upload          TIMESTAMP    NOT NULL,

    CONSTRAINT pk_anexos PRIMARY KEY (id),
    CONSTRAINT uk_anexos_arquivo_storage UNIQUE (nome_arquivo_storage),
    CONSTRAINT fk_anexos_os FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);
