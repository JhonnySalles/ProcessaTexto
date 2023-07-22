package org.jisho.textosJapones.util.constraints;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

public class Validadores {

	public static void setTextFieldNotEmpty(final JFXTextField textField) {
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue) {
				if (newPropertyValue) { // Usado para limpar o stilo para que pinte quando entra
					textField.setUnFocusColor(Color.web("#106ebe"));
				} else { // Após, na saida faz então a validacao.
					if (textField.textProperty().get().toString().isEmpty()) {
						textField.setUnFocusColor(Color.RED);
					} else {
						textField.setUnFocusColor(Color.web("#106ebe"));
					}
				}
			}
		});
	}

	public static void setTextFieldNotEmpty(final JFXPasswordField passwordField) {
		passwordField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue) {
				if (newPropertyValue) { // Usado para limpar o stilo para que pinte quando entra
					passwordField.setUnFocusColor(Color.web("#106ebe"));
				} else { // Após, na saida faz então a validacao.
					if (passwordField.textProperty().get().toString().isEmpty()) {
						passwordField.setUnFocusColor(Color.RED);
					} else {
						passwordField.setUnFocusColor(Color.web("#106ebe"));
					}
				}
			}
		});
	}

}
