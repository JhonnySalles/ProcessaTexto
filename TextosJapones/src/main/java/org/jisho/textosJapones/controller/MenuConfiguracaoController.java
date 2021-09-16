package org.jisho.textosJapones.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.util.constraints.Validadores;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;

public class MenuConfiguracaoController implements Initializable {

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

	@FXML
	public JFXTextField txtCaminhoMysql;

	@FXML
	public JFXButton btnCaminho;

	@FXML
	public JFXTextField txtDataBaseManga;

	private FrasesController controller;

	@FXML
	private void onBtnCarregarCaminhoMysql() {
		controller.getPopPup().setDetached(true);
		String caminho = selecionaPasta(txtCaminhoMysql.getText());
		txtCaminhoMysql.setText(caminho);
	}

	public void salvar() {
		ConexaoMysql.setDadosConexao(txtServer.getText(), txtPorta.getText(), txtDataBase.getText(),
				txtUsuario.getText(), pswSenha.getText(), txtCaminhoMysql.getText(), txtDataBaseManga.getText());
		controller.verificaConexao();
	}

	public void carregar() {
		ConexaoMysql.getDadosConexao();
		txtServer.setText(ConexaoMysql.getServer());
		txtPorta.setText(ConexaoMysql.getPort());
		txtDataBase.setText(ConexaoMysql.getDataBase());
		txtDataBaseManga.setText(ConexaoMysql.getDataBaseManga());
		txtUsuario.setText(ConexaoMysql.getUser());
		pswSenha.setText(ConexaoMysql.getPassword());
		txtCaminhoMysql.setText(ConexaoMysql.getCaminhoMysql());
	}

	private String selecionaPasta(String pasta) {
		DirectoryChooser fileChooser = new DirectoryChooser();
		fileChooser.setTitle("Selecione a pasta do mysql");

		if (pasta != null && !pasta.isEmpty())
			fileChooser.setInitialDirectory(new File(pasta));
		File caminho = fileChooser.showDialog(null);

		if (caminho == null)
			return "";
		else
			return caminho.getAbsolutePath();
	}

	public void setControllerPai(FrasesController cnt) {
		this.controller = cnt;
	}

	public static URL getFxmlLocate() {
		return MenuConfiguracaoController.class.getResource("/view/MenuConfiguracao.fxml");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Validadores.setTextFieldNotEmpty(txtServer);
		Validadores.setTextFieldNotEmpty(txtPorta);
		Validadores.setTextFieldNotEmpty(txtDataBase);
		Validadores.setTextFieldNotEmpty(txtUsuario);
		Validadores.setTextFieldNotEmpty(pswSenha);
		Validadores.setTextFieldNotEmpty(txtDataBaseManga);
	}
}
