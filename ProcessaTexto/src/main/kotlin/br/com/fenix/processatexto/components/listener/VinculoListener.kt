package br.com.fenix.processatexto.components.listener

import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Pagina
import javafx.scene.Node

interface VinculoListener {
    fun onDuploClique(root: Node, vinculo: VinculoPagina, origem: Pagina): Boolean
    fun onDrop(origem: Pagina, vinculoOrigem: VinculoPagina, destino: Pagina, vinculoDestino: VinculoPagina)
    fun onDragStart()
    fun onDragEnd()
}