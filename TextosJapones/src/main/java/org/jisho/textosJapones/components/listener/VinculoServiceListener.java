package org.jisho.textosJapones.components.listener;

import javafx.collections.ObservableList;
import org.jisho.textosJapones.model.entities.VinculoPagina;

public interface VinculoServiceListener {

	ObservableList<VinculoPagina> getVinculados();

	ObservableList<VinculoPagina> getNaoVinculados();
	
}
