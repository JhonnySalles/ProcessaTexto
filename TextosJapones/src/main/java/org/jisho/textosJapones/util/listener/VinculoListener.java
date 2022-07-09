package org.jisho.textosJapones.util.listener;

import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Pagina;

import javafx.scene.Node;
import javafx.scene.effect.Light.Point;

public interface VinculoListener {
	public Boolean onClique(Node root, VinculoPagina vinculo, Pagina origem);

	public void onDrop(Pagina origem, Pagina destino, String dragIndex, VinculoPagina vinculo);

	public void onDragScrolling(Point pointScreen);

	public void onDragStart();

	public void onDragEnd();
}
