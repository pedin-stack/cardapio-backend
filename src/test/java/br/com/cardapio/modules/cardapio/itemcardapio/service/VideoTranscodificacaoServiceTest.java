package br.com.cardapio.modules.cardapio.itemcardapio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VideoTranscodificacaoServiceTest {

    private final VideoTranscodificacaoService service = new VideoTranscodificacaoService(null, null);

    {
        ReflectionTestUtils.setField(service, "ffmpeg", "ffmpeg");
    }

    @Test
    @DisplayName("Com áudio: duas renditions com áudio cada, segmentos de 2 s e keyframe a cada 2 s")
    void buildComandoHls_ComAudio_MapeiaAudioNasDuasRenditions() {
        final List<String> comando = service.buildComandoHls(Path.of("in"), Path.of("out"), true);

        assertThat(comando).containsSequence("-var_stream_map", "v:0,a:0 v:1,a:1");
        assertThat(comando).containsSequence("-hls_time", "2");
        assertThat(comando).containsSequence("-force_key_frames", "expr:gte(t,n_forced*2)");
        assertThat(comando.stream().filter("0:a:0"::equals).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sem áudio: não mapeia faixa de áudio (o FFmpeg falharia com a:0 inexistente)")
    void buildComandoHls_SemAudio_NaoMapeiaAudio() {
        final List<String> comando = service.buildComandoHls(Path.of("in"), Path.of("out"), false);

        assertThat(comando).containsSequence("-var_stream_map", "v:0 v:1");
        assertThat(comando).doesNotContain("0:a:0", "-c:a");
    }
}
