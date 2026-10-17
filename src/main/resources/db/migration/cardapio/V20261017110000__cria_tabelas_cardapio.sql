-- =====================================================================
-- Módulo Cardápio — categorias personalizadas e itens (até 8 imagens + 1 vídeo HLS).
-- Tudo pendura no restaurante com ON DELETE CASCADE: excluir o restaurante limpa o cardápio.
-- =====================================================================

CREATE TABLE public.categorias (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    nome            VARCHAR(60) NOT NULL,
    ordem           INTEGER     NOT NULL DEFAULT 0,
    restaurante_id  UUID        NOT NULL,
    CONSTRAINT categorias_pkey PRIMARY KEY (id),
    CONSTRAINT fk_categorias_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE
);

-- Nome único por restaurante, sem diferenciar maiúsculas.
CREATE UNIQUE INDEX uk_categorias_restaurante_nome ON public.categorias (restaurante_id, lower(nome));

CREATE TABLE public.itens_cardapio (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    nome                 VARCHAR(120)  NOT NULL,
    descricao            TEXT,
    valor                NUMERIC(10,2) NOT NULL,
    ativo                BOOLEAN       NOT NULL DEFAULT TRUE,
    ordem                INTEGER       NOT NULL DEFAULT 0,
    restaurante_id       UUID          NOT NULL,
    video_status         VARCHAR(20)   NOT NULL DEFAULT 'SEM_VIDEO',
    video_original_path  VARCHAR(500),
    video_hls_path       VARCHAR(500),
    video_poster_path    VARCHAR(500),
    criado_em            TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em        TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT itens_cardapio_pkey PRIMARY KEY (id),
    CONSTRAINT chk_itens_cardapio_valor CHECK (valor >= 0),
    CONSTRAINT chk_itens_cardapio_video_status CHECK (video_status IN ('SEM_VIDEO', 'PROCESSANDO', 'PRONTO', 'ERRO')),
    CONSTRAINT fk_itens_cardapio_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE
);

CREATE INDEX idx_itens_cardapio_restaurante_id ON public.itens_cardapio (restaurante_id);

CREATE TABLE public.itens_categorias (
    item_id       UUID NOT NULL,
    categoria_id  UUID NOT NULL,
    CONSTRAINT itens_categorias_pkey PRIMARY KEY (item_id, categoria_id),
    CONSTRAINT fk_itens_categorias_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE,
    -- Excluir a categoria só desfaz o vínculo; o item continua.
    CONSTRAINT fk_itens_categorias_categorias FOREIGN KEY (categoria_id) REFERENCES public.categorias (id) ON DELETE CASCADE
);

CREATE INDEX idx_itens_categorias_categoria_id ON public.itens_categorias (categoria_id);

CREATE TABLE public.itens_ingredientes (
    item_id  UUID        NOT NULL,
    ordem    INTEGER     NOT NULL,
    nome     VARCHAR(80) NOT NULL,
    CONSTRAINT itens_ingredientes_pkey PRIMARY KEY (item_id, ordem),
    CONSTRAINT fk_itens_ingredientes_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE
);

CREATE TABLE public.itens_imagens (
    item_id  UUID         NOT NULL,
    ordem    INTEGER      NOT NULL,
    caminho  VARCHAR(500) NOT NULL,
    CONSTRAINT itens_imagens_pkey PRIMARY KEY (item_id, ordem),
    -- Regra de negócio: no máximo 8 imagens por item (ordem 0..7). Defesa final além do service e do DTO.
    CONSTRAINT chk_itens_imagens_ordem CHECK (ordem BETWEEN 0 AND 7),
    CONSTRAINT fk_itens_imagens_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE
);
