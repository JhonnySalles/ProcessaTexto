package org.jisho.textosJapones.controller.mangas;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jisho.textosJapones.components.listener.VinculoListener;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.util.Util;

import java.net.URL;
import java.util.ResourceBundle;

public class MangasVincularCelulaDuplaController implements Initializable {

	final PseudoClass ON_DRAG_SELECIONADO = PseudoClass.getPseudoClass("drag-selecionado");

	@FXML
	public HBox root;

	@FXML
	public AnchorPane esquerdaContainer;

	@FXML
	public ImageView esquerdaImagem;

	@FXML
	public Text esquerdaNumero;

	@FXML
	public VBox esquerdaNome;

	@FXML
	public Text esquerdaNomePagina;

	@FXML
	public AnchorPane direitaContainer;

	@FXML
	public ImageView direitaImagem;

	@FXML
	public Text direitaNumero;

	@FXML
	public VBox direitaNome;

	@FXML
	public Text direitaNomePagina;

	private VinculoListener listener;

	public void setDados(VinculoPagina vinculo) {
		root.setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (vinculo.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
					direitaContainer.setVisible(true);
					direitaContainer.setPrefWidth(esquerdaContainer.getWidth());
				}

			}
		});

		root.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (!vinculo.isImagemDupla && vinculo.getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA)
					direitaContainer.setVisible(false);

			}
		});

		root.setOnDragDone(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (!vinculo.isImagemDupla && vinculo.getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA)
					direitaContainer.setVisible(false);
			}
		});
		
		if (vinculo != null) {
			configuraDrop(esquerdaContainer, vinculo, Pagina.VINCULADO_ESQUERDA);
			if (vinculo.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
				esquerdaContainer.setOnDragDetected(null);
				esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
				esquerdaNumero.setText("");
				esquerdaNomePagina.setText("");
				esquerdaNome.setVisible(false);
			} else {
				esquerdaContainer.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						listener.onDragStart();
						Dragboard db = esquerdaContainer.startDragAndDrop(TransferMode.ANY);

						db.setDragView(Util.criaSnapshot(esquerdaContainer), event.getX(), event.getY());

						vinculo.onDragOrigem = Pagina.VINCULADO_ESQUERDA;
						ClipboardContent content = new ClipboardContent();
						content.put(Util.VINCULO_ITEM_FORMAT, vinculo);
						content.put(Util.NUMERO_PAGINA_ITEM_FORMAT, vinculo.getVinculadoEsquerdaPagina().toString());
						content.putString(vinculo.getVinculadoEsquerdaNomePagina());
						db.setContent(content);

						event.consume();
					}
				});

				esquerdaContainer.setOnDragDone(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						listener.onDragEnd();
						direitaContainer.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);
					}
				});

				esquerdaImagem.setImage(vinculo.getImagemVinculadoEsquerda());
				esquerdaNumero.setText(vinculo.getVinculadoEsquerdaPagina().toString());
				esquerdaNomePagina.setText(vinculo.getVinculadoEsquerdaNomePagina());
				esquerdaNome.setVisible(true);
			}

			configuraDrop(direitaContainer, vinculo, Pagina.VINCULADO_DIREITA);

			if (vinculo.getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA) {
				direitaContainer.setOnDragDetected(null);
				direitaContainer.setVisible(false);
				direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
				direitaNumero.setText("");
				direitaNomePagina.setText("");
			} else {
				direitaContainer.setOnDragDetected(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						listener.onDragStart();

						Dragboard db = esquerdaContainer.startDragAndDrop(TransferMode.ANY);

						vinculo.onDragOrigem = Pagina.VINCULADO_DIREITA;
						ClipboardContent content = new ClipboardContent();
						content.put(Util.VINCULO_ITEM_FORMAT, vinculo);
						content.put(Util.NUMERO_PAGINA_ITEM_FORMAT, vinculo.getVinculadoEsquerdaPagina().toString());
						content.putString(vinculo.getVinculadoEsquerdaNomePagina());
						db.setContent(content);

						event.consume();
					}
				});

				direitaContainer.setOnDragDone(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						listener.onDragEnd();
						direitaContainer.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);
					}
				});

				direitaContainer.setVisible(vinculo.getImagemVinculadoDireita() != null);
				direitaImagem.setImage(vinculo.getImagemVinculadoDireita());
				direitaNumero.setText(vinculo.getVinculadoDireitaPagina().toString());
				direitaNomePagina.setText(vinculo.getVinculadoDireitaNomePagina());
			}
		} else {
			configuraDrop(esquerdaContainer, vinculo, Pagina.VINCULADO_DIREITA);
			esquerdaImagem.setImage(null);
			esquerdaNumero.setText("");
			esquerdaNomePagina.setText("");
			esquerdaNome.setVisible(false);

			direitaContainer.setVisible(false);
			direitaImagem.setImage(null);
			direitaNumero.setText("");
			direitaNomePagina.setText("");
			limpaDrop(direitaContainer);
		}
	}

	private void limpaDrop(AnchorPane pane) {
		pane.setOnDragEntered(null);
		pane.setOnDragExited(null);
	}

	private void configuraDrop(AnchorPane pane, VinculoPagina destino, Pagina tipo) {
		pane.setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.getGestureSource() != pane && event.getDragboard().hasContent(Util.VINCULO_ITEM_FORMAT))
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

				event.consume();
			}
		});

		pane.setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.getGestureSource() != pane)
					pane.pseudoClassStateChanged(ON_DRAG_SELECIONADO, true);

				event.consume();
			}
		});

		pane.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				pane.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false);

				event.consume();
			}
		});

		pane.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasContent(Util.VINCULO_ITEM_FORMAT)) {
					VinculoPagina vinculo = (VinculoPagina) db.getContent(Util.VINCULO_ITEM_FORMAT);

					listener.onDrop(vinculo.onDragOrigem, vinculo, tipo, destino);
					success = true;
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});
	}

	public void setListener(VinculoListener listener) {
		this.listener = listener;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		esquerdaImagem.fitWidthProperty().bind(esquerdaContainer.widthProperty());
		esquerdaImagem.fitHeightProperty().bind(esquerdaContainer.heightProperty());
		esquerdaImagem.setPreserveRatio(true);
		esquerdaImagem.fitWidthProperty().bind(esquerdaNome.widthProperty());

		direitaImagem.fitWidthProperty().bind(direitaContainer.widthProperty());
		direitaImagem.fitHeightProperty().bind(direitaContainer.heightProperty());
		direitaImagem.setPreserveRatio(true);
		direitaImagem.fitWidthProperty().bind(direitaNome.widthProperty());

		direitaContainer.managedProperty().bind(direitaContainer.visibleProperty());

	}

	public static URL getFxmlLocate() {
		return MangasVincularCelulaDuplaController.class.getResource("/view/mangas/MangaVincularCelulaDupla.fxml");
	}
}
