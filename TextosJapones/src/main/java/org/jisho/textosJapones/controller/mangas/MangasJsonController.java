package org.jisho.textosJapones.controller.mangas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.database.mysql.ConexaoMysql;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaVinculo;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.MangaServices;
import org.jisho.textosJapones.model.services.VincularServices;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXAutoCompletePopup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

public class MangasJsonController implements Initializable {

	final PseudoClass vinculado = PseudoClass.getPseudoClass("vinculado");

	@FXML
	private AnchorPane apRoot;

	@FXML
	private JFXComboBox<String> cbBase;

	@FXML
	private JFXTextField txtManga;

	@FXML
	private JFXComboBox<Language> cbLinguagem;

	@FXML
	private Spinner<Integer> spnVolume;

	@FXML
	private Spinner<Double> spnCapitulo;

	@FXML
	private JFXTextField txtCaminhoSalvar;

	@FXML
	private JFXButton btnCaminhoSalvar;

	@FXML
	private JFXButton btnCarregar;

	@FXML
	private JFXButton btnGerarJson;

	@FXML
	private JFXCheckBox ckbSepararPorCapitulos;

	@FXML
	private JFXCheckBox ckbInverterOrdemTexto;

	@FXML
	private JFXCheckBox ckbInserirArquivos;

	@FXML
	private JFXCheckBox ckbExcluirAoInserirArquivos;

	@FXML
	private JFXCheckBox ckbCarregaVinculos;

	@FXML
	private JFXCheckBox ckbApenasVinculos;

	@FXML
	private JFXCheckBox ckbMarcarTodos;

	@FXML
	private TreeTableView<Manga> treeBases;

	@FXML
	private TreeTableColumn<Manga, Boolean> treecMacado;

	@FXML
	private TreeTableColumn<Manga, String> treecBase;

	@FXML
	private TreeTableColumn<Manga, String> treecManga;

	@FXML
	private TreeTableColumn<Manga, Language> treecLinguagem;

	@FXML
	private TreeTableColumn<Manga, Integer> treecVolume;

	@FXML
	private TreeTableColumn<Manga, Float> treecCapitulo;

	private MangaServices serviceManga = new MangaServices();
	private VincularServices serviceVinculo = new VincularServices();
	private ObservableList<MangaTabela> TABELAS;
	private Boolean PAUSAR;

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnCarregar() {
		carregar();
	}

	@FXML
	private void onBtnGerarJson() {
		if (txtCaminhoSalvar.getText().isEmpty()) {
			AlertasPopup.AvisoModal("Aviso", "Necessário informar um caminho de destino.");
			return;
		}

		if (ckbInserirArquivos.isSelected() && ConexaoMysql.getCaminhoWinrar().isEmpty()) {
			AlertasPopup.AvisoModal("Aviso", "Necessário informar o caminho do winrar nas configurações.");
			return;
		}

		if (btnGerarJson.getAccessibleText().equalsIgnoreCase("GERANDO")) {
			PAUSAR = true;
			return;
		}

		btnGerarJson.setAccessibleText("GERANDO");
		btnGerarJson.setText("Pausar");
		btnCarregar.setDisable(true);
		treeBases.setDisable(true);

		gerar();
	}

	@FXML
	private void onBtnCarregarCaminhoSalvar() {
		String caminho = selecionaPasta(btnCaminhoSalvar.getText());
		txtCaminhoSalvar.setText(caminho);
	}

	private String selecionaPasta(String pasta) {
		DirectoryChooser fileChooser = new DirectoryChooser();
		fileChooser.setTitle("Selecione a pasta de destino");

		if (pasta != null && !pasta.isEmpty())
			fileChooser.setInitialDirectory(new File(pasta));
		File caminho = fileChooser.showDialog(null);

		if (caminho == null)
			return "";
		else
			return caminho.getAbsolutePath();
	}

	@FXML
	private void onBtnMarcarTodos() {
		marcarTodosFilhos(treeBases.getRoot(), ckbMarcarTodos.isSelected());
		treeBases.refresh();
	}

	public void habilitar() {
		treeBases.setDisable(false);
		btnGerarJson.setAccessibleText("GERAR");
		btnGerarJson.setText("Gerar Json");
		btnCarregar.setDisable(false);
		TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
	}

	private String patern = ".*\\.(zip|cbz|rar|cbr|tar)$";

	private void insereDentroArquivos(String localPasta, String nomeArquivo, String nome, Integer volume,
			String localJson) {
		File pasta = new File(localPasta);
		File arquivo = null;

		if (!nomeArquivo.isEmpty()) {
			for (File item : pasta.listFiles()) {
				if (item.getName().toLowerCase().contains(".json"))
					continue;

				if (!item.getName().toLowerCase().matches(patern))
					continue;

				if (item.getName().toLowerCase().contains(nomeArquivo)) {
					arquivo = item;
					break;
				}
			}
		}

		if (arquivo == null) {
			for (File item : pasta.listFiles()) {
				if (item.getName().toLowerCase().contains(".json"))
					continue;

				if (!item.getName().toLowerCase().matches(patern))
					continue;

				if (item.getName().toLowerCase().contains("(jap)") || item.getName().toLowerCase().contains("(jpn)")) {
					if (item.getName().toLowerCase().contains(nome)
							&& (item.getName().toLowerCase().contains("volume " + String.format("%02d", volume) + " ")
									|| item.getName().toLowerCase()
											.contains("volume " + String.format("%03d", volume) + " "))) {
						arquivo = item;
						break;
					}
				} else if (item.getName().toLowerCase().contains(nome)
						&& (item.getName().toLowerCase().contains("volume " + String.format("%02d", volume))
								|| item.getName().toLowerCase().contains("volume " + String.format("%03d", volume)))) {
					arquivo = item;
					break;
				}
			}
		}

		// Necessário adicionar o winrar no path do windows.
		if (arquivo != null) {
			File json = new File(localJson);

			String comando = "cmd.exe /C cd " + winrar + " \n&&rar a -ep " + '"' + arquivo.getPath() + '"' + " " + '"'
					+ json.getPath() + '"';
			System.out.println("cmd.exe /C cd " + winrar);
			System.out.println("rar a -ep " + '"' + arquivo.getPath() + '"' + " " + '"' + json.getPath() + '"');
			try {
				Runtime rt = Runtime.getRuntime();

				Process proc = rt.exec(comando);
				System.out.println("Resultado: " + proc.waitFor());

				String resultado = "";

				BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

				String s = null;
				while ((s = stdInput.readLine()) != null)
					resultado += s + "\n";

				if (!resultado.isEmpty())
					System.out.println("Output comand:\n" + resultado);

				s = null;
				resultado = "";
				BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

				while ((s = stdError.readLine()) != null)
					resultado += s + "\n";

				if (!resultado.isEmpty())
					System.out.println("Error comand:\n" + resultado
							+ "\nNecessário adicionar o rar no path e reiniciar a aplicação.");

				if (excluirAoInserir)
					json.delete();

			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}

	private Boolean isSepararCapitulo;
	private Integer I;
	private String error, destino;
	private Boolean inserirArquivos, excluirAoInserir;
	private String winrar;

	private void gerar() {
		GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
		isSepararCapitulo = ckbSepararPorCapitulos.isSelected();
		destino = txtCaminhoSalvar.getText();

		PAUSAR = false;

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		progress.getTitulo().setText("Json");
		Task<Void> gerarJson = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					inserirArquivos = ckbInserirArquivos.isSelected();
					excluirAoInserir = ckbExcluirAoInserirArquivos.isSelected();
					winrar = ConexaoMysql.getCaminhoWinrar();
					error = "";

					updateMessage("Gravando Jsons....");

					ExclusionStrategy removeLinguagemCapitulo = new ExclusionStrategy() {
						@Override
						public boolean shouldSkipField(FieldAttributes field) {
							if (field.getDeclaringClass() == MangaCapitulo.class && field.getName().equals("lingua"))
								return true;

							if (field.getDeclaringClass() == MangaCapitulo.class && field.getName().equals("manga"))
								return true;

							if (field.getDeclaringClass() == MangaCapitulo.class && field.getName().equals("volume"))
								return true;

							return false;
						}

						@Override
						public boolean shouldSkipClass(Class<?> clazz) {
							return false;
						}
					};

					Gson gson = null;

					if (isSepararCapitulo)
						gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					else
						gson = new GsonBuilder().addSerializationExclusionStrategy(removeLinguagemCapitulo)
								.excludeFieldsWithoutExposeAnnotation().create();

					I = 0;
					for (MangaTabela tabela : TABELAS) {
						I++;

						if (!tabela.isProcessar()) {

							Platform.runLater(() -> {
								if (TaskbarProgressbar.isSupported())
									TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, TABELAS.size(),
											Type.NORMAL);
							});

							continue;
						}

						for (MangaVolume volume : tabela.getVolumes()) {

							if (!volume.isProcessar())
								continue;

							if (isSepararCapitulo) {
								for (MangaCapitulo capitulo : volume.getCapitulos()) {
									if (!capitulo.isProcessar())
										continue;

									String arquivo = destino + '/' + volume.getLingua() + " - " + volume.getManga()
											+ " - Volume " + String.format("%03d", volume.getVolume()) + " Capitulo "
											+ String.format("%03d", capitulo.getCapitulo()) + ".json";

									FileWriter file = new FileWriter(arquivo);
									gson.toJson(capitulo, file);
									file.flush();
									file.close();

									if (inserirArquivos)
										insereDentroArquivos(destino, volume.getArquivo().toLowerCase(),
												volume.getManga().toLowerCase(), volume.getVolume(), arquivo);

									if (PAUSAR)
										break;
								}
							} else {
								String arquivo = destino + '/' + volume.getLingua() + " - " + volume.getManga()
										+ " - Volume " + String.format("%03d", volume.getVolume()) + ".json";

								FileWriter file = new FileWriter(arquivo);
								gson.toJson(volume, file);
								file.flush();
								file.close();

								if (inserirArquivos)
									insereDentroArquivos(destino, volume.getArquivo().toLowerCase(),
											volume.getManga().toLowerCase(), volume.getVolume(), arquivo);
							}

						}

						Platform.runLater(() -> {
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, TABELAS.size(),
										Type.NORMAL);
						});

						if (PAUSAR)
							break;
					}

				} catch (Exception e) {
					e.printStackTrace();
					error = e.getMessage();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.failed();
				Platform.runLater(() -> {

					progress.getBarraProgresso().progressProperty().unbind();
					progress.getLog().textProperty().unbind();
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
					MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

					if (!error.isEmpty())
						AlertasPopup.ErroModal("Erro", error);
					else
						AlertasPopup.AvisoModal("Aviso", "Jsons gerado com sucesso.");

					habilitar();
				});

			}

			@Override
			protected void failed() {
				super.failed();
				System.out.println("Erro na thread gerar json: " + super.getMessage());
				habilitar();
			}
		};

		progress.getBarraProgresso().progressProperty().bind(gerarJson.progressProperty());
		progress.getLog().textProperty().bind(gerarJson.messageProperty());

		Thread t = new Thread(gerarJson);
		t.start();
	}

	private String BASE;
	private String MANGA;
	private Integer VOLUME;
	private Float CAPITULO;
	private Language LINGUAGEM;
	private TreeItem<Manga> DADOS;

	private void carregar() {
		MenuPrincipalController.getController().getLblLog().setText("Carregando json...");

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		btnCarregar.setDisable(true);
		btnGerarJson.setDisable(true);
		treeBases.setDisable(true);
		BASE = cbBase.getSelectionModel().getSelectedItem() == null ? ""
				: cbBase.getSelectionModel().getSelectedItem().trim();
		MANGA = txtManga.getText().trim();
		VOLUME = spnVolume.getValue();
		CAPITULO = spnCapitulo.getValue().floatValue();
		LINGUAGEM = cbLinguagem.getSelectionModel().getSelectedItem();

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> carregaItens = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					serviceManga = new MangaServices();

					if (ckbCarregaVinculos.isSelected() && ckbApenasVinculos.isSelected()) {
						TABELAS = FXCollections.observableArrayList(
								serviceVinculo.selectTabelasJson(BASE, MANGA, VOLUME, CAPITULO, LINGUAGEM));
					} else {
						TABELAS = FXCollections.observableArrayList(serviceManga.selectTabelasJson(BASE, MANGA, VOLUME,
								CAPITULO, LINGUAGEM, ckbInverterOrdemTexto.isSelected()));

						if (ckbCarregaVinculos.isSelected())
							for (MangaTabela tabela : TABELAS)
								tabela.setVinculados(serviceVinculo.getMangaVinculo(tabela.getBase(), MANGA, VOLUME,
										CAPITULO, LINGUAGEM));
					}

					DADOS = getTreeData();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				Platform.runLater(() -> {
					treeBases.setRoot(DADOS);

					MenuPrincipalController.getController().getLblLog().setText("");
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

					ckbMarcarTodos.setSelected(true);
					btnCarregar.setDisable(false);
					btnGerarJson.setDisable(false);
					treeBases.setDisable(false);
				});

			}

			@Override
			protected void failed() {
				super.failed();
				System.out.println("Erro na thread de carregamento de itens: " + super.getMessage());
				habilitar();
			}
		};
		Thread t = new Thread(carregaItens);
		t.start();
	}

	private String getLinguaVinculo(MangaVinculo vinculo) {
		String lingua = vinculo.getManga().getLingua().getSigla().toUpperCase() + " | ";
		for (MangaVolume vi : vinculo.getVinculos()) {
			lingua += vi.getLingua().getSigla().toUpperCase() + " -- ";
		}

		lingua = lingua.substring(0, lingua.lastIndexOf(" -- "));

		return lingua;
	}

	private TreeItem<Manga> getTreeData() {
		TreeItem<Manga> itmRoot = new TreeItem<Manga>(new Manga("...", ""));
		for (MangaTabela tabela : TABELAS) {
			tabela.setManga("...");
			TreeItem<Manga> itmTabela = new TreeItem<Manga>(tabela);
			TreeItem<Manga> itmManga = null;
			TreeItem<Manga> itmLingua = null;
			String volumeAnterior = "";
			Language linguagemAnterior = null;

			// ---------------- Mangas ---------------- //
			for (MangaVolume volume : tabela.getVolumes()) {

				// Implementa um nivel por tipo
				if (!volume.getManga().equalsIgnoreCase(volumeAnterior) || itmManga == null) {
					volumeAnterior = volume.getManga();
					itmManga = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), "..."));
					itmTabela.getChildren().add(itmManga);
					itmTabela.setExpanded(true);

					itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(),
							volume.getLingua().getSigla().toUpperCase()));
					linguagemAnterior = volume.getLingua();
					itmManga.getChildren().add(itmLingua);
				}

				if (linguagemAnterior == null || volume.getLingua().compareTo(linguagemAnterior) != 0) {
					itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(),
							volume.getLingua().getSigla().toUpperCase()));
					linguagemAnterior = volume.getLingua();
					itmManga.getChildren().add(itmLingua);
				}

				volume.setLinguagem(linguagemAnterior.getSigla().toUpperCase());
				volume.setBase(tabela.getBase());
				TreeItem<Manga> itmVolume = new TreeItem<Manga>(volume);

				for (MangaCapitulo capitulo : volume.getCapitulos()) {
					capitulo.setBase(tabela.getBase());
					capitulo.setNomePagina("...");
					capitulo.setLinguagem(linguagemAnterior.getSigla().toUpperCase());
					itmVolume.getChildren().add(new TreeItem<Manga>(capitulo));
				}

				itmLingua.getChildren().add(itmVolume);
			}

			String lingua = "";
			String linguaAnterior = "";

			// ---------------- Vinculados ---------------- //
			for (MangaVinculo vinculo : tabela.getVinculados()) {
				linguagemAnterior = null;

				MangaVolume volume = vinculo.getManga();
				volume.isVinculo = true;
				lingua = getLinguaVinculo(vinculo);

				// Implementa um nivel por tipo
				if (!volume.getManga().equalsIgnoreCase(volumeAnterior) || itmManga == null) {
					volumeAnterior = volume.getManga();
					itmManga = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), "...", true));
					itmTabela.getChildren().add(itmManga);
					itmTabela.setExpanded(true);

					itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), lingua, true));
					linguagemAnterior = volume.getLingua();
					linguaAnterior = getLinguaVinculo(vinculo);
					itmManga.getChildren().add(itmLingua);
				}

				if (linguagemAnterior == null || volume.getLingua().compareTo(linguagemAnterior) != 0) {
					itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), lingua, true));
					linguagemAnterior = volume.getLingua();
					linguaAnterior = getLinguaVinculo(vinculo);
					itmManga.getChildren().add(itmLingua);
				}

				volume.setLinguagem(linguaAnterior);
				volume.setBase(tabela.getBase());
				TreeItem<Manga> itmVolume = new TreeItem<Manga>(volume);

				for (MangaCapitulo capitulo : volume.getCapitulos()) {
					capitulo.setBase(tabela.getBase());
					capitulo.setNomePagina("...");
					capitulo.setLinguagem(linguaAnterior);
					capitulo.isVinculo = true;
					itmVolume.getChildren().add(new TreeItem<Manga>(capitulo));
				}

				itmLingua.getChildren().add(itmVolume);
			}

			// ---------------- Adicionado na tabela ---------------- //
			itmRoot.getChildren().add(itmTabela);
			itmRoot.setExpanded(true);
		}
		return itmRoot;
	}

	private void marcarTodosFilhos(TreeItem<Manga> treeItem, Boolean newValue) {
		treeItem.getValue().setProcessar(newValue);
		treeItem.getChildren().forEach(treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue));
	}

	private void ativaTodosPai(TreeItem<Manga> treeItem, Boolean newValue) {
		if (treeItem.getParent() != null) {
			treeItem.getParent().getValue().setProcessar(newValue);
			ativaTodosPai(treeItem.getParent(), newValue);
		}
	}

	private void editaColunas() {
		// ==== (CHECK-BOX) ===
		treecMacado.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<Manga, Boolean>, ObservableValue<Boolean>>() {

					@Override
					public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<Manga, Boolean> param) {
						TreeItem<Manga> treeItem = param.getValue();
						Manga item = treeItem.getValue();
						SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isProcessar());

						booleanProp.addListener(new ChangeListener<Boolean>() {
							@Override
							public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
									Boolean newValue) {
								item.setProcessar(newValue);
								marcarTodosFilhos(treeItem, newValue);
								if (newValue) // Somente ativa caso seja true, pois ao menos um nó precisa estar ativo
									ativaTodosPai(treeItem, newValue);

								treeBases.refresh();
							}
						});

						return booleanProp;
					}
				});

		treecMacado.setCellFactory(new Callback<TreeTableColumn<Manga, Boolean>, TreeTableCell<Manga, Boolean>>() {
			@Override
			public TreeTableCell<Manga, Boolean> call(TreeTableColumn<Manga, Boolean> p) {
				CheckBoxTreeTableCellCustom<Manga, Boolean> cell = new CheckBoxTreeTableCellCustom<Manga, Boolean>();
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

		treeBases.setRowFactory(tv -> {
			return new TreeTableRow<>() {
				@Override
				public void updateItem(Manga item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null) {
						setStyle("");
						pseudoClassStateChanged(vinculado, false);
					} else
						pseudoClassStateChanged(vinculado, item.isVinculo);

				}
			};
		});

	}

	private void linkaCelulas() {
		treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<Manga, Boolean>("processar"));
		treecBase.setCellValueFactory(new TreeItemPropertyValueFactory<>("base"));
		treecManga.setCellValueFactory(new TreeItemPropertyValueFactory<>("manga"));
		treecLinguagem.setCellValueFactory(new TreeItemPropertyValueFactory<>("linguagem"));
		treecVolume.setCellValueFactory(new TreeItemPropertyValueFactory<>("volume"));
		treecCapitulo.setCellValueFactory(new TreeItemPropertyValueFactory<>("capitulo"));
		treeBases.setShowRoot(false);

		editaColunas();

	}

	private Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			cbBase.getItems().setAll(serviceManga.getTabelas());
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro ao carregar as tabelas", e.getMessage());
		}

		cbBase.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (!newValue.isEmpty()) {
				if (cbBase.getItems().isEmpty())
					cbBase.setUnFocusColor(Color.RED);
				else
					cbBase.setUnFocusColor(Color.web("#106ebe"));
			}
		});

		JFXAutoCompletePopup<String> autoCompletePopup = new JFXAutoCompletePopup<>();
		autoCompletePopup.getSuggestions().addAll(cbBase.getItems());

		autoCompletePopup.setSelectionHandler(event -> {
			cbBase.setValue(event.getObject());
		});

		cbBase.getEditor().textProperty().addListener(observable -> {
			autoCompletePopup.filter(item -> item.toLowerCase().contains(cbBase.getEditor().getText().toLowerCase()));
			if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cbBase.showingProperty().get()
					|| cbBase.getEditor().getText().isEmpty())
				autoCompletePopup.hide();
			else
				autoCompletePopup.show(cbBase.getEditor());
		});

		cbBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbLinguagem.getItems().addAll(Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
				Language.PORTUGUESE_GOOGLE);

		cbLinguagem.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ESCAPE))
					cbLinguagem.getSelectionModel().clearSelection();
				else if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
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

		spnCapitulo.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtCaminhoSalvar.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		ckbApenasVinculos.selectedProperty().addListener((o, oldVal, newVal) -> {
			if (newVal)
				ckbCarregaVinculos.selectedProperty().set(true);
		});

		ckbCarregaVinculos.selectedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal)
				ckbApenasVinculos.selectedProperty().set(false);
		});

		spnVolume.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
		spnCapitulo.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 99999, 0, 1));

		linkaCelulas();
	}

	public static URL getFxmlLocate() {
		return MangasJsonController.class.getResource("/view/mangas/MangaJson.fxml");
	}

}
