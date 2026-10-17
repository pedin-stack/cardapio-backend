package br.com.cardapio.infrastructure.armazenamento;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Armazenamento do vídeo já transcodificado (HLS). Fica no disco da VPS e é servido pela própria API em
 * {@code /videos/**}: os chunks não consomem a cota de armazenamento/tráfego do Supabase (ADR-0001).
 */
public interface IArmazenamentoVideoService {

    /**
     * Copia os arquivos de {@code origem} (master.m3u8, playlists, .ts, pôster) para {@code prefixo}.
     *
     * @param prefixo caminho relativo de destino, terminado em "/" (ex.: {@code restaurantes/<id>/videos/hls/<uuid>/})
     * @param origem  pasta local com os arquivos gerados pelo FFmpeg
     * @throws IOException se a cópia falhar
     */
    void saveHls(String prefixo, Path origem) throws IOException;

    /** Apaga a pasta do HLS que contém {@code caminho} (ex.: o master.m3u8). Ausente não é erro. */
    void deleteHls(String caminho);

    /** Apaga tudo abaixo de {@code prefixo} (ex.: todos os vídeos de um restaurante excluído). Ausente não é erro. */
    void deleteDiretorio(String prefixo);

    /** URL pública do arquivo, ou {@code null} se o caminho for nulo. */
    String findUrlPublica(String caminho);
}
