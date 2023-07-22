package org.jisho.textosJapones.components.listener;

import javafx.scene.Node;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;

public interface VinculoListener {
	public Boolean onDuploClique(Node root, VinculoPagina vinculo, Pagina origem);

	public void onDrop(Pagina origem, VinculoPagina vinculoOrigem, Pagina destino, VinculoPagina vinculoDestino);

	public void onDragStart();

	public void onDragEnd();
}
