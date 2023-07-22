package org.jisho.textosJapones.components.listener;

import javafx.collections.ObservableList;
import org.jisho.textosJapones.model.entities.VinculoPagina;

public interface VinculoServiceListener {

	public ObservableList<VinculoPagina> getVinculados();

	public ObservableList<VinculoPagina> getNaoVinculados();
	
}
