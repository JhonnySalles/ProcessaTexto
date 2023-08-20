package org.jisho.textosJapones.components.listener;

import javafx.scene.Node;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;

public interface VinculoListener {
	Boolean onDuploClique(Node root, VinculoPagina vinculo, Pagina origem);

	void onDrop(Pagina origem, VinculoPagina vinculoOrigem, Pagina destino, VinculoPagina vinculoDestino);

	void onDragStart();

	void onDragEnd();
}
