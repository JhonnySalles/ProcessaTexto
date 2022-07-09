package org.jisho.textosJapones.util.listener;

import org.jisho.textosJapones.model.entities.VinculoPagina;

import javafx.collections.ObservableList;

public interface VinculoServiceListener {

	public ObservableList<VinculoPagina> getVinculados();

	public ObservableList<VinculoPagina> getNaoVinculados();
	
}
