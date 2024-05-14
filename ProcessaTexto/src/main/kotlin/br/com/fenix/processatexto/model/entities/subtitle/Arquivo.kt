package org.jisho.textosJapones.model.entities.subtitle

import java.io.File

data class Arquivo(
    var pasta: String,
    var nome: String,
    var arquivo: File,
    var episodio: Int,
    var isProcessar: Boolean = true
)