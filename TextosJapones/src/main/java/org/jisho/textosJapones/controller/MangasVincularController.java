package org.jisho.textosJapones.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VincularServices;
import org.jisho.textosJapones.parse.Parse;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.notification.AlertasPopup;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Pair;

public class MangasVincularController implements Initializable {

	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXComboBox<String> cbBase;

	@FXML
	private JFXTextField txtManga;

	@FXML
	private JFXTextField txtArquivoOriginal;

	@FXML
	private JFXButton btnOriginal;

	@FXML
	private JFXTextField txtArquivoVinculado;

	@FXML
	private JFXButton btnVinculado;

	@FXML
	private JFXButton btnDeletar;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private Spinner<Integer> spnVolume;

	@FXML
	private JFXComboBox<Language> cbLinguagemOrigem;

	@FXML
	private JFXComboBox<Language> cbLinguagemVinculado;

	@FXML
	private JFXButton btnRecarregar;

	@FXML
	private JFXButton btnOrderAutomatico;

	@FXML
	private JFXButton btnOrderPaginaDupla;

	@FXML
	private JFXButton btnOrderPaginaUnica;

	@FXML
	private JFXButton btnOrderSequencia;

	@FXML
	private JFXCheckBox ckbPaginaDuplaCalculada;

	@FXML
	private ListView<VinculoPagina> lvPaginasVinculadas;

	@FXML
	private ListView<VinculoPagina> lvPaginasNaoVinculadas;

	private File arquivoOriginal;
	private File arquivoVinculado;
	private Parse parseOriginal;
	private Parse parseVinculado;

	private VincularServices service = new VincularServices();
	private Vinculo vinculo = new Vinculo();
	private ObservableList<VinculoPagina> vinculado;
	private ObservableList<VinculoPagina> naoVinculado;

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnOriginal() {
		String pasta = arquivoOriginal != null ? arquivoOriginal.getPath()
				: (arquivoVinculado != null ? arquivoVinculado.getPath() : null);
		arquivoOriginal = selectFile("Selecione o arquivo de origem", pasta);
		if (!selecionarArquivo())
			carregarArquivo(arquivoOriginal, true);
		else
			carregaDados(arquivoOriginal, true);
	}

	@FXML
	private void onBtnVinculado() {
		if (arquivoOriginal == null) {
			AlertasPopup.AvisoModal("Selecione o arquivo original", "Necessário informar o arquivo original primeiro");
			return;
		}

		String pasta = arquivoVinculado != null ? arquivoVinculado.getPath()
				: (arquivoOriginal != null ? arquivoOriginal.getPath() : null);
		arquivoVinculado = selectFile("Selecione o arquivo vinculado", pasta);
		if (!selecionarArquivo())
			carregarArquivo(arquivoVinculado, false);
		else
			carregaDados(arquivoOriginal, false);
	}

	@FXML
	private void onBtnDeletar() {
		if (cbBase.getSelectionModel().getSelectedItem() == null || vinculo == null)
			return;

		try {
			service.delete(cbBase.getSelectionModel().getSelectedItem(), vinculo);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro ao deletar", e.getMessage());
		}
	}

	@FXML
	private void onBtnSalvar() {
		if (valida())
			salvar();
	}

	@FXML
	private void onBtnRecarregar() {

	}

	@FXML
	private void onBtnOrderAutomatico() {

	}

	@FXML
	private void onBtnOrderPaginaDupla() {
		service.ordenarPaginaDupla(vinculado, naoVinculado, ckbPaginaDuplaCalculada.isSelected());
	}

	@FXML
	private void onBtnOrderPaginaUnica() {
		service.ordenarPaginaSimples(vinculado, naoVinculado);
	}

	@FXML
	private void onBtnOrderSequencia() {

	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	private void limpar() {
		cbLinguagemVinculado.getSelectionModel().select(Language.PORTUGUESE);
		cbLinguagemOrigem.getSelectionModel().select(Language.JAPANESE);
		txtManga.setText("");
		txtArquivoOriginal.setText("");
		txtArquivoVinculado.setText("");
		ckbPaginaDuplaCalculada.selectedProperty().set(true);
		spnVolume.getValueFactory().setValue(0);

		arquivoOriginal = null;
		arquivoVinculado = null;
		parseOriginal = null;
		parseVinculado = null;
	}

	private Boolean selecionarArquivo() {
		if (cbLinguagemOrigem.getSelectionModel().getSelectedItem() == null && arquivoOriginal == null
				&& cbLinguagemVinculado.getSelectionModel().getSelectedItem() == null && arquivoVinculado == null)
			return false;

		try {
			String arquivoOriginal = this.arquivoOriginal != null ? this.arquivoOriginal.getName() : "";
			String arquivoVinculado = this.arquivoVinculado != null ? this.arquivoVinculado.getName() : "";

			Vinculo vinculo = service.select(cbBase.getSelectionModel().getSelectedItem(), txtManga.getText(),
					spnVolume.getValue(), cbLinguagemOrigem.getSelectionModel().getSelectedItem(), arquivoOriginal,
					cbLinguagemVinculado.getSelectionModel().getSelectedItem(), arquivoVinculado);

			if (vinculo != null) {
				this.vinculo = vinculo;

				cbLinguagemVinculado.getSelectionModel().select(vinculo.getLinguagemVinculado());
				cbLinguagemOrigem.getSelectionModel().select(vinculo.getLinguagemOriginal());
				txtArquivoOriginal.setText(vinculo.getNomeArquivoOriginal());
				txtArquivoVinculado.setText(vinculo.getNomeArquivoVinculado());
				return true;
			}
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro ao abrir o arquivo", e.getMessage());
		}
		return false;
	}

	private Pair<Image, Pair<Boolean, String>> carregaImagem(Parse parse, int pagina) {
		Image image = null;
		InputStream imput = null;
		Boolean dupla = false;
		String md5 = "";
		try {
			imput = parse.getPagina(pagina);
			md5 = Util.MD5(imput);
			image = new Image(imput);
			dupla = (image.getWidth() / image.getHeight()) > 0.9;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new Pair<Image, Pair<Boolean, String>>(image, new Pair<Boolean, String>(dupla, md5));
	}

	private void carregarArquivo(File arquivo, Boolean isManga) {
		Parse parse = Util.criaParse(arquivo);

		if (parse != null) {
			naoVinculado.clear();

			if (isManga) {
				txtArquivoOriginal.setText(arquivo.getName());
				Util.destroiParse(parseOriginal);
				parseOriginal = parse;
				parseVinculado = null;
				txtArquivoVinculado.setText("");

				ArrayList<VinculoPagina> list = new ArrayList<VinculoPagina>();
				for (int x = 0; x <= parseOriginal.getSize(); x++) {
					Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseOriginal, x);
					Pair<Boolean, String> detalhe = image.getValue();
					list.add(new VinculoPagina(Util.getNome(parseOriginal.getPaginaPasta(x)),
							Util.getPasta(parseOriginal.getPaginaPasta(x)), x, parse.getSize(), detalhe.getKey(),
							findPagina(parseOriginal.getPaginaPasta(x), detalhe.getValue(), x), image.getKey()));
				}

				vinculado = FXCollections.observableArrayList(list);
				lvPaginasVinculadas.setItems(vinculado);
			} else {
				txtArquivoVinculado.setText(arquivo.getName());
				Util.destroiParse(parseVinculado);
				parseVinculado = parse;

				for (int x = 0; x <= parseVinculado.getSize(); x++) {
					Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseVinculado, x);
					Pair<Boolean, String> detalhe = image.getValue();

					if (x < vinculado.size()) {
						VinculoPagina item = vinculado.get(x);
						item.limparVinculado();
						item.setVinculadoEsquerdaPagina(x);
						item.setVinculadoEsquerdaNomePagina(Util.getNome(parseVinculado.getPaginaPasta(x)));
						item.setVinculadoEsquerdaPathPagina(Util.getPasta(parseVinculado.getPaginaPasta(x)));
						item.setVinculadoEsquerdaPaginas(parseVinculado.getSize());
						item.isVinculadoEsquerdaPaginaDupla = detalhe.getKey();
						item.setImagemVinculadoEsquerda(image.getKey());
						item.setMangaPaginaEsquerda(
								findPagina(parseVinculado.getPaginaPasta(x), detalhe.getValue(), x));
					} else
						naoVinculado.add(new VinculoPagina(Util.getNome(parseVinculado.getPaginaPasta(x)),
								Util.getPasta(parseVinculado.getPaginaPasta(x)), x, parseVinculado.getSize(),
								detalhe.getKey(), findPagina(parseVinculado.getPaginaPasta(x), detalhe.getValue(), x),
								image.getKey(), true));
				}
			}
		}
	}

	private void carregaDados(File arquivo, Boolean isManga) {
		if (isManga) {
			Util.destroiParse(parseOriginal);
			parseOriginal = Util.criaParse(arquivo);
		} else {
			Util.destroiParse(parseVinculado);
			parseVinculado = Util.criaParse(arquivo);

		}

		for (VinculoPagina pagina : vinculado) {
			if (isManga) {
				Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseOriginal, pagina.getOriginalPagina());
				pagina.isOriginalPaginaDupla = image.getValue().getKey();
				pagina.setImagemOriginal(image.getKey());
			} else {
				if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
					Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseVinculado,
							pagina.getVinculadoEsquerdaPagina());
					pagina.isVinculadoEsquerdaPaginaDupla = image.getValue().getKey();
					pagina.setImagemVinculadoEsquerda(image.getKey());
				}

				if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA) {
					Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseVinculado,
							pagina.getVinculadoDireitaPagina());
					pagina.isVinculadoDireitaPaginaDupla = image.getValue().getKey();
					pagina.setImagemVinculadoDireita(image.getKey());
				}
			}
		}

		if (!isManga) {
			for (VinculoPagina pagina : naoVinculado) {
				if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
					Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseVinculado,
							pagina.getVinculadoEsquerdaPagina());
					pagina.isVinculadoEsquerdaPaginaDupla = image.getValue().getKey();
					pagina.setImagemVinculadoEsquerda(image.getKey());
				}
			}
		}
	}

	private Boolean valida() {
		return true;
	}

	private void salvar() {
		vinculo.setUltimaAlteracao(LocalDateTime.now());
		vinculo.setBase(cbBase.getSelectionModel().getSelectedItem());
		vinculo.setLinguagemOriginal(cbLinguagemOrigem.getSelectionModel().getSelectedItem());
		vinculo.setLinguagemVinculado(cbLinguagemVinculado.getSelectionModel().getSelectedItem());
		vinculo.setNomeArquivoOriginal(txtArquivoOriginal.getText());
		vinculo.setNomeArquivoVinculado(txtArquivoVinculado.getText());
		vinculo.setVinculados(vinculado);
		vinculo.setNaoVinculados(naoVinculado);

		try {
			service.salvar(cbBase.getSelectionModel().getSelectedItem(), vinculo);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro ao salvar", e.getMessage());
		}
	}

	private File selectFile(String titulo, String pasta) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(titulo);

		if (pasta != null && !pasta.isEmpty())
			fileChooser.setInitialDirectory(new File(pasta));

		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ALL FILES", "*.*"),
				new FileChooser.ExtensionFilter("ZIP", "*.zip"), new FileChooser.ExtensionFilter("CBZ", "*.cbz"),
				new FileChooser.ExtensionFilter("RAR", "*.rar"), new FileChooser.ExtensionFilter("CBR", "*.cbr"));

		return fileChooser.showOpenDialog(null);
	}

	private Boolean isExtra = false;
	private Float capitulo = -1f;

	private MangaPagina findPagina(String caminho, String hash, Integer pagina) {
		MangaVolume volume = vinculo.getVolumeOriginal();
		MangaPagina manga = null;

		isExtra = false;
		capitulo = -1f;
		String arquivo = Util.getNome(caminho);
		String pasta = Util.getPasta(caminho).toLowerCase();
		if (pasta.contains("capítulo"))
			capitulo = Float.valueOf(pasta.substring(pasta.indexOf("capítulo")).replaceAll("[^\\d.]", ""));
		else if (pasta.contains("capitulo"))
			capitulo = Float.valueOf(pasta.substring(pasta.indexOf("capitulo")).replaceAll("[^\\d.]", ""));
		else if (pasta.contains("extra")) {
			capitulo = Float.valueOf(pasta.substring(pasta.indexOf("extra")).replaceAll("[^\\d.]", ""));
			isExtra = true;
		}

		if (capitulo > -1) {
			List<MangaCapitulo> capitulos = volume.getCapitulos().stream()
					.filter((it) -> it.getCapitulo().compareTo(capitulo) == 0 && it.isExtra() == isExtra)
					.collect(Collectors.toList());

			Optional<MangaCapitulo> item = capitulos.stream()
					.filter((it) -> it.getPaginas().stream().anyMatch((pg) -> pg.getHash().equalsIgnoreCase(hash)))
					.findFirst();

			if (item.isPresent())
				manga = item.get().getPaginas().stream().filter((it) -> it.getHash().equalsIgnoreCase(hash)).findFirst()
						.get();

			if (manga == null) {
				item = capitulos.stream().filter(
						(it) -> it.getPaginas().stream().anyMatch((pg) -> pg.getNomePagina().equalsIgnoreCase(arquivo)))
						.findFirst();

				if (item.isPresent())
					manga = item.get().getPaginas().stream()
							.filter((it) -> it.getNomePagina().equalsIgnoreCase(arquivo)).findFirst().get();
			}

			if (manga == null) {
				item = capitulos.stream().filter(
						(it) -> it.getPaginas().stream().anyMatch((pg) -> pg.getPagina().compareTo(pagina) == 0))
						.findFirst();

				if (item.isPresent())
					manga = item.get().getPaginas().stream().filter((it) -> it.getPagina().compareTo(pagina) == 0)
							.findFirst().get();
			}

		} else {
			boolean parar = false;
			for (MangaCapitulo cap : volume.getCapitulos()) {
				for (MangaPagina pag : cap.getPaginas()) {
					if (pag.getHash().equalsIgnoreCase(hash)) {
						manga = pag;
						parar = true;
						break;
					}
					if (parar)
						break;
				}
				if (parar)
					break;
			}

			if (manga == null) {
				parar = false;
				for (MangaCapitulo cap : volume.getCapitulos()) {
					for (MangaPagina pag : cap.getPaginas()) {
						if (pag.getNomePagina().equalsIgnoreCase(arquivo)) {
							manga = pag;
							parar = true;
							break;
						}
						if (parar)
							break;
					}
					if (parar)
						break;
				}
			}

			if (manga == null) {
				parar = false;
				for (MangaCapitulo cap : volume.getCapitulos()) {
					for (MangaPagina pag : cap.getPaginas()) {
						if (pag.getPagina().compareTo(pagina) == 0) {
							manga = pag;
							parar = true;
							break;
						}
						if (parar)
							break;
					}
					if (parar)
						break;
				}
			}
		}

		return manga;
	}

	private void linkaCelulas() {
		lvPaginasVinculadas.setCellFactory(new Callback<ListView<VinculoPagina>, ListCell<VinculoPagina>>() {
			@Override
			public ListCell<VinculoPagina> call(ListView<VinculoPagina> studentListView) {
				ListCell<VinculoPagina> cell = new ListCell<VinculoPagina>() {
					@Override
					public void updateItem(VinculoPagina item, boolean empty) {
						super.updateItem(item, empty);

						if (empty || item == null) {
							setText(null);
							setGraphic(null);
						} else {
							FXMLLoader mLLoader = new FXMLLoader(MangaVincularCelulaController.getFxmlLocate());
							MangaVincularCelulaController controller = mLLoader.getController();

							try {
								mLLoader.load();
							} catch (IOException e) {
								e.printStackTrace();
							}

							controller.setDados(item);

							setText(null);
							setGraphic(controller.hbRoot);
						}
					}
				};

				return cell;
			}
		});
	}

	private Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			cbBase.getItems().setAll(service.getTabelas());
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro ao carregar as tabelas", e.getMessage());
		}

		cbBase.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			System.out.println(newValue);
		});

		txtManga.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		spnVolume.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbLinguagemOrigem.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbLinguagemVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtArquivoOriginal.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtArquivoVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		linkaCelulas();

		spnVolume.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
		cbLinguagemVinculado.getItems().addAll(Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
				Language.PORTUGUESE_GOOGLE);
		cbLinguagemOrigem.getItems().addAll(Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
				Language.PORTUGUESE_GOOGLE);

		limpar();
	}

	public static URL getFxmlLocate() {
		return MangasVincularController.class.getResource("/view/MangaVincular.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
