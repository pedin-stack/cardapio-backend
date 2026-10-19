-- =====================================================================
-- Datas geradas pelo Hibernate (@CreationTimestamp/@UpdateTimestamp) estavam em UTC: o relógio dele nascia antes de
-- o fuso padrão virar America/Sao_Paulo. O resto (curtidas, visualizações, último login, comentários) já gravava no
-- horário de Brasília. Agora o fuso vale desde o main; aqui o que já foi gravado é convertido.
-- As linhas que vieram do DEFAULT CURRENT_TIMESTAMP (SQL manual) também estavam em UTC: a sessão do banco era UTC.
-- =====================================================================

UPDATE public.restaurantes
SET criado_em = (criado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';

UPDATE public.usuarios
SET criado_em = (criado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';

UPDATE public.itens_cardapio
SET criado_em     = (criado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo',
    atualizado_em = (atualizado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';

UPDATE public.adicionais
SET criado_em = (criado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';

UPDATE public.clientes
SET criado_em     = (criado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo',
    atualizado_em = (atualizado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';

UPDATE public.avaliacoes
SET criado_em     = (criado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo',
    atualizado_em = (atualizado_em AT TIME ZONE 'UTC') AT TIME ZONE 'America/Sao_Paulo';

-- Sessões novas (psql, importações) também no horário de Brasília: o DEFAULT CURRENT_TIMESTAMP deixa de gravar UTC.
-- A API já conecta nesse fuso (o driver usa o fuso padrão da JVM).
DO $$
BEGIN
    EXECUTE format('ALTER DATABASE %I SET timezone TO %L', current_database(), 'America/Sao_Paulo');
END
$$;
