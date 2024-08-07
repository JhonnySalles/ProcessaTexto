package org.jisho.textosJapones.components.notification;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jisho.textosJapones.controller.PopupAlertaController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Classe responssável por apresentar alertas em janela, podendo ser um alerta
 * com borda (tala do windows) ou sem borda.
 * </p>
 * 
 * @author Jhonny de Salles Noschang
 */
public class Alertas {

	private static final Logger LOGGER = LoggerFactory.getLogger(Alertas.class);

	public static void Tela_Alerta(String titulo, String texto) {
		try {
			FXMLLoader loader = new FXMLLoader(PopupAlertaController.getFxmlLocate());
			AnchorPane scPnTelaPrincipal = loader.load();

			// Obtem a referencia do controller para editar as label.
			PopupAlertaController controller = loader.getController();

			controller.setTexto(titulo, texto);

			Scene tela = new Scene(scPnTelaPrincipal); // Carrega a scena

			Stage stageTela = new Stage();
			stageTela.setScene(tela); // Seta a cena principal

			controller.setEventosBotoes(ok -> {
				stageTela.close();
			});

			// Faz a tela ser obrigatoria para voltar ao voltar a tela anterior
			stageTela.setTitle(titulo);
			stageTela.getIcons().add(PopupAlertaController.IMG_ALERTA);
			stageTela.initModality(Modality.APPLICATION_MODAL);

			controller.setVisivel(true, true);

			stageTela.showAndWait(); // Mostra a tela.
		} catch (Exception e) {
			System.out.println("Erro ao tentar carregar o alerta.");
			
			LOGGER.error(e.getMessage(), e);
		}
	}

}
