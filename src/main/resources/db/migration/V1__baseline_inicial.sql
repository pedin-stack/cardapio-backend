-- =====================================================================
-- Baseline do Core: restaurantes, perfis, catálogo de permissões e usuários.
-- IAM enxuto (ADR-0001): perfil único por usuário; o gestor aponta para o seu restaurante.
-- `permissao` é tabela de domínio (sem CHECK gigante em perfil_permissoes).
-- =====================================================================

CREATE TABLE public.restaurantes (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    nome        VARCHAR(120) NOT NULL,
    slug        VARCHAR(80)  NOT NULL,
    descricao   VARCHAR(500),
    logo_path   VARCHAR(500),
    capa_path   VARCHAR(500),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT restaurantes_pkey PRIMARY KEY (id),
    CONSTRAINT uk_restaurantes_slug UNIQUE (slug)
);

CREATE TABLE public.perfis (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    nome       VARCHAR(100) NOT NULL,
    descricao  VARCHAR(255),
    modulo     VARCHAR(50)  NOT NULL,
    imutavel   BOOLEAN      NOT NULL,
    CONSTRAINT perfis_pkey PRIMARY KEY (id),
    CONSTRAINT uk_perfis_nome UNIQUE (nome)
);

CREATE TABLE public.permissao (
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT permissao_pkey PRIMARY KEY (nome)
);

CREATE TABLE public.perfil_permissoes (
    perfil_id  UUID         NOT NULL,
    permissao  VARCHAR(100) NOT NULL,
    CONSTRAINT perfil_permissoes_pkey PRIMARY KEY (perfil_id, permissao),
    CONSTRAINT fk_perfil_permissoes_perfis FOREIGN KEY (perfil_id) REFERENCES public.perfis (id),
    CONSTRAINT fk_perfil_permissoes_permissao FOREIGN KEY (permissao) REFERENCES public.permissao (nome)
);

CREATE TABLE public.usuarios (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    nome             VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL,
    senha            VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    perfil_id        UUID         NOT NULL,
    restaurante_id   UUID,
    criado_em        TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_login_em  TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT usuarios_pkey PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT chk_usuarios_status CHECK (status IN ('ATIVO', 'INATIVO')),
    CONSTRAINT fk_usuarios_perfis FOREIGN KEY (perfil_id) REFERENCES public.perfis (id),
    -- Excluir o restaurante exclui o seu gestor.
    CONSTRAINT fk_usuarios_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE
);

CREATE INDEX idx_usuarios_restaurante_id ON public.usuarios (restaurante_id);
