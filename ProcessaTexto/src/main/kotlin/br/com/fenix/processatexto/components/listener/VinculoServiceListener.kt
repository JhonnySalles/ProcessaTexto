package br.com.fenix.processatexto.components.listener

import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import javafx.collections.ObservableList

interface VinculoServiceListener {
    val vinculados: ObservableList<VinculoPagina>
    val naoVinculados: ObservableList<VinculoPagina>
}