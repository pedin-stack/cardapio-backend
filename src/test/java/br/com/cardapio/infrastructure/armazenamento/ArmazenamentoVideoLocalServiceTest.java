package br.com.cardapio.infrastructure.armazenamento;

import br.com.cardapio.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmazenamentoVideoLocalServiceTest {

    @TempDir
    Path diretorio;

    @Test
    @DisplayName("Grava o HLS, monta a URL pública e apaga a pasta inteira a partir do master.m3u8")
    void saveHls_EDeleteHls_CicloCompleto() throws Exception {
        final ArmazenamentoVideoLocalService servico = new ArmazenamentoVideoLocalService(diretorio.toString(), "https://api.exemplo.com/videos/");
        final Path origem = Files.createDirectory(diretorio.resolve("tmp"));
        Files.writeString(origem.resolve("master.m3u8"), "#EXTM3U");
        Files.writeString(origem.resolve("v0_000.ts"), "x");
        final String prefixo = "restaurantes/r1/videos/hls/abc/";

        servico.saveHls(prefixo, origem);

        assertThat(diretorio.resolve(prefixo + "v0_000.ts")).exists();
        assertThat(servico.findUrlPublica(prefixo + "master.m3u8"))
                .isEqualTo("https://api.exemplo.com/videos/restaurantes/r1/videos/hls/abc/master.m3u8");

        servico.deleteHls(prefixo + "master.m3u8");

        assertThat(diretorio.resolve(prefixo)).doesNotExist();
        assertThat(diretorio.resolve("restaurantes/r1/videos/hls")).exists();
    }

    @Test
    @DisplayName("Recusa caminho que tentaria sair do diretório de vídeos")
    void deleteDiretorio_CaminhoComPontoPonto_Recusa() {
        final ArmazenamentoVideoLocalService servico = new ArmazenamentoVideoLocalService(diretorio.toString(), "http://x/videos");

        assertThatThrownBy(() -> servico.deleteDiretorio("restaurantes/../../etc/"))
                .isInstanceOf(BusinessException.class);
    }
}
