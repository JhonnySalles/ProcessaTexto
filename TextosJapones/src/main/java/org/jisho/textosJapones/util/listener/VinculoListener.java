package org.jisho.textosJapones.util.listener;

import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;

import javafx.scene.Node;

public interface VinculoListener {
	public Boolean onDuploClique(Node root, VinculoPagina vinculo, Pagina origem);

	public void onDrop(Pagina origem, VinculoPagina vinculoOrigem, Pagina destino, VinculoPagina vinculoDestino);

	public void onDragStart();

	public void onDragEnd();
}
