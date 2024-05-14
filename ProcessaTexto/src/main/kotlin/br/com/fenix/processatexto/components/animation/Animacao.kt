package br.com.fenix.processatexto.components.animation

import animatefx.animation.FadeIn
import animatefx.animation.FadeOut
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.util.Duration

class Animacao {
    val tmLineImageBanco: Timeline = Timeline()

    @kotlin.jvm.Synchronized
    fun animaImageBanco(img: ImageView, img1: Image?, img2: Image?) {
        if (img1 == null || img2 == null) return
        tmLineImageBanco.getKeyFrames().addAll(KeyFrame(Duration.millis(250.0), object : EventHandler<ActionEvent> {
            @Override
            override fun handle(t: ActionEvent) {
                img.image = img1
            }
        }), KeyFrame(Duration.millis(500.0), { t -> img.image = img2 }))
        tmLineImageBanco.setCycleCount(Animation.INDEFINITE)
    }

    val tmLineImageBackup: Timeline = Timeline()

    @kotlin.jvm.Synchronized
    fun animaImageBackup(img: ImageView, img1: Image?, img2: Image?) {
        if (img1 == null || img2 == null) return
        tmLineImageBackup.getKeyFrames().clear()
        tmLineImageBackup.getKeyFrames().addAll(KeyFrame(Duration.millis(250.0), object : EventHandler<ActionEvent> {
            @Override
            override fun handle(t: ActionEvent?) {
                img.image = img1
            }
        }), KeyFrame(Duration.millis(500.0), object : EventHandler<ActionEvent> {
            @Override
            override fun handle(t: ActionEvent?) {
                img.image = img2
            }
        }))
        tmLineImageBackup.setCycleCount(Animation.INDEFINITE)
    }

    val tmLineImageSincronizacao: Timeline = Timeline()

    @kotlin.jvm.Synchronized
    fun animaImageSincronizacao(img: ImageView, img1: Image?, img2: Image?) {
        if (img1 == null || img2 == null) return
        tmLineImageSincronizacao.getKeyFrames().clear()
        tmLineImageSincronizacao.getKeyFrames().addAll(KeyFrame(Duration.millis(250.0), { t -> img.setImage(img1) }), KeyFrame(Duration.millis(500.0), { t -> img.setImage(img2) }))
        tmLineImageSincronizacao.setCycleCount(Animation.INDEFINITE)
    }

    /**
     *
     *
     * Função que fará a animação de abertura das telas, onde será movimentada a
     * esquerda.
     *
     *
     * @param rootPane O rootPane da tela pai em que a filho será sobreposta.
     * @param rootPane O rootPane da tela filho em que será feito a transição.
     *
     * @author Jhonny de Salles Noschang
     */
    @kotlin.jvm.Synchronized
    fun abrirPane(rootPane: Pane, rootPaneFilho: Node?) {
        val nodeBaixo: Node = rootPane.getChildren().get(0)
        rootPane.getChildren().add(rootPaneFilho)
        val fade = FadeIn(rootPaneFilho)
        fade.setOnFinished { event ->
            nodeBaixo.setVisible(false)
            nodeBaixo.setDisable(true)
        }
        fade.play()
    }

    /**
     *
     *
     * Função que fará a animação de fechamento das telas, onde será movimentado a
     * direita.
     *
     *
     * @param rootPane O rootPane da tela que deve ser fechada.
     *
     * @author Jhonny de Salles Noschang
     */
    @kotlin.jvm.Synchronized
    fun fecharPane(spRoot: Pane) {
        val nodeBaixo: Node = spRoot.getChildren().get(0)
        val nodeCima: Node = spRoot.getChildren().get(1)
        nodeBaixo.setDisable(false)
        nodeBaixo.setVisible(true)
        val fade = FadeOut(nodeCima)
        fade.setOnFinished { event ->
            spRoot.getChildren().remove(nodeCima)
            nodeCima.setVisible(false)
        }
        fade.play()
    }
}