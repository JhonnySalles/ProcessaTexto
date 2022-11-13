package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarJaponesServices;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.jisho.textosJapones.processar.JapanDict;
import org.jisho.textosJapones.processar.Jisho;
import org.jisho.textosJapones.processar.Kanshudo;
import org.jisho.textosJapones.processar.Tangorin;
import org.jisho.textosJapones.processar.TanoshiJapanese;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.jisho.textosJapones.util.Util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
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
	private JFXButton btnFormatar;

	@FXML
	private JFXCheckBox cbDuplicados;

	@FXML
	private JFXCheckBox cbSubstituirKanji;

	@FXML
	private JFXButton btnExcluir;

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
	private JFXCheckBox cbSimilar;

	@FXML
	private JFXTextField txtPesquisar;

	@FXML
	private JFXCheckBox cbCorrecao;

	@FXML
	private JFXComboBox<Api> cbContaGoolge;

	@FXML
	private JFXButton btnTraduzir;

	@FXML
	private JFXButton btnJapaneseTanoshi;

	@FXML
	private JFXButton btnJapanDict;

	@FXML
	private JFXButton btnJisho;

	@FXML
	private JFXButton btnKanshudo;

	@FXML
	private JFXTextArea txtAreaIngles;

	@FXML
	private JFXTextArea txtAreaPortugues;

	private RevisarJaponesServices service = new RevisarJaponesServices();
	private VocabularioJaponesServices vocabulario = new VocabularioJaponesServices();
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

	@FXML
	private void onBtnExcluir() {
		if (revisando == null)
			return;

		Boolean error = false;
		if (revisando != null) {
			try {
				service.delete(revisando);
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

	@FXML
	private void onBtnFormatar() {
		if (txtAreaPortugues.getText().isEmpty())
			return;

		if (cbDuplicados.isSelected())
			txtAreaPortugues.setText(Util.removeDuplicate(txtAreaPortugues.getText()));
		else
			txtAreaPortugues.setText(Util.normalize(txtAreaPortugues.getText()));
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
		limpaTextos();
		txtPesquisar.setText("");
	}

	private void limpaTextos() {
		txtVocabulario.setText("");
		txtSimilar.setText("");
		txtAreaIngles.setText("");
		txtAreaPortugues.setText("");
	}

	private Boolean pesquisaCorrecao(String pesquisar) throws ExcessaoBd {
		corrigindo = vocabulario.select(pesquisar);
		if (corrigindo != null && !corrigindo.getTraducao().isEmpty()) {
			limpaTextos();
			if (corrigindo != null) {
				txtVocabulario.setText(corrigindo.getVocabulario());
				txtAreaPortugues.setText(Util.normalize(corrigindo.getTraducao()));
			}

			return true;
		} else
			return false;
	}

	private Boolean pesquisaRevisao(String pesquisar) throws ExcessaoBd {
		revisando = service.selectRevisar(pesquisar, cbAnime.isSelected(), cbManga.isSelected());
		if (revisando != null) {
			limpaTextos();
			if (cbSimilar.isSelected())
				similar = service.selectSimilar(revisando.getVocabulario(), revisando.getIngles());
			else
				similar = new ArrayList<Revisar>();

			txtVocabulario.setText(revisando.getVocabulario());
			txtAreaIngles.setText(revisando.getIngles());
			txtAreaPortugues.setText(Util.normalize(revisando.getTraducao()));

			if (similar.size() > 0)
				txtSimilar.setText(similar.stream().map(Revisar::getVocabulario).collect(Collectors.joining(", ")));
			else
				txtSimilar.setText("");

			return true;
		} else
			return false;
	}

	public void pesquisar() {
		txtPesquisar.setUnFocusColor(Color.web("#106ebe"));
		String pesquisar = txtPesquisar.getText().trim();

		try {
			if (pesquisar.isEmpty())
				pesquisaRevisao(pesquisar);
			else if (cbCorrecao.isSelected() && pesquisaCorrecao(pesquisar))
				return;
			else if (pesquisaRevisao(pesquisar))
				return;
			else {
				txtPesquisar.setUnFocusColor(Color.RED);

				if (!cbCorrecao.isSelected())
					cbCorrecao.setSelected(true);
				else
					limpaCampos();
			}
		} catch (ExcessaoBd e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void onBtnNovo() {
		limpaCampos();
		pesquisar();
	}

	@FXML
	private void onBtnTraduzir() {
		if (txtAreaIngles.getText().isEmpty())
			return;

		try {
			String texto = Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
					Language.PORTUGUESE.getSigla(), txtAreaIngles.getText(), cbContaGoolge.getValue()));

			txtAreaPortugues.setText(Util.normalize(texto));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void onBtnJapaneseTanoshi() {
		if (txtVocabulario.getText().isEmpty())
			return;

		txtAreaIngles.setText(TanoshiJapanese.processa(txtVocabulario.getText()));
		onBtnTraduzir();
	}

	@FXML
	private void onBtnTangorin() {
		if (txtVocabulario.getText().isEmpty())
			return;

		txtAreaIngles.setText(Tangorin.processa(txtVocabulario.getText()));
		onBtnTraduzir();
	}

	@FXML
	private void onBtnJapanDict() {
		if (txtVocabulario.getText().isEmpty())
			return;

		txtAreaIngles.setText(JapanDict.processa(txtVocabulario.getText()));
		onBtnTraduzir();
	}

	@FXML
	private void onBtnJisho() {
		if (txtVocabulario.getText().isEmpty())
			return;

		txtAreaIngles.setText(Jisho.processa(txtVocabulario.getText()));
		onBtnTraduzir();
	}

	@FXML
	private void onBtnKanshudo() {
		if (txtVocabulario.getText().isEmpty())
			return;

		txtAreaIngles.setText(Kanshudo.processa(txtVocabulario.getText()));
		onBtnTraduzir();
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
	final private String notJapanese = "[A-Za-z0-9 ,;.à-úÀ-ú\\[\\]\\-\\(\\)]";
	private String frasePortugues = "";

	public void initialize(URL arg0, ResourceBundle arg1) {
		cbContaGoolge.getItems().addAll(Api.values());
		cbContaGoolge.getSelectionModel().selectLast();

		limpaCampos();

		txtAreaPortugues.textProperty().addListener((o, oldVal, newVal) -> {
			if (cbSubstituirKanji.isSelected())
				if (newVal.matches(allFlag + japanese + allFlag))
					Platform.runLater(
							() -> txtAreaPortugues.setText(newVal.replaceFirst(" - ", "").replaceAll(japanese, "")));
		});

		txtPesquisar.textProperty().addListener((o, oldVal, newVal) -> {
			if (cbSubstituirKanji.isSelected())
				if (newVal.matches(allFlag + notJapanese + allFlag))
					Platform.runLater(() -> {
						String texto = newVal.replaceFirst(" - ", "").replaceAll("¹", "");
						txtPesquisar.setText(texto.replaceAll(notJapanese, ""));

						if (newVal.matches(allFlag + japanese + allFlag))
							frasePortugues = texto.replaceAll(japanese, "");
					});
		});

		txtPesquisar.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				txtPesquisar.setUnFocusColor(Color.web("#106ebe"));
				pesquisar();

				if (revisando != null && cbSubstituirKanji.isSelected() && !frasePortugues.isEmpty()) {
					txtAreaPortugues.setText(frasePortugues);
					onBtnFormatar();
				}

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
				pesquisar();
			else
				limpaCampos();
		});

		cbSimilar.selectedProperty().addListener((o, oldVal, newVal) -> {
			if (cbSimilar.isSelected())
				pesquisar();
			else {
				txtSimilar.setText("");
				similar = new ArrayList<Revisar>();
			}
		});

		cbAnime.selectedProperty().addListener((o, oldVal, newVal) -> {
			pesquisar();
		});

		cbManga.selectedProperty().addListener((o, oldVal, newVal) -> {
			pesquisar();
		});

		pesquisar();
	}

	public static URL getFxmlLocate() {
		return RevisarController.class.getResource("/view/Revisar.fxml");
	}

}
