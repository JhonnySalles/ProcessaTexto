package org.jisho.textosJapones.components.animation;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class Animacao {

	final public Timeline tmLineImageBanco = new Timeline();

	synchronized public void animaImageBanco(ImageView img, Image img1, Image img2) {

		if (img1 == null || img2 == null)
			return;

		tmLineImageBanco.getKeyFrames().addAll(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				img.setImage(img1);
			}
		}), new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				img.setImage(img2);
			}
		}));
		tmLineImageBanco.setCycleCount(Animation.INDEFINITE);
	}

	final public Timeline tmLineImageBackup = new Timeline();

	synchronized public void animaImageBackup(ImageView img, Image img1, Image img2) {

		if (img1 == null || img2 == null)
			return;

		tmLineImageBackup.getKeyFrames().clear();
		tmLineImageBackup.getKeyFrames().addAll(new KeyFrame(Duration.millis(250), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				img.setImage(img1);
			}
		}), new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				img.setImage(img2);
			}
		}));
		tmLineImageBackup.setCycleCount(Animation.INDEFINITE);
	}

	/**
	 * <p>
	 * Função que fará a animação de abertura das telas, onde será movimentada a
	 * esquerda.
	 * </p>
	 * 
	 * @param rootPane O rootPane da tela pai em que a filho será sobreposta.
	 * @param rootPane O rootPane da tela filho em que será feito a transição.
	 * 
	 * @author Jhonny de Salles Noschang
	 */
	public synchronized void abrirPane(Pane rootPane, Node rootPaneFilho) {
		Node nodeBaixo = rootPane.getChildren().get(0);
		rootPane.getChildren().add(rootPaneFilho);

		FadeIn fade = new FadeIn(rootPaneFilho);
		fade.setOnFinished(event -> {
			nodeBaixo.setVisible(false);
			nodeBaixo.setDisable(true);
		});
		fade.play();
	}

	/**
	 * <p>
	 * Função que fará a animação de fechamento das telas, onde será movimentado a
	 * direita.
	 * </p>
	 * 
	 * @param rootPane O rootPane da tela que deve ser fechada.
	 * 
	 * @author Jhonny de Salles Noschang
	 */
	public synchronized void fecharPane(Pane spRoot) {
		Node nodeBaixo = spRoot.getChildren().get(0);
		Node nodeCima = spRoot.getChildren().get(1);

		nodeBaixo.setDisable(false);
		nodeBaixo.setVisible(true);

		FadeOut fade = new FadeOut(nodeCima);

		fade.setOnFinished(event -> {
			spRoot.getChildren().remove(nodeCima);
			nodeCima.setVisible(false);
		});
		fade.play();
	}

}
