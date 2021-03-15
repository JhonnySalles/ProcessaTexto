package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class RevisarController implements Initializable {

	@FXML
	private AnchorPane apRoot;

	@FXML
	private Label lblRestantes;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnNovo;

	@FXML
	private JFXTextField txtVocabulario;

	@FXML
	private JFXTextField txtSimilar;

	@FXML
	private JFXTextArea txtAreaIngles;

	@FXML
	private JFXTextArea txtAreaPortugues;

	private RevisarServices service = new RevisarServices();
	private VocabularioServices vocabulario = new VocabularioServices();
	private List<Revisar> similar;
	private Revisar revisando;

	@FXML
	private void onBtnSalvar() {
		if (revisando == null || txtAreaPortugues.getText().isEmpty())
			return;

		revisando.setTraducao(txtAreaPortugues.getText());

		Vocabulario palavra = Revisar.toVocabulario(revisando);

		for (Revisar obj : similar)
			obj.setTraducao(revisando.getTraducao());

		List<Vocabulario> lista = Revisar.toVocabulario(similar);

		try {
			vocabulario.insert(palavra);
			vocabulario.insert(lista);

			service.delete(revisando);
			service.delete(similar);

			onBtnNovo();
		} catch (ExcessaoBd e) {
			e.printStackTrace();
		}
	}

	private void limpaCampos() {
		lblRestantes.setText("Restante 0 palavras.");
		revisando = null;
		txtVocabulario.setText("");
		txtSimilar.setText("");
		txtAreaIngles.setText("");
		txtAreaPortugues.setText("");
	}

	@FXML
	private void onBtnNovo() {
		limpaCampos();
		try {
			lblRestantes.setText("Restante " + service.selectQuantidadeRestante() + " palavras.");

			revisando = service.selectRevisar();

			if (revisando != null) {
				similar = service.selectSimilar(revisando.getVocabulario(), revisando.getIngles());

				txtVocabulario.setText(revisando.getVocabulario());
				txtAreaIngles.setText(revisando.getIngles());
				txtAreaPortugues.setText(revisando.getTraducao());

				if (similar.size() > 0)
					txtSimilar.setText(similar.stream().map(Revisar::getVocabulario).collect(Collectors.joining(", ")));
			}

		} catch (ExcessaoBd e) {
			e.printStackTrace();
		}
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		onBtnNovo();
	}

	public static URL getFxmlLocate() {
		return RevisarController.class.getResource("/view/Revisar.fxml");
	}

}
