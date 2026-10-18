-- =====================================================================
-- Cor de destaque do restaurante (cardápio público) e especificações dos itens.
-- Especificações = grupos de opção única ({"nome": "Ponto", "opcoes": ["Mal passado", ...]}), em JSONB:
-- grupo -> opções aninhadas, sempre lidas e gravadas com o item inteiro.
-- =====================================================================

-- NULL = cor padrão do cardápio (coral).
ALTER TABLE public.restaurantes ADD COLUMN cor_destaque VARCHAR(7);

ALTER TABLE public.itens_cardapio ADD COLUMN especificacoes JSONB NOT NULL DEFAULT '[]';

-- O gestor muda a aparência do próprio restaurante (o id vem da sessão).
INSERT INTO public.permissao (nome) VALUES ('CORE_RESTAURANTE_ATUALIZAR_PROPRIO') ON CONFLICT DO NOTHING;

INSERT INTO public.perfil_permissoes (perfil_id, permissao) VALUES
('5b0c1d2e-0000-4000-8000-000000000002', 'CORE_RESTAURANTE_ATUALIZAR_PROPRIO');
