package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.util.constraints.Validadores;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class ConfiguracaoController implements Initializable {

	@FXML
	public JFXTextField txtUsuario;

	@FXML
	public JFXPasswordField pswSenha;

	@FXML
	public JFXTextField txtServer;

	@FXML
	public JFXTextField txtPorta;

	@FXML
	public JFXTextField txtDataBase;

	private ProcessarFrasesController controller;

	public void salvar() {
		ConexaoMysql.setDadosConexao(txtServer.getText(), txtPorta.getText(), txtDataBase.getText(),
				txtUsuario.getText(), pswSenha.getText());
		controller.verificaConexao();
	}

	public void carregar() {
		ConexaoMysql.getDadosConexao();
		txtServer.setText(ConexaoMysql.getServer());
		txtPorta.setText(ConexaoMysql.getPort());
		txtDataBase.setText(ConexaoMysql.getDataBase());
		txtUsuario.setText(ConexaoMysql.getUser());
		pswSenha.setText(ConexaoMysql.getPassword());
	}

	public void setControllerPai(ProcessarFrasesController cnt) {
		this.controller = cnt;
	}

	public static URL getFxmlLocate() {
		return ConfiguracaoController.class.getResource("/org/jisho/textosJapones/view/Configuracao.fxml");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Validadores.setTextFieldNotEmpty(txtServer);
		Validadores.setTextFieldNotEmpty(txtPorta);
		Validadores.setTextFieldNotEmpty(txtDataBase);
		Validadores.setTextFieldNotEmpty(txtUsuario);
		Validadores.setTextFieldNotEmpty(pswSenha);
	}
}
