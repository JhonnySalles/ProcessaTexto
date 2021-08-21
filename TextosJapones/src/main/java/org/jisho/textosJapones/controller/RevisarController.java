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
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;

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
	private JFXButton btnSalvarAux;

	@FXML
	private JFXButton btnNovoAux;

	@FXML
	private JFXCheckBox cbAnime;

	@FXML
	private JFXCheckBox cbManga;

	@FXML
	private JFXTextField txtVocabulario;

	@FXML
	private JFXTextField txtSimilar;

	@FXML
	private JFXTextField txtPesquisar;

	@FXML
	private JFXCheckBox cbImportarFrase;

	@FXML
	private JFXCheckBox cbCorrecao;

	@FXML
	private JFXTextArea txtAreaIngles;

	@FXML
	private JFXTextArea txtAreaPortugues;

	private RevisarServices service = new RevisarServices();
	private VocabularioServices vocabulario = new VocabularioServices();
	private List<Revisar> similar;
	private Revisar revisando;
	private Vocabulario corrigindo;
	private Robot robot = new Robot();

	@FXML
	private void onBtnSalvar() {
		if ((revisando == null && corrigindo == null) || txtAreaPortugues.getText().isEmpty())
			return;

		Boolean error = false;
		if (revisando != null) {
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
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				error = true;
			}

		} else if (corrigindo != null) {
			corrigindo.setTraducao(txtAreaPortugues.getText());

			try {
				vocabulario.insertOrUpdate(corrigindo);
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				error = true;
			}
		} else
			error = true;

		if (!error) {
			limpaCampos();
			pesquisar();
		}
	}

	private void limpaCampos() {
		try {
			lblRestantes.setText("Restante " + service.selectQuantidadeRestante() + " palavras.");
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			lblRestantes.setText("Restante 0 palavras.");
		}
		corrigindo = null;
		revisando = null;
		txtVocabulario.setText("");
		txtSimilar.setText("");
		txtPesquisar.setText("");
		txtAreaIngles.setText("");
		txtAreaPortugues.setText("");
	}

	private void pesquisar() {
		txtPesquisar.setUnFocusColor(Color.web("#106ebe"));

		try {
			if (cbCorrecao.isSelected()) {
				corrigindo = vocabulario.select(txtPesquisar.getText().trim());
				if (corrigindo.getTraducao().isEmpty())
					corrigindo = null;
			} else
				revisando = service.selectRevisar(txtPesquisar.getText(), cbAnime.isSelected(), cbManga.isSelected());

			if (!txtPesquisar.getText().isEmpty() && revisando == null && corrigindo == null)
				txtPesquisar.setUnFocusColor(Color.RED);

			if (corrigindo != null) {
				txtVocabulario.setText(corrigindo.getVocabulario());
				txtAreaPortugues.setText(corrigindo.getTraducao());
			} else if (revisando != null) {
				similar = service.selectSimilar(revisando.getVocabulario(), revisando.getIngles());

				txtVocabulario.setText(revisando.getVocabulario());
				txtAreaIngles.setText(revisando.getIngles());
				txtAreaPortugues.setText(revisando.getTraducao());

				if (similar.size() > 0)
					txtSimilar.setText(similar.stream().map(Revisar::getVocabulario).collect(Collectors.joining(", ")));
			} else
				limpaCampos();
		} catch (ExcessaoBd e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void onBtnNovo() {
		limpaCampos();
		pesquisar();
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	public void setAnime(Boolean ativo) {
		cbAnime.setSelected(ativo);
	}

	public void setManga(Boolean ativo) {
		cbManga.setSelected(ativo);
	}

	final private String allFlag = ".*";
	final private String japanese = "[\u3041-\u9FAF]";
	final private String notJapanese = "[A-Za-z0-9 ,.à-úÀ-ú\\[\\]\\-\\(\\)]";
	private String frasePortugues = "";

	public void initialize(URL arg0, ResourceBundle arg1) {
		limpaCampos();
		pesquisar();

		txtAreaPortugues.textProperty().addListener((o, oldVal, newVal) -> {
			if (cbImportarFrase.isSelected())
				if (newVal.matches(allFlag + japanese + allFlag))
					Platform.runLater(
							() -> txtAreaPortugues.setText(newVal.replaceFirst(" - ", "").replaceAll(japanese, "")));
		});

		txtPesquisar.textProperty().addListener((o, oldVal, newVal) -> {
			if (cbImportarFrase.isSelected())
				if (newVal.matches(allFlag + notJapanese + allFlag))
					Platform.runLater(() -> {
						txtPesquisar.setText(newVal.replaceFirst(" - ", "").replaceAll(notJapanese, ""));

						if (newVal.matches(allFlag + japanese + allFlag))
							frasePortugues = newVal.replaceFirst(" - ", "").replaceAll(japanese, "");
					});
		});

		txtPesquisar.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				txtPesquisar.setUnFocusColor(Color.web("#106ebe"));
				pesquisar();

				if (revisando != null && cbImportarFrase.isSelected() && !frasePortugues.isEmpty())
					txtAreaPortugues.setText(frasePortugues);

				frasePortugues = "";
			}
		});

		txtPesquisar.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbCorrecao.selectedProperty().addListener((o, oldVal, newVal) -> {
			if (newVal)
				limpaCampos();
		});
	}

	public static URL getFxmlLocate() {
		return RevisarController.class.getResource("/view/Revisar.fxml");
	}

}
