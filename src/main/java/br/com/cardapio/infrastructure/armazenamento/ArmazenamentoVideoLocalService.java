package br.com.cardapio.infrastructure.armazenamento;

import br.com.cardapio.infrastructure.util.CaminhoArquivoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * HLS no disco ({@code app.video.diretorio}, um volume Docker em produção), publicado em {@code app.video.url-base}.
 */
@Slf4j
@Service
public class ArmazenamentoVideoLocalService implements IArmazenamentoVideoService {

    private final Path diretorio;
    private final String urlBase;

    public ArmazenamentoVideoLocalService(@Value("${app.video.diretorio}") final String diretorio,
                                          @Value("${app.video.url-base}") final String urlBase) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    @Override
    public void saveHls(final String prefixo, final Path origem) throws IOException {
        final Path destino = resolverOrThrow(prefixo);
        Files.createDirectories(destino);
        try (Stream<Path> arquivos = Files.list(origem)) {
            for (final Path arquivo : arquivos.toList()) {
                Files.copy(arquivo, destino.resolve(arquivo.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public void deleteHls(final String caminho) {
        if (caminho != null && caminho.contains("/")) {
            deleteDiretorio(caminho.substring(0, caminho.lastIndexOf('/') + 1));
        }
    }

    @Override
    public void deleteDiretorio(final String prefixo) {
        final Path pasta = resolverOrThrow(prefixo);
        if (pasta.equals(diretorio) || !Files.isDirectory(pasta)) {
            return;
        }
        try (Stream<Path> caminhos = Files.walk(pasta)) {
            caminhos.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (final IOException ex) {
            log.warn("Não foi possível apagar o HLS {}: {}", pasta, ex.getMessage());
        }
    }

    @Override
    public String findUrlPublica(final String caminho) {
        return caminho == null ? null : urlBase + "/" + caminho;
    }

    /** Valida o caminho relativo e garante que ele não escapa do diretório de vídeos. */
    private Path resolverOrThrow(final String relativo) {
        CaminhoArquivoUtil.validateCaminhoOrThrow(relativo.replaceAll("/+$", ""));
        final Path resolvido = diretorio.resolve(relativo).normalize();
        if (!resolvido.startsWith(diretorio)) {
            throw new IllegalArgumentException("Caminho de vídeo fora do diretório: " + relativo);
        }
        return resolvido;
    }
}
