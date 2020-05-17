package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.animation.Animacao;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextArea;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class TelaProcessarFrasesController implements Initializable {

	final static Image imgAnimaBanco = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataBase_48.png"));
	final static Image imgAnimaBancoConec = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataEspera_48.png"));
	final static Image imgAnimaBancoErro = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataSemConexao_48.png"));
	final static Image imgAnimaBancoConectado = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataConectado_48.png"));

	@FXML
	private AnchorPane background;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnSalvar;
	
	@FXML
	private JFXButton btnBanco;

	@FXML
	private ImageView imgConexaoBase;

	@FXML
	private JFXTextArea txtAreaOrigem;

	@FXML
	private JFXTextArea txtAreaNaoEncontrados;

	@FXML
	private JFXTextArea txtAreaProcessar;

	@FXML
	private Label lblProcessBar;

	@FXML
	private JFXProgressBar pgrBarProcessamento;

	private VocabularioServices vocabServ;

	final private Animacao animacao = new Animacao();
	final private Tokenizer tokenizer = new Tokenizer();
	private List<Vocabulario> vocabNovo = new ArrayList<>();

	@FXML
	private void onBtnSalvar() {
		lblProcessBar.setText("Salvando....");
		salvarTexto();
		lblProcessBar.setText("Salvamento concluído.");
	}

	@FXML
	private void onBtnProcessar() {
		processaTexto();
	}

	@FXML
	private void onBtnVerificaConexao() {
		verificaConexao();
	}
	
	public JFXProgressBar getPgrBarProcessamento() {
		return pgrBarProcessamento;
	}

	//
	// UNICODE RANGE : DESCRIPTION
	//
	// 3000-303F : punctuation
	// 3040-309F : hiragana
	// 30A0-30FF : katakana
	// FF00-FFEF : Full-width roman + half-width katakana
	// 4E00-9FAF : Common and uncommon kanji
	//
	// Non-Japanese punctuation/formatting characters commonly used in Japanese text
	// 2605-2606 : Stars
	// 2190-2195 : Arrows
	// u203B : Weird asterisk thing

	private String pattern = ".*[\u4E00-\u9FAF].*";

	private void processaTexto() {
		if (!txtAreaOrigem.getText().isEmpty()) {

			String[] texto = txtAreaOrigem.getText().split("\n");
			String processado = "";
			String naoEncontrado = "";
			vocabNovo.clear();

			setVocabularioServices(new VocabularioServices());

			for (String txt : texto) {
				if (!txt.isEmpty()) {
					List<Token> tokens = tokenizer.tokenize(txt);

					for (Token tk : tokens) {
						if (tk.getSurface().matches(pattern)) {
							Vocabulario vc = vocabServ.select(tk.getSurface());

							if (vc != null) {
								processado += tk.getSurface() + " " + vc.getTraducao() + " ";

								if (vc.getFormaBasica().isEmpty() || vc.getLeitura().isEmpty()) {
									vc.setFormaBasica(tk.getBaseForm());
									vc.setLeitura(tk.getReading());
									vocabServ.update(vc);
								}
							} else {
								processado += tk.getSurface() + " **    ";

								naoEncontrado += tk.getSurface() + "\n";

								vocabNovo.add(new Vocabulario(tk.getSurface(), tk.getBaseForm(), tk.getReading(), ""));
							}
						}
					}
					processado += "\n\n\n\n";
				}
			}

			txtAreaProcessar.setText(processado);
			txtAreaNaoEncontrados.setText(naoEncontrado);
		}
	}

	private void salvarTexto() {
		String[] texto = txtAreaNaoEncontrados.getText().split("\n");

		for (String txt : texto) {
			List<Vocabulario> objencontrado = vocabNovo.stream()
					.filter(p -> p.getVocabulario().equalsIgnoreCase(txt.substring(0, txt.indexOf(" "))))
					.collect(Collectors.toList());

			for (Vocabulario item : objencontrado) {
				item.setTraducao(txt.substring(txt.indexOf(" ")));
			}
		}

		vocabServ.insert(vocabNovo);
		txtAreaNaoEncontrados.setText("");
	}

	private void setVocabularioServices(VocabularioServices vocabServ) {
		this.vocabServ = vocabServ;
	}

	private void verificaConexao() {
		animacao.tmLineImageBanco.play();

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<String> verificaConexao = new Task<String>() {

			@Override
			protected String call() throws Exception {
				return ConexaoMysql.testaConexaoMySQL();
			}

			@Override
			protected void succeeded() {
				animacao.tmLineImageBanco.stop();
				String conectado = getValue();

				if (!conectado.isEmpty()) {
					imgConexaoBase.setImage(imgAnimaBancoConectado);

				} else {
					imgConexaoBase.setImage(imgAnimaBancoErro);
				}

				imgConexaoBase.setFitWidth(30);
				imgConexaoBase.setFitHeight(30);
			}
		};

		Thread t = new Thread(verificaConexao);
		t.setDaemon(true);
		t.start();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		verificaConexao();
		animacao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoConec, 30, 30);

		txtAreaOrigem.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				processaTexto();
			}

		});

	}

	public static URL getFxmlLocate() {
		return TelaProcessarFrasesController.class
				.getResource("/org/jisho/textosJapones/view/TelaProcessarFrases.fxml");
	}

	public static String getIconLocate() {
		return "/org/jisho/textosJapones/resources/images/icoTranslate_128.png";
	}

}
