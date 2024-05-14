package br.com.fenix.processatexto.util.constraints

import com.jfoenix.controls.JFXComboBox
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.paint.Color


object Validadores {

    fun setTextFieldNotEmpty(textField: JFXTextField) {
        textField.focusedProperty().addListener { _, _, newPropertyValue ->
            if (newPropertyValue) // Usado para limpar o stilo para que pinte quando entra
                textField.unFocusColor = Color.web("#106ebe")
            else { // Após, na saida faz então a validacao.
                if (textField.textProperty().get().isEmpty())
                    textField.unFocusColor = Color.RED
                else
                    textField.unFocusColor = Color.web("#106ebe")
            }
        }
    }

    fun setTextFieldNotEmpty(passwordField: JFXPasswordField) {
        passwordField.focusedProperty().addListener(object : ChangeListener<Boolean> {
            override fun changed(arg0: ObservableValue<out Boolean>, oldPropertyValue: Boolean, newPropertyValue: Boolean) {
                if (newPropertyValue) // Usado para limpar o stilo para que pinte quando entra
                    passwordField.unFocusColor = Color.web("#106ebe")
                else { // Após, na saida faz então a validacao.
                    if (passwordField.textProperty().get().isEmpty())
                        passwordField.unFocusColor = Color.RED
                    else
                        passwordField.unFocusColor = Color.web("#106ebe")
                }
            }
        })
    }

    fun setComboBoxNotEmpty(comboBox: JFXComboBox<*>, valideText: Boolean) {
        comboBox.focusedProperty().addListener { _, _, newPropertyValue ->
            if (newPropertyValue) // Usado para limpar o stilo para que pinte quando entra
                comboBox.setUnFocusColor(Color.web("#106ebe"))
            else { // Após, na saida faz então a validacao.
                if (valideText && comboBox.editor.textProperty().get().isEmpty())
                    comboBox.setUnFocusColor(Color.RED)
                else if (!valideText && comboBox.value == null)
                    comboBox.setUnFocusColor(Color.RED)
                else
                    comboBox.setUnFocusColor(Color.web("#106ebe"))
            }
        }
    }
}