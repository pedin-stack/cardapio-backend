package br.com.cardapio.modules.core.restaurante.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import br.com.cardapio.modules.core.video.enums.StatusVideo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Restaurante cliente da plataforma. Desativado, o cardápio público responde "Serviço indisponível" (404)
 * e o gestor perde o acesso ao painel.
 * <p>
 * Mora no Core (e não no módulo cardápio) porque usuários e cardápio dependem dele (blueprint §2.2.2).
 * </p>
 */
@Entity
@Table(name = "restaurantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Restaurante extends PersistenceEntity {

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    /** Endereço público do cardápio: {@code /{slug}} no front. */
    @Column(name = "slug", nullable = false, unique = true, length = 80)
    private String slug;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Column(name = "capa_path", length = 500)
    private String capaPath;

    /** Hex {@code #rrggbb} do destaque no cardápio público; null = cor padrão. */
    @Column(name = "cor_destaque", length = 7)
    private String corDestaque;

    /** Logo horizontal do cabeçalho do cardápio, enviada pelo gestor (pasta do restaurante). */
    @Column(name = "logo_cabecalho_path", length = 500)
    private String logoCabecalhoPath;

    /** Vídeo de fundo da capa: mesmo ciclo do vídeo do item (original no bucket → HLS no disco). */
    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false, length = 20)
    private StatusVideo videoStatus = StatusVideo.SEM_VIDEO;

    @Column(name = "video_original_path", length = 500)
    private String videoOriginalPath;

    @Column(name = "video_hls_path", length = 500)
    private String videoHlsPath;

    @Column(name = "video_poster_path", length = 500)
    private String videoPosterPath;

    /** DDD + número, só dígitos (o front monta o link wa.me com o 55). */
    @Column(name = "whatsapp", length = 11)
    private String whatsapp;

    /** Usuário do Instagram, sem o @. */
    @Column(name = "instagram", length = 30)
    private String instagram;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
