-- =====================================================================
-- Módulo Cardápio — métricas do cliente anônimo (identificado só pelo dispositivo_id do navegador).
-- curtidas: curtida = pedido; removido_em preenchido = "quase pedido".
-- visualizacoes: tempo de tela por item.
-- =====================================================================

CREATE TABLE public.curtidas (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    restaurante_id  UUID NOT NULL,
    item_id         UUID NOT NULL,
    dispositivo_id  UUID NOT NULL,
    curtido_em      TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    removido_em     TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT curtidas_pkey PRIMARY KEY (id),
    CONSTRAINT fk_curtidas_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE,
    CONSTRAINT fk_curtidas_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE
);

-- No máximo uma curtida ATIVA por (item, dispositivo); o histórico de curtidas desfeitas fica.
CREATE UNIQUE INDEX uk_curtidas_item_dispositivo_ativa ON public.curtidas (item_id, dispositivo_id) WHERE removido_em IS NULL;
CREATE INDEX idx_curtidas_restaurante_curtido_em ON public.curtidas (restaurante_id, curtido_em);

CREATE TABLE public.visualizacoes (
    id              UUID   NOT NULL DEFAULT gen_random_uuid(),
    restaurante_id  UUID   NOT NULL,
    item_id         UUID   NOT NULL,
    dispositivo_id  UUID   NOT NULL,
    duracao_ms      BIGINT NOT NULL,
    ocorrido_em     TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT visualizacoes_pkey PRIMARY KEY (id),
    CONSTRAINT chk_visualizacoes_duracao_ms CHECK (duracao_ms >= 0),
    CONSTRAINT fk_visualizacoes_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE,
    CONSTRAINT fk_visualizacoes_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE
);

CREATE INDEX idx_visualizacoes_restaurante_ocorrido_em ON public.visualizacoes (restaurante_id, ocorrido_em);
CREATE INDEX idx_visualizacoes_item_id ON public.visualizacoes (item_id);
