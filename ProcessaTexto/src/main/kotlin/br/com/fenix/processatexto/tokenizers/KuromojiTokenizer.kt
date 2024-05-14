package br.com.fenix.processatexto.tokenizers

import br.com.fenix.processatexto.controller.FrasesAnkiController
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.service.VocabularioJaponesServices
import com.atilika.kuromoji.ipadic.Token
import com.atilika.kuromoji.ipadic.Tokenizer
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors


class KuromojiTokenizer {

    private val LOGGER = LoggerFactory.getLogger(KuromojiTokenizer::class.java)

    private var vocabServ: VocabularioJaponesServices? = null
    private val repetido: MutableSet<String> = mutableSetOf()
    private val vocabNovo: MutableList<Vocabulario> = mutableListOf()

    // UNICODE RANGE : DESCRIPTION
    //
    // 3000-303F : punctuation
    // 3040-309F : hiragana
    // 30A0-30FF : katakana
    // FF00-FFEF : Full-width roman + half-width katakana
    // 4E00-9FAF : Common and uncommon kanji
    //
    // Non-Japanese punctuation/formatting characters commonly used in Japanese text
    // 2605-2606 : Stars
    // 2190-2195 : Arrows
    // u203B : Weird asterisk thing
    private val tokenizer: Tokenizer = Tokenizer()
    private val pattern = ".*[\u4E00-\u9FAF].*".toRegex()
    private var i = 0
    fun processaTexto(cnt: FrasesAnkiController) {
        setVocabularioServices(VocabularioJaponesServices())
        val texto: List<String> = cnt.textoOrigem.split("\n")
        var processado = ""
        vocabNovo.clear()
        repetido.clear()
        cnt.setPalavra(texto[0])
        try {
            for (txt in texto) {
                if (txt !== texto[0]) {
                    if (txt.isNotEmpty()) {
                        val tokens: List<Token> = tokenizer.tokenize(txt)
                        i = 0
                        while (i < tokens.size) {
                            println((tokens[i].surface + "\t" + tokens[i].baseForm) + "\t" + tokens[i].conjugationForm)
                            if (tokens[i].surface.matches(pattern)
                                && !tokens[i].surface.equals(texto[0], ignoreCase = false)
                                && !repetido.contains(tokens[i].baseForm)
                            ) {

                                // Faz a validação se o próximo tokem também é um kanji, se for junta para ser uma palavra só.
                                if (i + 1 < tokens.size && tokens[i + 1].surface.matches(pattern)) {
                                    if (!texto[0].equals(
                                            tokens[i].surface + tokens[i + 1].surface,
                                            ignoreCase = true
                                        ) && !repetido.contains(tokens[i].baseForm + tokens[i + 1].baseForm)
                                    ) {
                                        var palavra: Optional<Vocabulario> =
                                            vocabServ!!.select(tokens[i].surface + tokens[i + 1].surface, tokens[i].baseForm + tokens[i + 1].baseForm)
                                        if (palavra.isPresent) {
                                            processado += ((tokens[i].baseForm + tokens[i + 1].surface) + " " + palavra.get().portugues) + " "
                                            if (palavra.get().formaBasica.isEmpty() || palavra.get().leitura.isEmpty()) {
                                                palavra.get().formaBasica = tokens[i].baseForm + tokens[i + 1].baseForm
                                                palavra.get().leitura = tokens[i].reading + tokens[i + 1].reading
                                                vocabServ!!.update(palavra.get())
                                            }
                                            repetido.add(tokens[i].baseForm + tokens[i + 1].baseForm)
                                        } else {
                                            // Caso não encontre, irá verificar eles separadamente, no caso um laço duas vezes.
                                            for (x in 0..1) {
                                                palavra = vocabServ!!.select(tokens[i + x].surface, tokens[i + x].baseForm)
                                                if (palavra.isPresent) {
                                                    processado += (tokens[i + x].baseForm + " " + palavra.get().portugues) + " "
                                                    if (palavra.get().formaBasica.isEmpty() || palavra.get().leitura.isEmpty()) {
                                                        palavra.get().formaBasica = tokens[i + x].baseForm
                                                        palavra.get().leitura = tokens[i + x].reading
                                                        vocabServ!!.update(palavra.get())
                                                    }
                                                } else {
                                                    val existe: List<Vocabulario> = vocabNovo.stream()
                                                        .filter { p -> p.vocabulario.equals(tokens[i].surface, ignoreCase = true) }
                                                        .collect(Collectors.toList())
                                                    processado += tokens[i + x].baseForm + " ** "
                                                    if (existe.isEmpty()) {
                                                        // naoEncontrado += tokens.get(i + x).surface + " \n";
                                                        vocabNovo.add(Vocabulario(tokens[i + x].surface, tokens[i + x].baseForm, tokens[i + x].reading, ""))
                                                    }
                                                }
                                                repetido.add(tokens[i + x].baseForm)
                                            }
                                        }
                                    }
                                    i++
                                } else {
                                    val palavra: Optional<Vocabulario> = vocabServ!!.select(tokens[i].surface, tokens[i].baseForm)
                                    if (palavra.isPresent) {
                                        processado += (tokens[i].baseForm + " " + palavra.get().portugues) + " "
                                        if (palavra.get().formaBasica.isEmpty() || palavra.get().leitura.isEmpty()) {
                                            palavra.get().formaBasica = tokens[i].baseForm
                                            palavra.get().leitura = tokens[i].reading
                                            vocabServ!!.update(palavra.get())
                                        }
                                    } else {
                                        val existe: List<Vocabulario> = vocabNovo.stream()
                                            .filter { p -> p.vocabulario.equals(tokens[i].surface, ignoreCase = true) }
                                            .collect(Collectors.toList())
                                        processado += tokens[i].baseForm + " ** "
                                        if (existe.isEmpty()) {
                                            // naoEncontrado += tokens.get(i).surface + " \n";
                                            vocabNovo.add(Vocabulario(tokens[i].surface, tokens[i].baseForm, tokens[i].reading, ""))
                                        }
                                    }
                                    repetido.add(tokens[i].baseForm)
                                }
                            }
                            i++
                        }
                        processado += "\n\n\n"
                    }
                }
            }
        } catch (e: SQLException) {
            println("Erro ao processar. Erro ao carregar informações do banco.")
            LOGGER.error(e.message, e)
        }
        cnt.setVocabulario(vocabNovo)
        cnt.setTextoDestino(processado)
    }

    private fun setVocabularioServices(vocabServ: VocabularioJaponesServices) {
        this.vocabServ = vocabServ
    }

}