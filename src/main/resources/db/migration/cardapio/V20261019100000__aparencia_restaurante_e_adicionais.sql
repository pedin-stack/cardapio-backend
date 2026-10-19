-- =====================================================================
-- Aparência do restaurante editada pelo gestor (logo do cabeçalho, vídeo de fundo da capa, redes sociais)
-- e adicionais pagos (um adicional vale para vários itens).
-- =====================================================================

-- Logo horizontal do cabeçalho do cardápio (a logo quadrada continua em logo_path).
ALTER TABLE public.restaurantes ADD COLUMN logo_cabecalho_path VARCHAR(500);

-- Vídeo da capa: mesmo pipeline HLS dos itens.
ALTER TABLE public.restaurantes ADD COLUMN video_status VARCHAR(20) NOT NULL DEFAULT 'SEM_VIDEO';
ALTER TABLE public.restaurantes ADD COLUMN video_original_path VARCHAR(500);
ALTER TABLE public.restaurantes ADD COLUMN video_hls_path VARCHAR(500);
ALTER TABLE public.restaurantes ADD COLUMN video_poster_path VARCHAR(500);
ALTER TABLE public.restaurantes ADD CONSTRAINT chk_restaurantes_video_status
    CHECK (video_status IN ('SEM_VIDEO', 'PROCESSANDO', 'PRONTO', 'ERRO'));

-- WhatsApp: DDD + número, só dígitos. Instagram: o @ sem o @.
ALTER TABLE public.restaurantes ADD COLUMN whatsapp VARCHAR(11);
ALTER TABLE public.restaurantes ADD COLUMN instagram VARCHAR(30);

CREATE TABLE public.adicionais (
    id              UUID          NOT NULL DEFAULT gen_random_uuid(),
    nome            VARCHAR(60)   NOT NULL,
    valor           NUMERIC(10,2) NOT NULL,
    restaurante_id  UUID          NOT NULL,
    criado_em       TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT adicionais_pkey PRIMARY KEY (id),
    CONSTRAINT chk_adicionais_valor CHECK (valor >= 0),
    CONSTRAINT fk_adicionais_restaurantes FOREIGN KEY (restaurante_id) REFERENCES public.restaurantes (id) ON DELETE CASCADE
);

CREATE INDEX idx_adicionais_restaurante_id ON public.adicionais (restaurante_id);

-- Excluir o adicional ou o item só desfaz o vínculo.
CREATE TABLE public.itens_adicionais (
    adicional_id  UUID NOT NULL,
    item_id       UUID NOT NULL,
    CONSTRAINT itens_adicionais_pkey PRIMARY KEY (adicional_id, item_id),
    CONSTRAINT fk_itens_adicionais_adicionais FOREIGN KEY (adicional_id) REFERENCES public.adicionais (id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_adicionais_itens_cardapio FOREIGN KEY (item_id) REFERENCES public.itens_cardapio (id) ON DELETE CASCADE
);

CREATE INDEX idx_itens_adicionais_item_id ON public.itens_adicionais (item_id);
