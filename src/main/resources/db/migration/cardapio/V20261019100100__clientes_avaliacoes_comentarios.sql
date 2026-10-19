-- =====================================================================
-- Clientes que se identificam no cardápio público (nome, telefone e CPF, por restaurante),
-- avaliações (1 a 5 estrelas por item) e comentários dos itens.
-- Sem verificação por SMS: a identificação é declarada pelo cliente.
-- =====================================================================

CREATE TABLE public.clientes (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    nome            VARCHAR(120) NOT NULL,
    telefone        VARCHAR(11)  NOT NULL,
    cpf             VARCHAR(11)  NOT NULL,
    restaurante_id  UUID         NOT NULL,
    criado_em       TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT clientes_pkey PRIMARY KEY (id),
    CONSTRAINT fk_clientes_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE
);

-- Um cadastro por CPF em cada restaurante (identificar-se de novo atualiza nome e telefone).
CREATE UNIQUE INDEX uk_clientes_restaurante_cpf ON public.clientes (restaurante_id, cpf);

CREATE TABLE public.avaliacoes (
    id             UUID     NOT NULL DEFAULT gen_random_uuid(),
    item_id        UUID     NOT NULL,
    cliente_id     UUID     NOT NULL,
    estrelas       SMALLINT NOT NULL,
    criado_em      TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em  TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT avaliacoes_pkey PRIMARY KEY (id),
    CONSTRAINT chk_avaliacoes_estrelas CHECK (estrelas BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacoes_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacoes_clientes FOREIGN KEY (cliente_id) REFERENCES public.clientes (id) ON DELETE CASCADE
);

-- Uma avaliação por cliente em cada item (avaliar de novo troca as estrelas).
CREATE UNIQUE INDEX uk_avaliacoes_item_cliente ON public.avaliacoes (item_id, cliente_id);

CREATE TABLE public.comentarios (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    item_id     UUID         NOT NULL,
    cliente_id  UUID         NOT NULL,
    texto       VARCHAR(500) NOT NULL,
    criado_em   TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT comentarios_pkey PRIMARY KEY (id),
    CONSTRAINT fk_comentarios_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE,
    CONSTRAINT fk_comentarios_clientes FOREIGN KEY (cliente_id) REFERENCES public.clientes (id) ON DELETE CASCADE
);

CREATE INDEX idx_comentarios_item_criado_em ON public.comentarios (item_id, criado_em DESC);
