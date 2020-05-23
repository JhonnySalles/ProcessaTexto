package org.jisho.textosJapones.util.animation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

}
