package org.jisho.textosJapones.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VincularServices;
import org.jisho.textosJapones.parse.Parse;
import org.jisho.textosJapones.util.ListaExecucoes;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.components.NoSelectionModel;
import org.jisho.textosJapones.util.listener.VinculoListener;
import org.jisho.textosJapones.util.listener.VinculoServiceListener;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.notification.Notificacoes;

import com.jfoenix.controls.JFXAutoCompletePopup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Pair;

public class MangasVincularController implements Initializable, VinculoListener, VinculoServiceListener {

	final PseudoClass ON_DRAG_INICIADO = PseudoClass.getPseudoClass("drag-iniciado");
	final PseudoClass ON_DRAG_SELECIONADO = PseudoClass.getPseudoClass("drag-selecionado");
	final ListaExecucoes EXECUCOES = new ListaExecucoes();

	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXComboBox<String> cbBase;

	@FXML
	private JFXTextField txtMangaOriginal;

	@FXML
	private JFXTextField txtMangaVinculado;

	@FXML
	private JFXTextField txtArquivoOriginal;

	@FXML
	private JFXButton btnOriginal;

	@FXML
	private JFXTextField txtArquivoVinculado;

	@FXML
	private JFXButton btnVinculado;

	@FXML
	private JFXButton btnLimpar;

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
	private JFXButton btnCarregarLegendas;

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

	@FXML
	protected AnchorPane apDragScroolUp;

	@FXML
	protected AnchorPane apDragScroolDown;

	@FXML
	private ListView<String> lvCapitulosOriginal;

	@FXML
	private ListView<String> lvCapitulosVinculado;

	private JFXAutoCompletePopup<String> autoCompleteMangaOriginal;
	private JFXAutoCompletePopup<String> autoCompleteMangaVinculado;

	private File arquivoOriginal;
	private File arquivoVinculado;
	private Parse parseOriginal;
	private Parse parseVinculado;

	private VincularServices service = new VincularServices();
	private Vinculo vinculo = new Vinculo();
	private ObservableList<VinculoPagina> vinculado;
	private ObservableList<VinculoPagina> naoVinculado;

	private Map<String, Integer> capitulosOriginal = new HashMap<String, Integer>();
	private Map<String, Integer> capitulosVinculado = new HashMap<String, Integer>();

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnCarregarLegendas() {
		EXECUCOES.addExecucao(() -> {
			if (carregarLegendas()) {
				vincularLegenda();

				String texto = ""
						+ (vinculo.getVolumeOriginal() != null ? "Original: " + vinculo.getVolumeOriginal().getManga()
								+ " - V: " + vinculo.getVolumeOriginal().getVolume() + " - L: "
								+ vinculo.getVolumeOriginal().getLingua() + "|" : "")
						+ (vinculo.getVolumeVinculado() != null
								? "Vinculado: " + vinculo.getVolumeVinculado().getManga() + " - V:"
										+ vinculo.getVolumeVinculado().getVolume() + " - L: "
										+ vinculo.getVolumeOriginal().getLingua()
								: "");

				Notificacoes.notificacao(Notificacao.SUCESSO, "Legenda carregada com sucesso", texto);
			}
			return false;
		});
	}

	@FXML
	private void onBtnOriginal() {

		String pasta = arquivoOriginal != null ? arquivoOriginal.getPath()
				: (arquivoVinculado != null ? arquivoVinculado.getPath() : null);
		arquivoOriginal = selecionaArquivo("Selecione o arquivo de origem", pasta);

		if (!selecionarArquivo())
			carregarArquivo(arquivoOriginal, true);
		else
			carregaDados(arquivoOriginal, true);

	}

	@FXML
	private void onBtnVinculado() {

		if (arquivoOriginal == null) {
			AlertasPopup.AvisoModal("Selecione o arquivo original", "Necessário informar o arquivo original primeiro.");
			return;
		}

		String pasta = arquivoVinculado != null ? arquivoVinculado.getPath()
				: (arquivoOriginal != null ? arquivoOriginal.getPath() : null);
		arquivoVinculado = selecionaArquivo("Selecione o arquivo vinculado", pasta);
		if (!selecionarArquivo())
			carregarArquivo(arquivoVinculado, false);
		else
			carregaDados(arquivoOriginal, false);

	}

	@FXML
	private void onBtnLimpar() {
		EXECUCOES.addExecucao(() -> {
			limpar();

			return false;
		});

	}

	@FXML
	private void onBtnDeletar() {
		EXECUCOES.addExecucao(() -> {
			if (cbBase.getSelectionModel().getSelectedItem() == null || vinculo == null)
				return false;

			try {
				service.delete(cbBase.getSelectionModel().getSelectedItem(), vinculo);
				Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Arquivo deletado com sucesso.");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				AlertasPopup.ErroModal("Erro ao deletar", e.getMessage());
			}

			return false;
		});
	}

	@FXML
	private void onBtnSalvar() {
		EXECUCOES.addExecucao(() -> {
			if (valida()) {
				salvar();
				Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Salvo com sucesso.");
			}

			return false;
		});
	}

	@FXML
	private void onBtnRecarregar() {
		EXECUCOES.addExecucao(() -> {
			lvPaginasVinculadas.refresh();
			lvPaginasNaoVinculadas.refresh();
			lvPaginasVinculadas.requestLayout();
			lvPaginasNaoVinculadas.requestLayout();

			Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Recarregar.");

			return false;
		});
	}

	@FXML
	private void onBtnOrderAutomatico() {
		service.autoReordenarPaginaDupla(false);
		lvPaginasVinculadas.refresh();
		lvPaginasNaoVinculadas.refresh();
		lvPaginasVinculadas.requestLayout();
		lvPaginasNaoVinculadas.requestLayout();

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação automatica.");
	}

	@FXML
	private void onBtnOrderPaginaDupla() {
		service.ordenarPaginaDupla(ckbPaginaDuplaCalculada.isSelected());
		lvPaginasVinculadas.refresh();
		lvPaginasNaoVinculadas.refresh();
		lvPaginasVinculadas.requestLayout();
		lvPaginasNaoVinculadas.requestLayout();

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação página dupla.");
	}

	@FXML
	private void onBtnOrderPaginaUnica() {
		service.ordenarPaginaSimples();
		lvPaginasVinculadas.refresh();
		lvPaginasNaoVinculadas.refresh();
		lvPaginasVinculadas.requestLayout();
		lvPaginasNaoVinculadas.requestLayout();

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação página simples.");
	}

	@FXML
	private void onBtnOrderSequencia() {
		service.reordenarPeloNumeroPagina();
		lvPaginasVinculadas.refresh();
		lvPaginasNaoVinculadas.refresh();
		lvPaginasVinculadas.requestLayout();
		lvPaginasNaoVinculadas.requestLayout();

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação pela sequencia.");
	}

	private Long lastTime = System.currentTimeMillis();
	private final static Integer FAST = 200;
	private final static Integer SLOW = 500;

	@FXML
	private void onDragScroolUp(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > SLOW) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(lvPaginasVinculadas);
			if (index != null && index > 0)
				lvPaginasVinculadas.scrollTo(index - 1);
		}
	}

	@FXML
	private void onDragScroolUpFast(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > FAST) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(lvPaginasVinculadas);
			if (index != null && index > 0)
				lvPaginasVinculadas.scrollTo(index - 1);
		}
	}

	@FXML
	private void onDragScroolDown(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > SLOW) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(lvPaginasVinculadas);
			if (index != null && (index - 1) < lvPaginasVinculadas.getItems().size())
				lvPaginasVinculadas.scrollTo(index + 1);
		}
	}

	@FXML
	private void onDragScroolDownFast(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > FAST) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(lvPaginasVinculadas);
			if (index != null && (index - 1) < lvPaginasVinculadas.getItems().size())
				lvPaginasVinculadas.scrollTo(index + 1);
		}
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	@Override
	public Boolean onDuploClique(Node root, VinculoPagina vinculo, Pagina origem) {
		return true;
	}

	@Override
	public void onDrop(Pagina origem, VinculoPagina vinculoOrigem, Pagina destino, VinculoPagina vinculoDestino) {
		VinculoPagina itemOrigem = getVinculoOriginal(origem, vinculoOrigem);
		VinculoPagina itemDestino = getVinculoOriginal(destino, vinculoDestino);

		if (origem == Pagina.NAO_VINCULADO)
			service.fromNaoVinculado(itemOrigem, itemDestino, destino);
		else if (origem == Pagina.VINCULADO_DIREITA || destino == Pagina.VINCULADO_DIREITA)
			service.onMovimentaDireita(origem, itemOrigem, destino, itemDestino);
		else if (origem == Pagina.VINCULADO_ESQUERDA)
			service.onMovimentaEsquerda(itemOrigem, itemDestino);

		lvPaginasVinculadas.refresh();
		lvPaginasNaoVinculadas.refresh();
		lvPaginasVinculadas.requestLayout();
		lvPaginasNaoVinculadas.requestLayout();
	}

	@Override
	public void onDragStart() {
		apDragScroolUp.setVisible(true);
		apDragScroolDown.setVisible(true);
		lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, true);

	}

	@Override
	public void onDragEnd() {
		apDragScroolUp.setVisible(false);
		apDragScroolDown.setVisible(false);
		lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);
		lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, false);

	}

	@Override
	public ObservableList<VinculoPagina> getVinculados() {
		return vinculado;
	}

	@Override
	public ObservableList<VinculoPagina> getNaoVinculados() {
		return naoVinculado;
	}

	private void desabilita() {
		btnOrderPaginaUnica.setDisable(true);
		btnOrderPaginaDupla.setDisable(true);
		btnOrderSequencia.setDisable(true);
		btnOrderAutomatico.setDisable(true);
	}

	private void habilita() {
		btnOrderPaginaUnica.setDisable(false);
		btnOrderPaginaDupla.setDisable(false);
		btnOrderSequencia.setDisable(false);
		btnOrderAutomatico.setDisable(false);
	}

	private void limpar() {
		cbLinguagemVinculado.getSelectionModel().select(Language.PORTUGUESE);
		cbLinguagemOrigem.getSelectionModel().select(Language.JAPANESE);
		txtMangaOriginal.setText("");
		txtMangaVinculado.setText("");
		txtArquivoOriginal.setText("");
		txtArquivoVinculado.setText("");
		ckbPaginaDuplaCalculada.selectedProperty().set(true);
		spnVolume.getValueFactory().setValue(1);

		vinculado = FXCollections.observableArrayList(new ArrayList<VinculoPagina>());
		naoVinculado = FXCollections.observableArrayList(new ArrayList<VinculoPagina>());

		lvPaginasVinculadas.setItems(vinculado);
		lvPaginasNaoVinculadas.setItems(naoVinculado);
		lvPaginasVinculadas.refresh();
		lvPaginasNaoVinculadas.refresh();
		lvPaginasVinculadas.requestLayout();
		lvPaginasNaoVinculadas.requestLayout();

		Util.destroiParse(parseOriginal);
		Util.destroiParse(parseVinculado);

		arquivoOriginal = null;
		arquivoVinculado = null;
		parseOriginal = null;
		parseVinculado = null;
	}

	private Boolean carregarLegendas() {
		if (cbBase.getSelectionModel().getSelectedItem() == null
				|| cbBase.getSelectionModel().getSelectedItem().isBlank() || txtMangaOriginal.getText().isEmpty()
				|| txtMangaVinculado.getText().isEmpty()) {
			AlertasPopup.AvisoModal("Aviso", "Necessário selecionar a base e o manga.");
			return false;
		}

		MangaVolume volumeOriginal = service.selectVolume(cbBase.getSelectionModel().getSelectedItem(),
				txtMangaOriginal.getText(), spnVolume.getValue(),
				cbLinguagemOrigem.getSelectionModel().getSelectedItem());

		MangaVolume volumeVinculado = service.selectVolume(cbBase.getSelectionModel().getSelectedItem(),
				txtMangaVinculado.getText(), spnVolume.getValue(),
				cbLinguagemVinculado.getSelectionModel().getSelectedItem());

		vinculo.setVolumeOriginal(volumeOriginal);
		vinculo.setVolumeVinculado(volumeVinculado);

		if (volumeOriginal == null && volumeVinculado == null)
			AlertasPopup.AvisoModal("Aviso", "Não encontrado nenhum item com as informações repassadas.");
		else if (volumeOriginal == null)
			AlertasPopup.AvisoModal("Aviso", "Manga original não encontrado.");
		else if (volumeVinculado == null)
			AlertasPopup.AvisoModal("Aviso", "Manga vinculado não encontrado.");

		return volumeOriginal != null && volumeVinculado != null;
	}

	private void vincularLegenda() {
		if (vinculo == null || vinculado.isEmpty())
			return;

		EXECUCOES.addExecucao(() -> {
			desabilita();
			GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
			progress.getTitulo().setText("Vinculando legendas");
			if (TaskbarProgressbar.isSupported())
				TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

			Task<Void> vincular = new Task<Void>() {
				Integer I, Max;
				String error = "";

				@Override
				protected Void call() throws Exception {
					try {
						I = 0;
						Max = 1;
						error = "";

						updateMessage("Vinculando mangas....");

						if (vinculo.getVolumeOriginal() != null && vinculo.getVolumeVinculado() != null)
							Max = vinculado.size() * 2 + naoVinculado.size();
						else if (vinculo.getVolumeOriginal() != null)
							Max = vinculado.size();
						else if (vinculo.getVolumeOriginal() != null)
							Max = vinculado.size() + naoVinculado.size();

						if (vinculo.getVolumeOriginal() != null) {
							updateMessage("Vinculando mangas original....");

							final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
							final List<MangaPagina> paginasOriginal = new ArrayList<MangaPagina>();
							vinculo.getVolumeOriginal().getCapitulos().stream()
									.forEach(it -> paginasOriginal.addAll(it.getPaginas()));

							vinculado.parallelStream().forEach(vi -> {
								vi.setMangaPaginaOriginal(service.findPagina(paginasOriginal, encontrados,
										vi.getOriginalPathPagina(), vi.getOriginalHash(), vi.getOriginalNomePagina(),
										vi.getOriginalPagina()));

								I++;
								updateProgress(I, Max);
								Platform.runLater(() -> {
									if (TaskbarProgressbar.isSupported())
										TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
												Type.NORMAL);
								});
							});
						}

						if (vinculo.getVolumeVinculado() != null) {
							updateMessage("Vinculando mangas vinculado....");

							final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
							final List<MangaPagina> paginasOriginal = new ArrayList<MangaPagina>();
							vinculo.getVolumeVinculado().getCapitulos().stream()
									.forEach(it -> paginasOriginal.addAll(it.getPaginas()));

							vinculado.parallelStream().forEach(vi -> {
								if (vi.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
									vi.setMangaPaginaEsquerda(service.findPagina(paginasOriginal, encontrados,
											vi.getVinculadoEsquerdaPathPagina(), vi.getVinculadoEsquerdaHash(),
											vi.getVinculadoEsquerdaNomePagina(), vi.getVinculadoEsquerdaPagina()));

								if (vi.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
									vi.setMangaPaginaDireita(service.findPagina(paginasOriginal, encontrados,
											vi.getVinculadoDireitaPathPagina(), vi.getVinculadoDireitaHash(),
											vi.getVinculadoDireitaNomePagina(), vi.getVinculadoDireitaPagina()));

								I++;
								updateProgress(I, Max);
								Platform.runLater(() -> {
									if (TaskbarProgressbar.isSupported())
										TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
												Type.NORMAL);
								});
							});

							naoVinculado.parallelStream().forEach(vi -> {
								if (vi.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
									vi.setMangaPaginaEsquerda(service.findPagina(paginasOriginal, encontrados,
											vi.getVinculadoEsquerdaPathPagina(), vi.getVinculadoEsquerdaHash(),
											vi.getVinculadoEsquerdaNomePagina(), vi.getVinculadoEsquerdaPagina()));

								I++;
								updateProgress(I, Max);
								Platform.runLater(() -> {
									if (TaskbarProgressbar.isSupported())
										TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
												Type.NORMAL);
								});
							});
						}

						I++;
						updateProgress(I, Max);
						Platform.runLater(() -> {
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max, Type.NORMAL);
						});

					} catch (Exception e) {
						e.printStackTrace();
						error = e.getMessage();
					}

					return null;
				}

				@Override
				protected void succeeded() {
					Platform.runLater(() -> {
						progress.getBarraProgresso().progressProperty().unbind();
						progress.getLog().textProperty().unbind();
						MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

						if (!error.isEmpty())
							AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
									error);

						habilita();

						EXECUCOES.endProcess();
					});

				}

				@Override
				protected void failed() {
					Platform.runLater(() -> {
						progress.getBarraProgresso().progressProperty().unbind();
						progress.getLog().textProperty().unbind();

						MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

						MenuPrincipalController.getController().getLblLog().setText("");
						habilita();

						EXECUCOES.endProcess();
					});

				}

			};

			progress.getLog().textProperty().bind(vincular.messageProperty());
			progress.getBarraProgresso().progressProperty().bind(vincular.progressProperty());
			Thread t = new Thread(vincular);
			t.start();

			return true;
		});
	}

	private Boolean selecionarArquivo() {
		if (cbLinguagemOrigem.getSelectionModel().getSelectedItem() == null && arquivoOriginal == null
				&& cbLinguagemVinculado.getSelectionModel().getSelectedItem() == null && arquivoVinculado == null)
			return false;

		try {
			String arquivoOriginal = this.arquivoOriginal != null ? this.arquivoOriginal.getName() : "";
			String arquivoVinculado = this.arquivoVinculado != null ? this.arquivoVinculado.getName() : "";

			Vinculo vinculo = service.select(cbBase.getSelectionModel().getSelectedItem(), txtMangaOriginal.getText(),
					spnVolume.getValue(), cbLinguagemOrigem.getSelectionModel().getSelectedItem(), arquivoOriginal,
					cbLinguagemVinculado.getSelectionModel().getSelectedItem(), arquivoVinculado);

			if (vinculo != null) {
				this.vinculo = vinculo;

				txtArquivoOriginal.setText(vinculo.getNomeArquivoOriginal());
				txtArquivoVinculado.setText(vinculo.getNomeArquivoVinculado());

				if (cbLinguagemOrigem.getSelectionModel().getSelectedItem() == null)
					cbLinguagemOrigem.getSelectionModel().select(vinculo.getLinguagemOriginal());
				else if (cbLinguagemOrigem.getSelectionModel().getSelectedItem()
						.compareTo(vinculo.getLinguagemVinculado()) != 0) {
					if (AlertasPopup.ConfirmacaoModal("Aviso",
							"A linguagem selecionada e o manga original são diferentes.\nDeseja recarregar?")) {
						cbLinguagemOrigem.getSelectionModel().select(vinculo.getLinguagemOriginal());
						return selecionarArquivo();
					}
				}

				if (cbLinguagemVinculado.getSelectionModel().getSelectedItem() == null)
					cbLinguagemVinculado.getSelectionModel().select(vinculo.getLinguagemVinculado());
				else if (cbLinguagemVinculado.getSelectionModel().getSelectedItem()
						.compareTo(vinculo.getLinguagemVinculado()) != 0) {
					if (AlertasPopup.ConfirmacaoModal("Aviso",
							"A linguagem selecionada e o manga vinculado são diferentes.\nDeseja recarregar?")) {
						cbLinguagemVinculado.getSelectionModel().select(vinculo.getLinguagemVinculado());
						return selecionarArquivo();
					}
				}

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
			md5 = Util.MD5(parse.getPagina(pagina));
			imput = parse.getPagina(pagina);
			image = new Image(imput);
			dupla = (image.getWidth() / image.getHeight()) > 0.9;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new Pair<Image, Pair<Boolean, String>>(image, new Pair<Boolean, String>(dupla, md5));
	}

	private void carregarArquivo(File arquivo, Boolean isManga) {
		if (arquivo == null)
			return;

		Parse parse = Util.criaParse(arquivo);

		if (parse != null) {

			EXECUCOES.addExecucao(() -> {

				desabilita();
				GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
				progress.getTitulo().setText("Vinculando legendas");
				if (TaskbarProgressbar.isSupported())
					TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

				Task<Void> carregar = new Task<Void>() {
					String error = "";
					Integer X = 0;

					@Override
					protected Void call() throws Exception {
						try {

							error = "";
							updateMessage("Carregando manga....");

							naoVinculado.clear();

							if (isManga) {
								txtArquivoOriginal.setText(arquivo.getName());
								Util.destroiParse(parseOriginal);
								Util.destroiParse(parseVinculado);
								parseOriginal = parse;
								parseVinculado = null;

								Util.getCapitulos(parseOriginal, capitulosOriginal, lvCapitulosOriginal);
								Util.getCapitulos(parseVinculado, capitulosVinculado, lvCapitulosVinculado);

								txtArquivoVinculado.setText("");
								MangaVolume volume = service.selectVolume(cbBase.getSelectionModel().getSelectedItem(),
										txtMangaOriginal.getText(), spnVolume.getValue(),
										cbLinguagemOrigem.getSelectionModel().getSelectedItem());
								vinculo.setVolumeOriginal(volume);

								updateMessage("Carregando manga original....");

								final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
								final List<MangaPagina> paginas = new ArrayList<MangaPagina>();

								if (volume != null)
									volume.getCapitulos().stream().forEach(it -> paginas.addAll(it.getPaginas()));

								ArrayList<VinculoPagina> list = new ArrayList<VinculoPagina>();
								for (int x = 0; x < parseOriginal.getSize(); x++) {
									Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseOriginal, x);
									Pair<Boolean, String> detalhe = image.getValue();
									String path = parseOriginal.getPaginaPasta(x);

									list.add(new VinculoPagina(Util.getNome(path), Util.getPasta(path), x,
											parse.getSize(), detalhe.getKey(), service.findPagina(paginas, encontrados,
													path, Util.getPasta(path), detalhe.getValue(), x),
											image.getKey(), detalhe.getValue()));

									X = x;
									updateProgress(x, parseOriginal.getSize());
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X,
													parseOriginal.getSize(), Type.NORMAL);
									});
								}

								vinculado = FXCollections.observableArrayList(list);
								lvPaginasVinculadas.setItems(vinculado);
							} else {
								txtArquivoVinculado.setText(arquivo.getName());
								Util.destroiParse(parseVinculado);
								parseVinculado = parse;

								Util.getCapitulos(parseVinculado, capitulosVinculado, lvCapitulosVinculado);

								updateMessage("Carregando manga vinculado....");

								MangaVolume volume = vinculo.getVolumeVinculado();
								final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
								final List<MangaPagina> paginas = new ArrayList<MangaPagina>();

								if (volume != null)
									volume.getCapitulos().stream().forEach(it -> paginas.addAll(it.getPaginas()));

								for (int x = 0; x < parseVinculado.getSize(); x++) {
									Pair<Image, Pair<Boolean, String>> image = carregaImagem(parseVinculado, x);
									Pair<Boolean, String> detalhe = image.getValue();
									String path = parseVinculado.getPaginaPasta(x);

									if (x < vinculado.size()) {
										VinculoPagina item = vinculado.get(x);

										item.limparVinculado();
										item.setVinculadoEsquerdaPagina(x);
										item.setVinculadoEsquerdaNomePagina(Util.getNome(path));
										item.setVinculadoEsquerdaPathPagina(Util.getPasta(path));
										item.setVinculadoEsquerdaPaginas(parseVinculado.getSize());
										item.isVinculadoEsquerdaPaginaDupla = detalhe.getKey();
										item.setImagemVinculadoEsquerda(image.getKey());
										item.setMangaPaginaEsquerda(service.findPagina(paginas, encontrados, path,
												Util.getPasta(path), detalhe.getValue(), x));
										item.setVinculadoEsquerdaHash(detalhe.getValue());
									} else
										naoVinculado.add(new VinculoPagina(Util.getNome(path), Util.getPasta(path), x,
												parseVinculado.getSize(), detalhe.getKey(),
												service.findPagina(paginas, encontrados, path, Util.getPasta(path),
														detalhe.getValue(), x),
												image.getKey(), true, detalhe.getValue()));

									X = x;
									updateProgress(x, parseOriginal.getSize());
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X,
													parseOriginal.getSize(), Type.NORMAL);
									});
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
							error = e.getMessage();
						}
						return null;
					}

					@Override
					protected void succeeded() {
						Platform.runLater(() -> {
							progress.getBarraProgresso().progressProperty().unbind();
							progress.getLog().textProperty().unbind();

							MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
							TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

							if (!error.isEmpty())
								AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
										error);

							MenuPrincipalController.getController().getLblLog().setText("");
							habilita();
							lvPaginasVinculadas.refresh();

							EXECUCOES.endProcess();
						});

					}

					@Override
					protected void failed() {
						Platform.runLater(() -> {
							progress.getBarraProgresso().progressProperty().unbind();
							progress.getLog().textProperty().unbind();

							MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
							TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

							MenuPrincipalController.getController().getLblLog().setText("");
							habilita();
							lvPaginasVinculadas.refresh();

							EXECUCOES.endProcess();
						});

					}

				};

				progress.getLog().textProperty().bind(carregar.messageProperty());
				progress.getBarraProgresso().progressProperty().bind(carregar.progressProperty());
				Thread t = new Thread(carregar);
				t.start();

				return true;
			});
		}
	}

	private void carregaDados(File arquivo, Boolean isManga) {
		EXECUCOES.addExecucao(() -> {
			if (isManga) {
				Util.destroiParse(parseOriginal);
				parseOriginal = Util.criaParse(arquivo);

				Util.getCapitulos(parseOriginal, capitulosOriginal, lvCapitulosOriginal);
			} else {
				Util.destroiParse(parseVinculado);
				parseVinculado = Util.criaParse(arquivo);

				Util.getCapitulos(parseVinculado, capitulosVinculado, lvCapitulosVinculado);

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

			return false;
		});
	}

	private Boolean valida() {
		if (cbBase.getSelectionModel().getSelectedItem() == null) {
			cbBase.setUnFocusColor(Color.RED);
			AlertasPopup.AvisoModal("Alerta", "Necessário inforar uma base.");
			return false;
		}

		if (txtMangaOriginal.getText().isEmpty()) {
			txtMangaOriginal.setUnFocusColor(Color.RED);
			AlertasPopup.AvisoModal("Alerta", "Necessário inforar um manga principal.");
			return false;
		}

		if (txtMangaVinculado.getText().isEmpty()) {
			txtMangaVinculado.setUnFocusColor(Color.RED);
			AlertasPopup.AvisoModal("Alerta", "Necessário inforar um manga vinculado.");
			return false;
		}

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

	private File selecionaArquivo(String titulo, String pasta) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(titulo);

		if (pasta != null && !pasta.isEmpty())
			fileChooser.setInitialDirectory(new File(Util.getCaminho(pasta)));

		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ALL FILES", "*.*"),
				new FileChooser.ExtensionFilter("ZIP", "*.zip"), new FileChooser.ExtensionFilter("CBZ", "*.cbz"),
				new FileChooser.ExtensionFilter("RAR", "*.rar"), new FileChooser.ExtensionFilter("CBR", "*.cbr"));

		return fileChooser.showOpenDialog(null);
	}

	private void selecionaBase(String base) {
		autoCompleteMangaOriginal.getSuggestions().clear();
		autoCompleteMangaVinculado.getSuggestions().clear();

		if (base == null || base.isEmpty())
			return;

		try {
			service.createTabelas(base);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			System.out.println("Erro ao consultar as sugestões de mangas.");
		}

		selectionaManga(autoCompleteMangaOriginal, cbLinguagemOrigem, txtMangaOriginal);
		selectionaManga(autoCompleteMangaVinculado, cbLinguagemVinculado, txtMangaVinculado);

	}

	private void selectionaManga(JFXAutoCompletePopup<String> autoComplete, JFXComboBox<Language> linguagem,
			JFXTextField manga) {
		try {
			autoComplete.getSuggestions().clear();
			List<String> mangas = service.getMangas(cbBase.getSelectionModel().getSelectedItem(),
					linguagem.getSelectionModel().getSelectedItem());
			autoComplete.getSuggestions().addAll(mangas);
			manga.setText("");
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			System.out.println("Erro ao consultar as sugestões de mangas.");
		}
	}

	private void onClose() {
		Util.destroiParse(parseOriginal);
		Util.destroiParse(parseVinculado);
	}

	private void selecionaCapitulo(String capitulo, Boolean isManga) {
		if (capitulo == null || capitulo.isEmpty())
			return;

		if (isManga && capitulosOriginal.isEmpty() || !isManga && capitulosVinculado.isEmpty())
			return;

		if (isManga) {
			Integer numero = capitulosOriginal.get(capitulo);
			Optional<VinculoPagina> pagina = lvPaginasVinculadas.getItems().stream()
					.filter(pg -> pg.getOriginalPagina().compareTo(numero) == 0).findFirst();
			if (pagina.isPresent())
				lvPaginasVinculadas.scrollTo(pagina.get());
		} else {
			Integer numero = capitulosVinculado.get(capitulo);
			Optional<VinculoPagina> pagina = lvPaginasVinculadas.getItems().stream()
					.filter(pg -> pg.getVinculadoEsquerdaPagina().compareTo(numero) == 0
							|| pg.getVinculadoDireitaPagina().compareTo(numero) == 0)
					.findFirst();
			if (pagina.isPresent())
				lvPaginasVinculadas.scrollTo(pagina.get());
			else {
				pagina = lvPaginasNaoVinculadas.getItems().stream()
						.filter(pg -> pg.getVinculadoEsquerdaPagina().compareTo(numero) == 0).findFirst();
				if (pagina.isPresent())
					lvPaginasNaoVinculadas.scrollTo(pagina.get());
			}
		}
	}

	// Necessário pois a imagem não é serializada
	private VinculoPagina getVinculoOriginal(Pagina origem, VinculoPagina copia) {
		VinculoPagina original = null;
		switch (origem) {
		case VINCULADO_DIREITA, VINCULADO_ESQUERDA:
			original = vinculado.get(vinculado.indexOf(copia));
			break;
		case NAO_VINCULADO:
			original = naoVinculado.get(naoVinculado.indexOf(copia));
			break;
		default:

		}

		return original;
	}

	private void preparaOnDrop() {
		lvPaginasNaoVinculadas.setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.getGestureSource() != lvPaginasNaoVinculadas
						&& event.getDragboard().hasContent(Util.VINCULO_ITEM_FORMAT))
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

				event.consume();
			}
		});

		lvPaginasNaoVinculadas.setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_SELECIONADO, true);
				lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, false);

				event.consume();
			}
		});

		lvPaginasNaoVinculadas.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);
				lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, true);

				event.consume();
			}
		});

		lvPaginasNaoVinculadas.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasContent(Util.VINCULO_ITEM_FORMAT)) {
					VinculoPagina vinculo = (VinculoPagina) db.getContent(Util.VINCULO_ITEM_FORMAT);

					service.addNaoVInculado(getVinculoOriginal(vinculo.onDragOrigem, vinculo), vinculo.onDragOrigem);

					lvPaginasVinculadas.refresh();
					lvPaginasNaoVinculadas.refresh();
					lvPaginasVinculadas.requestLayout();
					lvPaginasNaoVinculadas.requestLayout();

					success = true;
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});

		lvPaginasNaoVinculadas.setOnDragDone(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				onDragEnd();
			}
		});

	}

	private void preparaCelulas() {
		lvPaginasVinculadas.setSelectionModel(new NoSelectionModel<VinculoPagina>());
		lvPaginasNaoVinculadas.setSelectionModel(new NoSelectionModel<VinculoPagina>());

		lvPaginasVinculadas.setCellFactory(new Callback<ListView<VinculoPagina>, ListCell<VinculoPagina>>() {
			@Override
			public ListCell<VinculoPagina> call(ListView<VinculoPagina> studentListView) {
				ListCell<VinculoPagina> cell = new ListCell<VinculoPagina>() {
					@Override
					public void updateItem(VinculoPagina item, boolean empty) {
						super.updateItem(item, empty);

						setText(null);

						if (empty || item == null)
							setGraphic(null);
						else {
							FXMLLoader mLLoader = new FXMLLoader(MangasVincularCelulaController.getFxmlLocate());

							try {
								mLLoader.load();
							} catch (IOException e) {
								e.printStackTrace();
							}

							MangasVincularCelulaController controller = mLLoader.getController();
							controller.setDados(item);
							controller.setListener(MangasVincularController.this);

							setGraphic(controller.hbRoot);
						}
					}
				};

				return cell;
			}
		});

		lvPaginasNaoVinculadas.setCellFactory(new Callback<ListView<VinculoPagina>, ListCell<VinculoPagina>>() {
			@Override
			public ListCell<VinculoPagina> call(ListView<VinculoPagina> studentListView) {
				ListCell<VinculoPagina> cell = new ListCell<VinculoPagina>() {
					@Override
					public void updateItem(VinculoPagina item, boolean empty) {
						super.updateItem(item, empty);

						setText(null);
						if (empty || item == null)
							setGraphic(null);
						else {
							FXMLLoader mLLoader = new FXMLLoader(MangasVincularSimplesController.getFxmlLocate());

							try {
								mLLoader.load();
							} catch (IOException e) {
								e.printStackTrace();
							}

							MangasVincularSimplesController controller = mLLoader.getController();
							controller.setDados(item);
							controller.setListener(MangasVincularController.this);

							setGraphic(controller.hbRoot);
						}
					}
				};

				return cell;
			}
		});

		lvCapitulosOriginal.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() > 1)
					selecionaCapitulo(lvCapitulosOriginal.getSelectionModel().getSelectedItem(), true);
			}
		});

		lvCapitulosVinculado.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() > 1)
					selecionaCapitulo(lvCapitulosVinculado.getSelectionModel().getSelectedItem(), false);
			}
		});
	}

	private Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		Run.getPrimaryStage().setOnCloseRequest(e -> onClose());
		service.setListener(this);

		try {
			cbBase.getItems().setAll(service.getTabelas());
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro ao carregar as tabelas", e.getMessage());
		}

		cbBase.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (!newValue.isEmpty()) {
				if (cbBase.getItems().isEmpty())
					cbBase.setUnFocusColor(Color.RED);
				else {
					cbBase.setUnFocusColor(Color.web("#106ebe"));
					if (cbBase.getItems().contains(newValue))
						selecionaBase(newValue);
				}
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

		txtMangaOriginal.focusTraversableProperty().addListener((options, oldValue, newValue) -> {
			if (oldValue)
				txtMangaOriginal.setUnFocusColor(Color.web("#106ebe"));
		});

		txtMangaOriginal.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		autoCompleteMangaOriginal = new JFXAutoCompletePopup<String>();

		autoCompleteMangaOriginal.setSelectionHandler(event -> {
			txtMangaOriginal.setText(event.getObject());
		});

		txtMangaOriginal.textProperty().addListener(observable -> {
			if (cbBase.getItems().isEmpty())
				cbBase.setUnFocusColor(Color.RED);

			autoCompleteMangaOriginal
					.filter(string -> string.toLowerCase().contains(txtMangaOriginal.getText().toLowerCase()));
			if (autoCompleteMangaOriginal.getFilteredSuggestions().isEmpty() || txtMangaOriginal.getText().isEmpty())
				autoCompleteMangaOriginal.hide();
			else
				autoCompleteMangaOriginal.show(txtMangaOriginal);
		});

		txtMangaVinculado.focusTraversableProperty().addListener((options, oldValue, newValue) -> {
			if (oldValue)
				txtMangaVinculado.setUnFocusColor(Color.web("#106ebe"));
		});

		txtMangaVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		autoCompleteMangaVinculado = new JFXAutoCompletePopup<String>();

		autoCompleteMangaVinculado.setSelectionHandler(event -> {
			txtMangaVinculado.setText(event.getObject());
		});

		txtMangaVinculado.textProperty().addListener(observable -> {
			if (cbBase.getItems().isEmpty())
				cbBase.setUnFocusColor(Color.RED);

			autoCompleteMangaVinculado
					.filter(string -> string.toLowerCase().contains(txtMangaVinculado.getText().toLowerCase()));
			if (autoCompleteMangaVinculado.getFilteredSuggestions().isEmpty() || txtMangaVinculado.getText().isEmpty())
				autoCompleteMangaVinculado.hide();
			else
				autoCompleteMangaVinculado.show(txtMangaVinculado);
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

		cbLinguagemVinculado.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			selectionaManga(autoCompleteMangaOriginal, cbLinguagemOrigem, txtMangaOriginal);
		});

		cbLinguagemVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbLinguagemVinculado.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			selectionaManga(autoCompleteMangaVinculado, cbLinguagemVinculado, txtMangaVinculado);
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

		preparaCelulas();
		preparaOnDrop();

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
