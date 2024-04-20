package org.jisho.textosJapones.controller.mangas;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Pair;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.ListViewNoSelectionModel;
import org.jisho.textosJapones.components.TableViewNoSelectionModel;
import org.jisho.textosJapones.components.animation.Animacao;
import org.jisho.textosJapones.components.listener.VinculoListener;
import org.jisho.textosJapones.components.listener.VinculoServiceListener;
import org.jisho.textosJapones.components.listener.VinculoTextoListener;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.components.notification.Notificacoes;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.fileparse.Parse;
import org.jisho.textosJapones.model.entities.Atributos;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.model.enums.Tabela;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VincularServices;
import org.jisho.textosJapones.util.ListaExecucoes;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class MangasVincularController implements Initializable, VinculoListener, VinculoServiceListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MangasVincularController.class);

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
	private JFXButton btnVisualizarLegendas;

	@FXML
	private JFXButton btnOrderAutomatico;

	@FXML
	private JFXButton btnOrderPaginaDupla;

	@FXML
	private JFXButton btnOrderPaginaUnica;

	@FXML
	private JFXButton btnOrderSequencia;

	@FXML
	private JFXButton btnOrderPHash;

	@FXML
	private JFXButton btnOrderHistogram;

	@FXML
	private JFXButton btnOrderInteligente;

	@FXML
	private JFXSlider sldPrecisao;

	@FXML
	private JFXCheckBox ckbPaginaDuplaCalculada;

	@FXML
	private TableView<VinculoPagina> tvPaginasVinculadas;

	@FXML
	private TableColumn<VinculoPagina, Integer> tcMangaOriginal;

	@FXML
	private TableColumn<VinculoPagina, Integer> tcMangaVinculado;

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
	private boolean automatico = false;

	private File arquivoOriginal;
	private File arquivoVinculado;
	private Parse parseOriginal;
	private Parse parseVinculado;

	private final VincularServices service = new VincularServices();
	private Vinculo vinculo = new Vinculo();
	private ObservableList<VinculoPagina> vinculado;
	private ObservableList<VinculoPagina> naoVinculado;

	private final Map<String, Integer> capitulosOriginal = new HashMap<String, Integer>();
	private final Map<String, Integer> capitulosVinculado = new HashMap<String, Integer>();

	public VinculoTextoListener refreshListener;

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	public Vinculo getVinculo() {
		return vinculo;
	}

	public JFXTextField getTxtMangaOriginal() {
		return txtMangaOriginal;
	}

	public JFXTextField getTxtMangaVinculado() {
		return txtMangaVinculado;
	}

	public JFXButton getBtnSalvar() {
		return btnSalvar;
	}

	public JFXComboBox<Language> getCbLinguagemOrigem() {
		return cbLinguagemOrigem;
	}

	public JFXComboBox<Language> getCbLinguagemVinculado() {
		return cbLinguagemVinculado;
	}

	public JFXButton getBtnCarregarLegendas() {
		return btnCarregarLegendas;
	}

	public ObservableList<String> getListCapitulosOriginal() {
		return lvCapitulosOriginal.getItems();
	}

	public ObservableList<String> getListCapitulosVinculado() {
		return lvCapitulosVinculado.getItems();
	}

	public Map<String, Integer> getCapitulosOriginal() {
		return capitulosOriginal;
	}

	public Map<String, Integer> getCapitulosVinculado() {
		return capitulosVinculado;
	}

	public JFXAutoCompletePopup<String> getAutoCompleteMangaOriginal() {
		return autoCompleteMangaOriginal;
	}

	public JFXAutoCompletePopup<String> getAutoCompleteMangaVinculado() {
		return autoCompleteMangaVinculado;
	}

	@FXML
	private void onBtnCarregarLegendas() {
		EXECUCOES.addExecucao(abort -> {
			if (carregar()) {
				vincularLegenda(true);

				String texto = ""
						+ (vinculo.getVolumeOriginal() != null ? "Original: " + vinculo.getVolumeOriginal().getManga()
								+ " - V: " + vinculo.getVolumeOriginal().getVolume() + " - L: "
								+ vinculo.getVolumeOriginal().getLingua() + "|" : "")
						+ (vinculo.getVolumeVinculado() != null
								? "Vinculado: " + vinculo.getVolumeVinculado().getManga() + " - V:"
										+ vinculo.getVolumeVinculado().getVolume() + " - L: "
										+ vinculo.getVolumeOriginal().getLingua()
								: "");

				Notificacoes.notificacao(Notificacao.SUCESSO, "Manga vinculado carregada com sucesso", texto);
			} else if (carregarLegendas()) {
				vincularLegenda(false);

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
	private void onBtnVisualizarLegendas() {
		visualizarLegendas(null);
	}

	@FXML
	private void onBtnOriginal() {

		String pasta = arquivoOriginal != null ? arquivoOriginal.getPath()
				: (arquivoVinculado != null ? arquivoVinculado.getPath() : null);
		File arquivo = selecionaArquivo("Selecione o arquivo de origem", pasta);

		if (arquivo != null) {
			arquivoOriginal = arquivo;
			if (!selecionarArquivo())
				carregarArquivo(arquivo, true);
			else
				carregaDados(arquivo, true);
		}
	}

	@FXML
	private void onBtnVinculado() {

		if (arquivoOriginal == null) {
			AlertasPopup.AvisoModal("Selecione o arquivo original", "Necessário informar o arquivo original primeiro.");
			return;
		}

		String pasta = arquivoVinculado != null ? arquivoVinculado.getPath()
				: (arquivoOriginal != null ? arquivoOriginal.getPath() : null);
		File arquivo = selecionaArquivo("Selecione o arquivo vinculado", pasta);

		if (arquivo != null) {
			arquivoVinculado = arquivo;
			if (!selecionarArquivo())
				carregarArquivo(arquivo, false);
			else
				carregaDados(arquivo, false);
		}
	}

	@FXML
	private void onBtnLimpar() {
		EXECUCOES.addExecucao(abort -> {
			limpar();
			return false;
		});

	}

	@FXML
	private void onBtnDeletar() {
		EXECUCOES.addExecucao(abort -> {
			if (cbBase.getSelectionModel().getSelectedItem() == null || vinculo == null)
				return false;

			try {
				service.delete(cbBase.getSelectionModel().getSelectedItem(), vinculo);
				limpar();
				Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Arquivo deletado com sucesso.");
			} catch (ExcessaoBd e) {
				
				LOGGER.error(e.getMessage(), e);
				AlertasPopup.ErroModal("Erro ao deletar", e.getMessage());
			}

			return false;
		});
	}

	@FXML
	private void onBtnSalvar() {
		EXECUCOES.addExecucao(abort -> {
			if (valida())
				salvar();

			return false;
		});
	}

	@FXML
	private void onBtnRecarregar() {
		EXECUCOES.addExecucao(abort -> {
			if (recarregar()) {
				vincularLegenda(true);
				refreshTabelas(Tabela.ALL);
				Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Recarregar.");
			} else
				Notificacoes.notificacao(Notificacao.ALERTA, "Erro", "Não foi possivel recarregar.");
			return false;
		});
	}

	@FXML
	private void onBtnOrderAutomatico() {
		service.autoReordenarPaginaDupla(false);
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação automatica.");
	}

	@FXML
	private void onBtnOrderPaginaDupla() {
		service.ordenarPaginaDupla(ckbPaginaDuplaCalculada.isSelected());
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação página dupla.");
	}

	@FXML
	private void onBtnOrderPaginaUnica() {
		service.ordenarPaginaSimples();
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação página simples.");
	}

	@FXML
	private void onBtnOrderSequencia() {
		service.reordenarPeloNumeroPagina();
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação pela sequencia.");
	}

	@FXML
	private void onBtnOrderPHash() {
		service.autoReordenarPHash(sldPrecisao.getValue());
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação PHash.");
	}

	@FXML
	private void onBtnOrderHistogram() {
		service.autoReordenarHistogram(sldPrecisao.getValue());
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação Histogram.");
	}

	@FXML
	private void onBtnOrderInteligente() {
		service.autoReordenarInteligente(sldPrecisao.getValue());
		refreshTabelas(Tabela.ALL);

		Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação Inteligente.");
	}

	private Long lastTime = System.currentTimeMillis();
	private final static Integer FAST = 200;
	private final static Integer SLOW = 500;

	@FXML
	private void onDragScroolUp(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > SLOW) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(tvPaginasVinculadas);
			if (index != null && index > 0)
				tvPaginasVinculadas.scrollTo(index - 1);
		}
	}

	@FXML
	private void onDragScroolUpFast(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > FAST) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(tvPaginasVinculadas);
			if (index != null && index > 0)
				tvPaginasVinculadas.scrollTo(index - 1);
		}
	}

	@FXML
	private void onDragScroolDown(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > SLOW) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(tvPaginasVinculadas);
			if (index != null && (index - 1) < tvPaginasVinculadas.getItems().size())
				tvPaginasVinculadas.scrollTo(index + 1);
		}
	}

	@FXML
	private void onDragScroolDownFast(DragEvent event) {
		if ((System.currentTimeMillis() - lastTime) > FAST) {
			lastTime = System.currentTimeMillis();
			Integer index = Util.getFirstVisibleIndex(tvPaginasVinculadas);
			if (index != null && (index - 1) < tvPaginasVinculadas.getItems().size())
				tvPaginasVinculadas.scrollTo(index + 1);
		}
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	@Override
	public Boolean onDuploClique(Node root, VinculoPagina vinculo, Pagina origem) {
		VinculoPagina item = null;
		if (origem == Pagina.VINCULADO_DIREITA)
			item = getVinculoOriginal(origem, vinculo);
		else if (origem == Pagina.VINCULADO_ESQUERDA)
			item = getVinculoOriginal(origem, vinculo);

		if (item != null)
			visualizarLegendas(vinculado.indexOf(item));
		else
			visualizarLegendas(null);

		return true;
	}

	@Override
	public void onDrop(Pagina origem, VinculoPagina vinculoOrigem, Pagina destino, VinculoPagina vinculoDestino) {
		VinculoPagina itemOrigem = getVinculoOriginal(origem, vinculoOrigem);
		VinculoPagina itemDestino = getVinculoOriginal(destino, vinculoDestino);

		if (origem == Pagina.VINCULADO_DIREITA || destino == Pagina.VINCULADO_DIREITA)
			service.onMovimentaDireita(origem, itemOrigem, destino, itemDestino);
		else if (origem == Pagina.VINCULADO_ESQUERDA)
			service.onMovimentaEsquerda(itemOrigem, itemDestino);
		else if (origem == Pagina.NAO_VINCULADO)
			service.fromNaoVinculado(itemOrigem, itemDestino, destino);

		refreshTabelas(Tabela.ALL);
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

	private void refreshTabelas(Tabela tabela) {
		switch (tabela) {
		case VINCULADOS:
			tvPaginasVinculadas.refresh();
			tvPaginasVinculadas.requestLayout();
			if (refreshListener != null)
				refreshListener.refresh();

			break;
		case NAOVINCULADOS:
			lvPaginasNaoVinculadas.refresh();
			lvPaginasNaoVinculadas.requestLayout();
			break;
		default:
			tvPaginasVinculadas.refresh();
			lvPaginasNaoVinculadas.refresh();
			tvPaginasVinculadas.requestLayout();
			lvPaginasNaoVinculadas.requestLayout();
			if (refreshListener != null)
				refreshListener.refresh();
		}
	}

	private void desabilita() {
		btnOrderPaginaUnica.setDisable(true);
		btnOrderPaginaDupla.setDisable(true);
		btnOrderSequencia.setDisable(true);
		btnOrderAutomatico.setDisable(true);
		btnRecarregar.setDisable(true);
		btnOrderPHash.setDisable(true);
		btnOrderHistogram.setDisable(true);
		btnOrderInteligente.setDisable(true);
	}

	private void habilita() {
		btnOrderPaginaUnica.setDisable(false);
		btnOrderPaginaDupla.setDisable(false);
		btnOrderSequencia.setDisable(false);
		btnOrderAutomatico.setDisable(false);
		btnRecarregar.setDisable(false);
		btnOrderPHash.setDisable(false);
		btnOrderHistogram.setDisable(false);
		btnOrderInteligente.setDisable(false);
	}

	private void limpar() {
		EXECUCOES.abortProcess();

		cbLinguagemVinculado.getSelectionModel().select(Language.PORTUGUESE);
		cbLinguagemOrigem.getSelectionModel().select(Language.JAPANESE);
		txtArquivoOriginal.setText("");
		txtArquivoVinculado.setText("");
		ckbPaginaDuplaCalculada.selectedProperty().set(true);
		spnVolume.getValueFactory().setValue(1);

		try {
			automatico = true;
			txtMangaOriginal.setText("");
			txtMangaVinculado.setText("");
		} finally {
			automatico = false;
		}

		setLista(new ArrayList<VinculoPagina>(), new ArrayList<VinculoPagina>());

		Util.destroiParse(parseOriginal);
		Util.destroiParse(parseVinculado);

		arquivoOriginal = null;
		arquivoVinculado = null;
		parseOriginal = null;
		parseVinculado = null;
	}

	private void setLista(List<VinculoPagina> vinculado, List<VinculoPagina> naoVinculado) {
		if (this.vinculado != null) {
			this.vinculado.clear();
			this.naoVinculado.clear();
		}

		this.vinculado = FXCollections.observableArrayList(vinculado);
		this.naoVinculado = FXCollections.observableArrayList(naoVinculado);

		tvPaginasVinculadas.setItems(this.vinculado);
		lvPaginasNaoVinculadas.setItems(this.naoVinculado);
		refreshTabelas(Tabela.ALL);
	}

	private void visualizarLegendas(Integer index) {
		FXMLLoader loader = new FXMLLoader(MangasTextoController.getFxmlLocate());
		try {
			AnchorPane newRoot = loader.load();
			MangasTextoController controlador = loader.getController();
			controlador.setDados(vinculado, this);
			controlador.setControllerPai(this);
			controlador.scroolTo(index);
			refreshListener = controlador;

			new Animacao().abrirPane(this.controller.getStackPane(), newRoot);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private Boolean validaLegendaOriginal() {
		return cbBase.getSelectionModel().getSelectedItem() != null
				&& !cbBase.getSelectionModel().getSelectedItem().isEmpty() && !txtMangaOriginal.getText().isEmpty()
				&& cbLinguagemOrigem.getSelectionModel().getSelectedItem() != null;
	}

	private Boolean validaLegendaVinculado() {
		return cbBase.getSelectionModel().getSelectedItem() != null
				&& !cbBase.getSelectionModel().getSelectedItem().isEmpty() && !txtMangaVinculado.getText().isEmpty()
				&& cbLinguagemVinculado.getSelectionModel().getSelectedItem() != null;
	}

	private Boolean carregarLegendas() {
		if (!validaLegendaOriginal() && !validaLegendaVinculado()) {
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

		return volumeOriginal != null || volumeVinculado != null;
	}

	private Integer itensCapas = 0;

	private void vincularLegenda(Boolean isCarregado) {
		if (vinculo == null || vinculado.isEmpty())
			return;

		EXECUCOES.addExecucao((Boolean abort) -> {
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

						if (!isCarregado) {

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
								vinculo.getVolumeOriginal().getCapitulos().stream().forEach(cp -> {
									cp.getPaginas().stream().forEach(pg -> pg.setCapitulo(cp.getCapitulo()));
									paginasOriginal.addAll(cp.getPaginas());
								});

								itensCapas = Long.valueOf(vinculado.stream()
										.filter(it -> it.getOriginalPathPagina().toLowerCase().contains("capa"))
										.count()).intValue();

								vinculado.parallelStream().forEach(vi -> {
									if (abort)
										return;

									vi.setMangaPaginaOriginal(service.findPagina(paginasOriginal, encontrados,
											vi.getOriginalPathPagina(), vi.getOriginalNomePagina(),
											vi.getOriginalPagina() - itensCapas, vi.getOriginalHash()));

									I++;
									updateProgress(I, Max);
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
													Type.NORMAL);
									});
								});
							}

							if (abort)
								return null;

							if (vinculo.getVolumeVinculado() != null) {
								updateMessage("Vinculando mangas vinculado....");

								final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
								final List<MangaPagina> paginasOriginal = new ArrayList<MangaPagina>();
								vinculo.getVolumeVinculado().getCapitulos().stream().forEach(cp -> {
									cp.getPaginas().stream().forEach(pg -> pg.setCapitulo(cp.getCapitulo()));
									paginasOriginal.addAll(cp.getPaginas());
								});

								itensCapas = Long.valueOf(vinculado.stream()
										.filter(it -> it.getVinculadoEsquerdaPathPagina().toLowerCase().contains("capa")
												|| it.getVinculadoDireitaPathPagina().toLowerCase().contains("capa"))
										.count()
										+ naoVinculado.stream().filter(it -> it.getVinculadoEsquerdaPathPagina()
												.toLowerCase().contains("capa")).count())
										.intValue();

								vinculado.parallelStream().forEach(vi -> {
									if (abort)
										return;

									if (vi.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
										vi.setMangaPaginaEsquerda(service.findPagina(paginasOriginal, encontrados,
												vi.getVinculadoEsquerdaPathPagina(),
												vi.getVinculadoEsquerdaNomePagina(),
												vi.getVinculadoEsquerdaPagina() - itensCapas,
												vi.getVinculadoEsquerdaHash()));

									if (vi.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
										vi.setMangaPaginaDireita(service.findPagina(paginasOriginal, encontrados,
												vi.getVinculadoDireitaPathPagina(), vi.getVinculadoDireitaNomePagina(),
												vi.getVinculadoDireitaPagina() - itensCapas,
												vi.getVinculadoDireitaHash()));

									I++;
									updateProgress(I, Max);
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
													Type.NORMAL);
									});
								});

								if (abort)
									return null;

								naoVinculado.parallelStream().forEach(vi -> {
									if (abort)
										return;

									if (vi.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
										vi.setMangaPaginaEsquerda(service.findPagina(paginasOriginal, encontrados,
												vi.getVinculadoEsquerdaPathPagina(),
												vi.getVinculadoEsquerdaNomePagina(),
												vi.getVinculadoEsquerdaPagina() - itensCapas,
												vi.getVinculadoEsquerdaHash()));

									I++;
									updateProgress(I, Max);
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
													Type.NORMAL);
									});
								});

								if (abort)
									return null;
							}

							I++;
							updateProgress(I, Max);
							Platform.runLater(() -> {
								if (TaskbarProgressbar.isSupported())
									TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max, Type.NORMAL);
							});
						} else {
							Vinculo vinculo = MangasVincularController.this.vinculo;

							ObservableList<VinculoPagina> vinculado = MangasVincularController.this.vinculado;
							ObservableList<VinculoPagina> naoVinculado = MangasVincularController.this.naoVinculado;

							updateMessage("Carregando vinculo salvo....");

							I = 0;
							Max = vinculo.getVinculados().size() + vinculo.getNaoVinculados().size();
							updateProgress(I, Max);
							Platform.runLater(() -> {
								if (TaskbarProgressbar.isSupported())
									TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max, Type.NORMAL);
							});

							for (VinculoPagina pagina : vinculo.getVinculados()) {

								if (abort)
									return null;

								I++;

								pagina.addOriginal(vinculado.get(vinculo.getVinculados().indexOf(pagina)));

								if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
									VinculoPagina paginaCarregada = service.findPagina(vinculado, naoVinculado,
											pagina.getVinculadoEsquerdaPagina());

									if (paginaCarregada == null)
										pagina.limparVinculadoEsquerda(false);
									else if (paginaCarregada.getVinculadoEsquerdaPagina()
											.compareTo(pagina.getVinculadoEsquerdaPagina()) == 0)
										pagina.addVinculoEsquerda(paginaCarregada);
									else
										pagina.addVinculoEsquerdaApartirDireita(paginaCarregada);

								}

								if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA) {
									VinculoPagina paginaCarregada = service.findPagina(vinculado, naoVinculado,
											pagina.getVinculadoDireitaPagina());

									if (paginaCarregada == null)
										pagina.limparVinculadoDireita();
									else if (paginaCarregada.getVinculadoEsquerdaPagina()
											.compareTo(pagina.getVinculadoEsquerdaPagina()) == 0)
										pagina.addVinculoDireitaApartirEsquerda(paginaCarregada);
									else
										pagina.addVinculoDireita(paginaCarregada);
								}

								updateProgress(I, Max);
								Platform.runLater(() -> {
									if (TaskbarProgressbar.isSupported())
										TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
												Type.NORMAL);
								});

							}

							for (VinculoPagina pagina : vinculo.getNaoVinculados()) {

								if (abort)
									return null;

								I++;

								if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
									VinculoPagina paginaCarregada = service.findPagina(vinculado, naoVinculado,
											pagina.getVinculadoEsquerdaPagina());

									if (paginaCarregada == null)
										pagina.limparVinculadoEsquerda(false);
									else if (paginaCarregada.getVinculadoEsquerdaPagina()
											.compareTo(pagina.getVinculadoEsquerdaPagina()) == 0)
										pagina.addVinculoEsquerda(paginaCarregada);
									else
										pagina.addVinculoEsquerdaApartirDireita(paginaCarregada);
								}

								updateProgress(I, Max);
								Platform.runLater(() -> {
									if (TaskbarProgressbar.isSupported())
										TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, Max,
												Type.NORMAL);
								});
							}

							vinculo.getNaoVinculados()
									.removeIf(it -> it.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA);

							Platform.runLater(() -> setLista(vinculo.getVinculados(), vinculo.getNaoVinculados()));

						}
					} catch (Exception e) {
						
						LOGGER.error(e.getMessage(), e);
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
						refreshTabelas(Tabela.VINCULADOS);

						EXECUCOES.endProcess();
					});

				}

				@Override
				protected void failed() {
					super.failed();
					LOGGER.warn("Falha ao executar a thread de vincular a legenda: " + super.getMessage());
					Platform.runLater(() -> {
						progress.getBarraProgresso().progressProperty().unbind();
						progress.getLog().textProperty().unbind();

						MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

						MenuPrincipalController.getController().getLblLog().setText("");
						habilita();
						refreshTabelas(Tabela.VINCULADOS);

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

		if (carregar()) {
			Vinculo vinculo = this.vinculo;

			txtArquivoOriginal.setText(vinculo.getNomeArquivoOriginal());
			txtArquivoVinculado.setText(vinculo.getNomeArquivoVinculado());

			if (cbLinguagemOrigem.getSelectionModel().getSelectedItem() == null)
				cbLinguagemOrigem.getSelectionModel().select(vinculo.getLinguagemOriginal());
			else if (vinculo.getLinguagemOriginal() != null && cbLinguagemOrigem.getSelectionModel().getSelectedItem()
					.compareTo(vinculo.getLinguagemOriginal()) != 0) {
				if (AlertasPopup.ConfirmacaoModal("Aviso",
						"A linguagem selecionada e o manga original são diferentes.\nDeseja recarregar?")) {
					cbLinguagemOrigem.getSelectionModel().select(vinculo.getLinguagemOriginal());
					return selecionarArquivo();
				}
			}

			if (cbLinguagemVinculado.getSelectionModel().getSelectedItem() == null)
				cbLinguagemVinculado.getSelectionModel().select(vinculo.getLinguagemVinculado());
			else if (vinculo.getLinguagemVinculado() != null && cbLinguagemVinculado.getSelectionModel()
					.getSelectedItem().compareTo(vinculo.getLinguagemVinculado()) != 0) {
				if (AlertasPopup.ConfirmacaoModal("Aviso",
						"A linguagem selecionada e o manga vinculado são diferentes.\nDeseja recarregar?")) {
					cbLinguagemVinculado.getSelectionModel().select(vinculo.getLinguagemVinculado());
					return selecionarArquivo();
				}
			}

			try {
				automatico = true;
				if (vinculo.getVolumeOriginal() != null)
					txtMangaOriginal.setText(vinculo.getVolumeOriginal().getManga());

				if (vinculo.getVolumeVinculado() != null)
					txtMangaVinculado.setText(vinculo.getVolumeVinculado().getManga());
			} finally {
				automatico = false;
			}

			return true;
		}

		return false;
	}

	private MangaVolume getMangaVolume(Boolean isOriginal) {
		MangaVolume manga = null;
		if (isOriginal) {
			manga = vinculo.getVolumeOriginal();
			if (manga == null && validaLegendaOriginal()) {
				manga = service.selectVolume(cbBase.getSelectionModel().getSelectedItem(), txtMangaOriginal.getText(),
						spnVolume.getValue(), cbLinguagemOrigem.getSelectionModel().getSelectedItem());
				vinculo.setVolumeOriginal(manga);
			}
		} else {
			manga = vinculo.getVolumeVinculado();
			if (manga == null && validaLegendaVinculado()) {
				manga = service.selectVolume(cbBase.getSelectionModel().getSelectedItem(), txtMangaVinculado.getText(),
						spnVolume.getValue(), cbLinguagemVinculado.getSelectionModel().getSelectedItem());
				vinculo.setVolumeVinculado(manga);
			}
		}

		return manga;
	}

	private Pair<Image, Atributos> carregaImagem(Parse parse, int pagina) {
		Image image = null;
		InputStream imput = null;
		Boolean dupla = false;
		String md5 = "";
		try {
			md5 = Util.MD5(parse.getPagina(pagina));
			imput = parse.getPagina(pagina);
			image = new Image(imput);
			dupla = (image.getWidth() / image.getHeight()) > 0.9;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		return new Pair<Image, Atributos>(image, new Atributos(dupla, md5, ""));
	}

	private void carregarArquivo(File arquivo, Boolean isManga) {
		if (arquivo == null)
			return;

		Parse parse = Util.criaParse(arquivo);

		if (parse != null) {

			EXECUCOES.addExecucao(abort -> {

				desabilita();
				GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
				progress.getTitulo().setText("Vinculando legendas");
				if (TaskbarProgressbar.isSupported())
					TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

				Task<Void> carregar = new Task<Void>() {
					String error = "";
					Integer X = 0, SIZE = 0;

					@Override
					protected Void call() throws Exception {
						try {

							error = "";
							updateMessage("Carregando manga....");

							if (isManga) {
								Platform.runLater(() -> {
									txtArquivoOriginal.setText(arquivo.getName());
									txtArquivoVinculado.setText("");
								});
								MangaVolume volume = getMangaVolume(true);

								updateMessage("Carregando manga original....");

								final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
								final List<MangaPagina> paginas = new ArrayList<MangaPagina>();

								if (volume != null)
									volume.getCapitulos().stream().forEach(it -> paginas.addAll(it.getPaginas()));

								itensCapas = Long.valueOf(
										parse.getPastas().keySet().stream().filter(k -> k.contains("capa")).count())
										.intValue();

								ArrayList<VinculoPagina> list = new ArrayList<VinculoPagina>();
								for (int x = 0; x < parse.getSize(); x++) {
									Pair<Image, Atributos> image = carregaImagem(parse, x);
									Atributos detalhe = image.getValue();
									String path = parse.getPaginaPasta(x);

									list.add(new VinculoPagina(Util.getNome(path), Util.getPasta(path), x,
											parse.getSize(), detalhe.getDupla(),
											service.findPagina(paginas, encontrados, path, Util.getPasta(path),
													x - itensCapas, detalhe.getMd5()),
											image.getKey(), detalhe.getMd5(), "", null));

									X = x;
									SIZE = parse.getSize();
									updateProgress(X, SIZE);
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE,
													Type.NORMAL);
									});

									if (abort)
										break;

								}

								if (!abort) {
									Util.destroiParse(parseOriginal);
									Util.destroiParse(parseVinculado);

									parseOriginal = parse;
									parseVinculado = null;

									Util.getCapitulos(parseOriginal, capitulosOriginal, lvCapitulosOriginal);
									Util.getCapitulos(parseVinculado, capitulosVinculado, lvCapitulosVinculado);

									Platform.runLater(() -> setLista(list, new ArrayList<VinculoPagina>()));
								} else
									limpar();
							} else {
								Platform.runLater(() -> txtArquivoVinculado.setText(arquivo.getName()));

								updateMessage("Carregando manga vinculado....");

								final List<MangaPagina> encontrados = new ArrayList<MangaPagina>();
								final List<MangaPagina> paginas = new ArrayList<MangaPagina>();

								MangaVolume volume = getMangaVolume(false);
								if (volume != null)
									volume.getCapitulos().stream().forEach(it -> paginas.addAll(it.getPaginas()));

								itensCapas = Long.valueOf(
										parse.getPastas().keySet().stream().filter(k -> k.contains("capa")).count())
										.intValue();

								final List<VinculoPagina> vinculado = new ArrayList<VinculoPagina>(
										MangasVincularController.this.vinculado);
								final List<VinculoPagina> naoVinculado = new ArrayList<VinculoPagina>();

								for (int x = 0; x < parse.getSize(); x++) {
									Pair<Image, Atributos> image = carregaImagem(parse, x);
									Atributos detalhe = image.getValue();
									String path = parse.getPaginaPasta(x);

									if (x < vinculado.size()) {
										VinculoPagina item = vinculado.get(x);

										item.limparVinculado();
										item.setVinculadoEsquerdaPagina(x);
										item.setVinculadoEsquerdaNomePagina(Util.getNome(path));
										item.setVinculadoEsquerdaPathPagina(Util.getPasta(path));
										item.setVinculadoEsquerdaPaginas(parse.getSize());
										item.isVinculadoEsquerdaPaginaDupla = detalhe.getDupla();
										item.setImagemVinculadoEsquerda(image.getKey());
										item.setVinculadoEsquerdaHash(detalhe.getMd5());
										item.setMangaPaginaEsquerda(service.findPagina(paginas, encontrados, path,
												Util.getPasta(path), x - itensCapas, detalhe.getMd5()));
									} else
										naoVinculado.add(new VinculoPagina(Util.getNome(path), Util.getPasta(path), x,
												parse.getSize(), detalhe.getDupla(),
												service.findPagina(paginas, encontrados, path, Util.getPasta(path),
														x - itensCapas, detalhe.getMd5()),
												image.getKey(), true, detalhe.getMd5(), "", null));

									X = x;
									SIZE = parse.getSize();
									updateProgress(X, SIZE);
									Platform.runLater(() -> {
										if (TaskbarProgressbar.isSupported())
											TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE,
													Type.NORMAL);
									});

									if (abort)
										break;
								}

								if (!abort) {
									Platform.runLater(() -> setLista(vinculado, naoVinculado));
									Util.destroiParse(parseVinculado);
									parseVinculado = parse;
									Platform.runLater(() -> Util.getCapitulos(parseVinculado, capitulosVinculado,
											lvCapitulosVinculado));
								}
							}

						} catch (Exception e) {
							
							LOGGER.error(e.getMessage(), e);
							error = e.getMessage();
						}
						return null;
					}

					@Override
					protected void succeeded() {
						Platform.runLater(() -> {
							service.gerarAtributos(parse, isManga);

							progress.getBarraProgresso().progressProperty().unbind();
							progress.getLog().textProperty().unbind();

							MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
							TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

							if (!error.isEmpty())
								AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
										error);

							MenuPrincipalController.getController().getLblLog().setText("");
							habilita();
							refreshTabelas(Tabela.VINCULADOS);

							EXECUCOES.endProcess();
						});

					}

					@Override
					protected void failed() {
						super.failed();
						LOGGER.warn("Falha ao executar a thread de carregar arquivos: " + super.getMessage());
						Platform.runLater(() -> {
							progress.getBarraProgresso().progressProperty().unbind();
							progress.getLog().textProperty().unbind();

							MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
							TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

							MenuPrincipalController.getController().getLblLog().setText("");
							habilita();
							refreshTabelas(Tabela.VINCULADOS);

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
		EXECUCOES.addExecucao(abort -> {
			desabilita();
			GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
			progress.getTitulo().setText("Carregando dados.");
			if (TaskbarProgressbar.isSupported())
				TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

			Task<Void> carregar = new Task<Void>() {
				Integer X = 0, SIZE = 0;

				@Override
				protected Void call() throws Exception {

					updateMessage("Carregando dados....");

					if (isManga) {
						Util.destroiParse(parseOriginal);
						parseOriginal = Util.criaParse(arquivo);
						Util.getCapitulos(parseOriginal, capitulosOriginal, lvCapitulosOriginal);
					} else {
						Util.destroiParse(parseVinculado);
						parseVinculado = Util.criaParse(arquivo);
						Util.getCapitulos(parseVinculado, capitulosVinculado, lvCapitulosVinculado);

					}

					List<VinculoPagina> original = MangasVincularController.this.vinculado;
					List<VinculoPagina> vinculado = MangasVincularController.this.vinculo.getVinculados();
					List<VinculoPagina> naoVinculado = MangasVincularController.this.vinculo.getNaoVinculados();

					SIZE = vinculado.size() + naoVinculado.size();

					for (VinculoPagina pagina : vinculado) {
						if (isManga) {
							Pair<Image, Atributos> image = carregaImagem(parseOriginal, pagina.getOriginalPagina());
							pagina.isOriginalPaginaDupla = image.getValue().getDupla();
							pagina.setImagemOriginal(image.getKey());
							pagina.setOriginalHash(image.getValue().getMd5());
						} else {
							pagina.addOriginalSemId(original.get(vinculado.indexOf(pagina)));

							if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
								Pair<Image, Atributos> image = carregaImagem(parseVinculado,
										pagina.getVinculadoEsquerdaPagina());
								pagina.isVinculadoEsquerdaPaginaDupla = image.getValue().getDupla();
								pagina.setImagemVinculadoEsquerda(image.getKey());
								pagina.setVinculadoEsquerdaHash(image.getValue().getMd5());
							}

							if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA) {
								Pair<Image, Atributos> image = carregaImagem(parseVinculado,
										pagina.getVinculadoDireitaPagina());
								pagina.isVinculadoDireitaPaginaDupla = image.getValue().getDupla();
								pagina.setImagemVinculadoDireita(image.getKey());
								pagina.setVinculadoDireitaHash(image.getValue().getMd5());
							}
						}

						X++;
						updateProgress(X, SIZE);
						Platform.runLater(() -> {
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE, Type.NORMAL);
						});
					}

					if (!isManga) {
						for (VinculoPagina pagina : naoVinculado) {
							if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
								Pair<Image, Atributos> image = carregaImagem(parseVinculado,
										pagina.getVinculadoEsquerdaPagina());
								pagina.isVinculadoEsquerdaPaginaDupla = image.getValue().getDupla();
								pagina.setImagemVinculadoEsquerda(image.getKey());
								pagina.setVinculadoEsquerdaHash(image.getValue().getMd5());
							}

							X++;
							updateProgress(X, SIZE);
							Platform.runLater(() -> {
								if (TaskbarProgressbar.isSupported())
									TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE, Type.NORMAL);
							});
						}
					}

					Platform.runLater(() -> setLista(vinculado, naoVinculado));
					return null;
				}

				@Override
				protected void succeeded() {
					Platform.runLater(() -> {
						progress.getBarraProgresso().progressProperty().unbind();
						progress.getLog().textProperty().unbind();

						MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

						MenuPrincipalController.getController().getLblLog().setText("");
						habilita();
						refreshTabelas(Tabela.VINCULADOS);

						EXECUCOES.endProcess();
					});

				}

				@Override
				protected void failed() {
					super.failed();
					LOGGER.warn("Falha ao executar a thread de carregamento de dados: " + super.getMessage());
					Platform.runLater(() -> {
						progress.getBarraProgresso().progressProperty().unbind();
						progress.getLog().textProperty().unbind();

						MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

						MenuPrincipalController.getController().getLblLog().setText("");
						habilita();
						refreshTabelas(Tabela.VINCULADOS);

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

	private Vinculo limpaVinculo(Vinculo vinculo) {
		vinculo.setId(null);
		vinculo.setVolumeVinculado(null);
		vinculo.setLinguagemVinculado(null);
		vinculo.setNomeArquivoVinculado("");
		vinculo.getVinculados().stream().forEach(it -> {
			it.setId(null);
			it.limparVinculado();
		});
		vinculo.setNaoVinculados(new ArrayList<VinculoPagina>());
		return vinculo;
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

	private Boolean recarregar() {
		try {
			Vinculo vinculo = service.select(cbBase.getSelectionModel().getSelectedItem(), this.vinculo.getId());
			if (vinculo != null) {
				this.vinculo = vinculo;
				return true;
			}
		} catch (ExcessaoBd e) {
			LOGGER.error(e.getMessage(), e);
		}

		return false;
	}

	private Boolean carregar() {
		Boolean carregado = false;

		if (cbBase.getSelectionModel().getSelectedItem() != null
				&& !cbBase.getSelectionModel().getSelectedItem().isEmpty()) {
			try {

				if (arquivoOriginal != null && arquivoVinculado != null) {
					String arquivoOriginal = this.arquivoOriginal != null ? this.arquivoOriginal.getName() : "";
					String arquivoVinculado = this.arquivoVinculado != null ? this.arquivoVinculado.getName() : "";
					Vinculo vinculo = service.select(cbBase.getSelectionModel().getSelectedItem(), spnVolume.getValue(),
							txtMangaOriginal.getText(), cbLinguagemOrigem.getSelectionModel().getSelectedItem(),
							arquivoOriginal, txtMangaVinculado.getText(),
							cbLinguagemVinculado.getSelectionModel().getSelectedItem(), arquivoVinculado);

					if (vinculo != null) {
						carregado = true;
						this.vinculo = vinculo;
					} else
						this.vinculo = limpaVinculo(this.vinculo);

				} else if (arquivoOriginal != null) {
					String arquivoOriginal = this.arquivoOriginal != null ? this.arquivoOriginal.getName() : "";
					Vinculo vinculo = service.select(cbBase.getSelectionModel().getSelectedItem(), spnVolume.getValue(),
							txtMangaOriginal.getText(), cbLinguagemOrigem.getSelectionModel().getSelectedItem(),
							arquivoOriginal, "", null, "");
					if (vinculo != null) {
						carregado = true;
						this.vinculo = limpaVinculo(vinculo);
					}
				}
			} catch (ExcessaoBd e) {
				
				LOGGER.error(e.getMessage(), e);
			}
		}

		return carregado;
	}

	private void salvar() {
		vinculo.setUltimaAlteracao(LocalDateTime.now());
		vinculo.setBase(cbBase.getSelectionModel().getSelectedItem());
		vinculo.setVolume(spnVolume.getValue());
		vinculo.setLinguagemOriginal(cbLinguagemOrigem.getSelectionModel().getSelectedItem());
		vinculo.setLinguagemVinculado(cbLinguagemVinculado.getSelectionModel().getSelectedItem());
		vinculo.setNomeArquivoOriginal(txtArquivoOriginal.getText());
		vinculo.setNomeArquivoVinculado(txtArquivoVinculado.getText());
		vinculo.setVinculados(vinculado);
		vinculo.setNaoVinculados(naoVinculado);

		try {
			service.salvar(cbBase.getSelectionModel().getSelectedItem(), vinculo);
			Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Salvo com sucesso.");
		} catch (ExcessaoBd e) {
			LOGGER.error(e.getMessage(), e);
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

	private final InvalidationListener acMangaOriginal = observable -> {
		if (cbBase.getItems().isEmpty())
			cbBase.setUnFocusColor(Color.RED);

		autoCompleteMangaOriginal
				.filter(string -> string.toLowerCase().contains(txtMangaOriginal.getText().toLowerCase()));
		if (autoCompleteMangaOriginal.getFilteredSuggestions().isEmpty() || txtMangaOriginal.getText().isEmpty()
				|| automatico)
			autoCompleteMangaOriginal.hide();
		else
			autoCompleteMangaOriginal.show(txtMangaOriginal);
	};

	private final InvalidationListener acMangaVinculado = observable -> {
		if (cbBase.getItems().isEmpty())
			cbBase.setUnFocusColor(Color.RED);

		autoCompleteMangaVinculado
				.filter(string -> string.toLowerCase().contains(txtMangaVinculado.getText().toLowerCase()));
		if (autoCompleteMangaVinculado.getFilteredSuggestions().isEmpty() || txtMangaVinculado.getText().isEmpty()
				|| automatico)
			autoCompleteMangaVinculado.hide();
		else
			autoCompleteMangaVinculado.show(txtMangaVinculado);
	};

	public void setAutoCompleteListener(Boolean isClear) {
		if (isClear) {
			txtMangaOriginal.textProperty().removeListener(acMangaOriginal);
			txtMangaVinculado.textProperty().removeListener(acMangaVinculado);

			autoCompleteMangaOriginal.setSelectionHandler(null);
			autoCompleteMangaVinculado.setSelectionHandler(null);
		} else {
			txtMangaOriginal.textProperty().addListener(acMangaOriginal);
			txtMangaVinculado.textProperty().addListener(acMangaVinculado);

			autoCompleteMangaOriginal.setSelectionHandler(event -> {
				txtMangaOriginal.setText(event.getObject());
			});

			autoCompleteMangaVinculado.setSelectionHandler(event -> {
				txtMangaVinculado.setText(event.getObject());
			});

		}
	}

	private void selecionaBase(String base) {
		autoCompleteMangaOriginal.getSuggestions().clear();
		autoCompleteMangaVinculado.getSuggestions().clear();

		if (base == null || base.isEmpty())
			return;

		try {
			service.createTabelas(base);
		} catch (ExcessaoBd e) {
			LOGGER.error(e.getMessage(), e);
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
			LOGGER.error(e.getMessage(), e);
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
			Optional<VinculoPagina> pagina = tvPaginasVinculadas.getItems().stream()
					.filter(pg -> pg.getOriginalPagina().compareTo(numero) == 0).findFirst();
			if (pagina.isPresent())
				tvPaginasVinculadas.scrollTo(pagina.get());
		} else {
			Integer numero = capitulosVinculado.get(capitulo);
			Optional<VinculoPagina> pagina = tvPaginasVinculadas.getItems().stream()
					.filter(pg -> pg.getVinculadoEsquerdaPagina().compareTo(numero) == 0
							|| pg.getVinculadoDireitaPagina().compareTo(numero) == 0)
					.findFirst();
			if (pagina.isPresent())
				tvPaginasVinculadas.scrollTo(pagina.get());
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
					refreshTabelas(Tabela.ALL);
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
		tvPaginasVinculadas.setSelectionModel(new TableViewNoSelectionModel<VinculoPagina>(tvPaginasVinculadas));
		lvPaginasNaoVinculadas.setSelectionModel(new ListViewNoSelectionModel<VinculoPagina>());

		tcMangaOriginal.setCellValueFactory(new PropertyValueFactory<>("originalPagina"));
		tcMangaOriginal
				.setCellFactory(new Callback<TableColumn<VinculoPagina, Integer>, TableCell<VinculoPagina, Integer>>() {
					@Override
					public TableCell<VinculoPagina, Integer> call(TableColumn<VinculoPagina, Integer> param) {
						TableCell<VinculoPagina, Integer> cell = new TableCell<VinculoPagina, Integer>() {
							@Override
							public void updateItem(Integer item, boolean empty) {
								setText(null);
								if (empty || item == null)
									setGraphic(null);
								else {
									FXMLLoader mLLoader = new FXMLLoader(
											MangasVincularCelulaSimplesController.getFxmlLocate());

									try {
										mLLoader.load();
										MangasVincularCelulaSimplesController controller = mLLoader.getController();
										controller.setDados(getTableRow().getItem());
										setGraphic(controller.root);
									} catch (IOException e) {
										
										LOGGER.error(e.getMessage(), e);
										setGraphic(null);
									}
								}
							}
						};
						return cell;
					}
				});

		tcMangaVinculado.setCellValueFactory(new PropertyValueFactory<>("vinculadoEsquerdaPagina"));
		tcMangaVinculado
				.setCellFactory(new Callback<TableColumn<VinculoPagina, Integer>, TableCell<VinculoPagina, Integer>>() {
					@Override
					public TableCell<VinculoPagina, Integer> call(TableColumn<VinculoPagina, Integer> param) {
						TableCell<VinculoPagina, Integer> cell = new TableCell<VinculoPagina, Integer>() {
							@Override
							public void updateItem(Integer item, boolean empty) {
								setText(null);
								if (empty || item == null)
									setGraphic(null);
								else {
									FXMLLoader mLLoader = new FXMLLoader(
											MangasVincularCelulaDuplaController.getFxmlLocate());

									try {
										mLLoader.load();
										MangasVincularCelulaDuplaController controller = mLLoader.getController();
										controller.setDados(getTableRow().getItem());
										controller.setListener(MangasVincularController.this);
										HBox.setHgrow(controller.root, Priority.ALWAYS);
										setGraphic(controller.root);
									} catch (IOException e) {
										
										LOGGER.error(e.getMessage(), e);
										setGraphic(null);
									}
								}
							}
						};
						return cell;
					}
				});

		tvPaginasVinculadas.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {
				Pane header = (Pane) tvPaginasVinculadas.lookup("TableHeaderRow");
				if (header.isVisible()) {
					header.setMaxHeight(0);
					header.setMinHeight(0);
					header.setPrefHeight(0);
					header.setVisible(false);
				}
			}
		});

		lvPaginasNaoVinculadas.setCellFactory(new Callback<ListView<VinculoPagina>, ListCell<VinculoPagina>>() {
			@Override
			public ListCell<VinculoPagina> call(ListView<VinculoPagina> list) {
				ListCell<VinculoPagina> cell = new ListCell<VinculoPagina>() {
					@Override
					public void updateItem(VinculoPagina item, boolean empty) {
						super.updateItem(item, empty);

						setText(null);
						if (empty || item == null)
							setGraphic(null);
						else {
							FXMLLoader mLLoader = new FXMLLoader(MangasVincularCelulaPequenaController.getFxmlLocate());

							try {
								mLLoader.load();
							} catch (IOException e) {
								
								LOGGER.error(e.getMessage(), e);
							}

							MangasVincularCelulaPequenaController controller = mLLoader.getController();
							controller.setDados(item);
							controller.setListener(MangasVincularController.this);

							setGraphic(controller.root);
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

	private final Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		Run.getPrimaryStage().setOnCloseRequest(e -> onClose());
		service.setListener(this);

		try {
			cbBase.getItems().setAll(service.getTabelas());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
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

		cbBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
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

		setAutoCompleteListener(false);

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

		cbLinguagemOrigem.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
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
		return MangasVincularController.class.getResource("/view/mangas/MangaVincular.fxml");
	}

}
