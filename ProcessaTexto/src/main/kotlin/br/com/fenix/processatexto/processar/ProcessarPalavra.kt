package br.com.fenix.processatexto.processar

import br.com.fenix.processatexto.model.enums.Dicionario
import br.com.fenix.processatexto.model.enums.Modo
import br.com.fenix.processatexto.tokenizers.SudachiTokenizer
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException


class ProcessarPalavra {

    private val LOGGER: Logger = LoggerFactory.getLogger(ProcessarPalavra::class.java)

    private lateinit var tokenizer: Tokenizer
    private lateinit var mode: SplitMode

    fun processarDesmembrar(palavra: String, dicionario: Dicionario, modo: Modo): MutableList<String> {
        try {
            DictionaryFactory().create(
                "",
                SudachiTokenizer.readAll(FileInputStream(SudachiTokenizer.getPathSettings(dicionario)))
            ).use { dict ->
                tokenizer = dict.create()
                mode = SudachiTokenizer.getModo(modo)
                return processar(palavra)
            }
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            return mutableListOf()
        }
    }

    private val pattern = ".*[\u4E00-\u9FAF].*".toRegex()
    private fun processar(palavra: String): MutableList<String> {
        val resultado: MutableList<String> = mutableListOf()
        for (m in tokenizer.tokenize(mode, palavra))
            if (m.surface().matches(pattern))
                resultado.add(m.surface())
        return resultado
    }
}