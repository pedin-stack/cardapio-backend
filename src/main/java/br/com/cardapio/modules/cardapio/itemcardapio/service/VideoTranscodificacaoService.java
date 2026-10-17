package br.com.cardapio.modules.cardapio.itemcardapio.service;

import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.modules.cardapio.itemcardapio.enums.StatusVideo;
import br.com.cardapio.modules.cardapio.itemcardapio.repository.IItemCardapioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Transcodifica o vídeo do item em HLS — o "chunking" estilo TikTok do lado do servidor.
 * <p>
 * O original vira segmentos {@code .ts} de 2 s em duas qualidades (lado menor 480 px ~800 kbps e 720 px ~2,2 Mbps)
 * com keyframe forçado a cada 2 s, para que todo segmento comece num quadro decodificável: o player do feed
 * baixa só o primeiro segmento do próximo card e o ABR troca de qualidade em qualquer fronteira de segmento.
 * </p>
 * <p>
 * Layout publicado (plano, sem subpastas): {@code .../hls/<uuid>/master.m3u8}, {@code v0.m3u8}, {@code v1.m3u8},
 * {@code v0_000.ts…}, {@code poster.jpg}. O {@code <uuid>} por envio torna os arquivos imutáveis (cache longo).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoTranscodificacaoService implements IVideoTranscodificacaoService {

    private static final long TIMEOUT_MINUTOS = 10;
    private static final Map<String, String> TIPOS = Map.of(
            "m3u8", "application/vnd.apple.mpegurl",
            "ts", "video/mp2t",
            "jpg", "image/jpeg");

    private final IArmazenamentoArquivoService armazenamentoArquivoService;
    private final IItemCardapioRepository itemCardapioRepository;

    @Value("${app.video.ffmpeg}")
    private String ffmpeg;

    @Value("${app.video.ffprobe}")
    private String ffprobe;

    @Value("${app.video.duracao-maxima-segundos}")
    private int duracaoMaximaSegundos;

    @Override
    public void transcodeVideoItem(final UUID itemId, final String caminhoOriginal) {
        Path pasta = null;
        try {
            pasta = Files.createTempDirectory("video-");
            final Path original = pasta.resolve("original");
            final Path saida = Files.createDirectory(pasta.resolve("hls"));
            final Path arquivoLog = pasta.resolve("ffmpeg.log");

            armazenamentoArquivoService.downloadArquivo(caminhoOriginal, original);

            final double duracao = Double.parseDouble(executar(arquivoLog, ffprobe, "-v", "error",
                    "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1",
                    original.toString()).trim());
            if (duracao <= 0 || duracao > duracaoMaximaSegundos) {
                throw new IllegalArgumentException("Duração fora do limite: " + duracao + " s");
            }
            final boolean temAudio = !executar(arquivoLog, ffprobe, "-v", "error", "-select_streams", "a",
                    "-show_entries", "stream=index", "-of", "csv=p=0", original.toString()).isBlank();

            executar(arquivoLog, buildComandoHls(original, saida, temAudio).toArray(String[]::new));
            executar(arquivoLog, ffmpeg, "-hide_banner", "-loglevel", "error", "-y", "-i", original.toString(),
                    "-frames:v", "1", "-vf", "scale=720:720:force_original_aspect_ratio=increase:force_divisible_by=2",
                    "-q:v", "3", saida.resolve("poster.jpg").toString());

            final String prefixo = caminhoOriginal.substring(0, caminhoOriginal.lastIndexOf('/') + 1)
                    + "hls/" + UUID.randomUUID() + "/";
            publicarArquivos(saida, prefixo);

            final int atualizados = itemCardapioRepository.updateVideoResultado(itemId, caminhoOriginal,
                    StatusVideo.PRONTO, prefixo + "master.m3u8", prefixo + "poster.jpg");
            log.info("Vídeo do item {} transcodificado ({} s, áudio={}, aplicado={})", itemId, duracao, temAudio, atualizados > 0);
        } catch (final Exception ex) {
            log.error("Falha ao transcodificar o vídeo do item {}: {}", itemId, ex.getMessage(), ex);
            itemCardapioRepository.updateVideoResultado(itemId, caminhoOriginal, StatusVideo.ERRO, null, null);
        } finally {
            apagarPasta(pasta);
        }
    }

    /**
     * Uma passada do FFmpeg gera as duas renditions e a playlist mestre.
     * {@code force_original_aspect_ratio=increase} em um quadrado N:N fixa o LADO MENOR em N — funciona igual para
     * vídeo vertical (9:16, o caso do feed) e horizontal.
     */
    List<String> buildComandoHls(final Path original, final Path saida, final boolean temAudio) {
        final List<String> comando = new ArrayList<>(List.of(
                ffmpeg, "-hide_banner", "-loglevel", "error", "-y", "-i", original.toString(),
                "-filter_complex",
                "[0:v]split=2[a][b];"
                        + "[a]scale=480:480:force_original_aspect_ratio=increase:force_divisible_by=2[v0];"
                        + "[b]scale=720:720:force_original_aspect_ratio=increase:force_divisible_by=2[v1]",
                "-map", "[v0]"));
        if (temAudio) {
            comando.addAll(List.of("-map", "0:a:0"));
        }
        comando.addAll(List.of("-map", "[v1]"));
        if (temAudio) {
            comando.addAll(List.of("-map", "0:a:0", "-c:a", "aac", "-b:a", "96k", "-ac", "2"));
        }
        comando.addAll(List.of(
                "-c:v", "libx264", "-preset", "veryfast", "-profile:v", "main", "-pix_fmt", "yuv420p",
                "-b:v:0", "800k", "-maxrate:v:0", "900k", "-bufsize:v:0", "1600k",
                "-b:v:1", "2200k", "-maxrate:v:1", "2500k", "-bufsize:v:1", "4400k",
                // Keyframe a cada 2 s exatos + sem keyframe extra em troca de cena: segmentos alinhados entre qualidades.
                "-force_key_frames", "expr:gte(t,n_forced*2)", "-sc_threshold", "0",
                "-f", "hls", "-hls_time", "2", "-hls_playlist_type", "vod", "-hls_flags", "independent_segments",
                "-hls_segment_filename", saida.resolve("v%v_%03d.ts").toString(),
                "-master_pl_name", "master.m3u8",
                "-var_stream_map", temAudio ? "v:0,a:0 v:1,a:1" : "v:0 v:1",
                saida.resolve("v%v.m3u8").toString()));
        return comando;
    }

    private void publicarArquivos(final Path saida, final String prefixo) throws IOException {
        try (Stream<Path> arquivos = Files.list(saida)) {
            for (final Path arquivo : arquivos.toList()) {
                final String nome = arquivo.getFileName().toString();
                final String tipo = TIPOS.get(nome.substring(nome.lastIndexOf('.') + 1));
                if (tipo != null) {
                    armazenamentoArquivoService.saveArquivo(prefixo + nome, arquivo, tipo);
                }
            }
        }
    }

    /** Executa e devolve a saída. A saída vai para arquivo (pipe cheio travaria o processo) e há timeout. */
    private String executar(final Path arquivoLog, final String... comando) throws IOException, InterruptedException {
        final Process processo = new ProcessBuilder(comando)
                .redirectErrorStream(true)
                .redirectOutput(arquivoLog.toFile())
                .start();
        if (!processo.waitFor(TIMEOUT_MINUTOS, TimeUnit.MINUTES)) {
            processo.destroyForcibly();
            throw new IOException("Timeout ao executar " + comando[0]);
        }
        final String saida = Files.readString(arquivoLog);
        if (processo.exitValue() != 0) {
            throw new IOException(comando[0] + " terminou com código " + processo.exitValue() + ": " + saida);
        }
        return saida;
    }

    private void apagarPasta(final Path pasta) {
        if (pasta == null) {
            return;
        }
        try (Stream<Path> caminhos = Files.walk(pasta)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(caminho -> caminho.toFile().delete());
        } catch (final IOException ex) {
            log.warn("Não foi possível apagar a pasta temporária {}: {}", pasta, ex.getMessage());
        }
    }
}
