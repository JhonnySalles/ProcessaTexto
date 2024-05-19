package br.com.fenix.processatexto.processar.kanjiStatics

import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.model.entities.processatexto.japones.Estatistica
import br.com.fenix.processatexto.model.enums.Notificacao
import br.com.fenix.processatexto.service.EstatisticaServices
import javafx.stage.FileChooser
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.*
import java.sql.SQLException


object ImportaEstatistica {

    private val LOGGER = LoggerFactory.getLogger(ImportaEstatistica::class.java)

    private const val PIPE = "\\|\\|"
    private const val TITULO_HTML = "<!DOCTYPE html> <html lang=\"jp\"> <body>"
    private const val RODAPE_HTML = "</body> </html>"

    private val estatisticas: MutableList<Estatistica> = mutableListOf()
    private var service: EstatisticaServices? = null

    fun importa() {
        val escolha = FileChooser()
        escolha.setInitialDirectory(File(System.getProperty("user.home")))
        escolha.setTitle("Carregar arquivo estatistica.")
        escolha.getExtensionFilters()
            .add(FileChooser.ExtensionFilter("Arquivo de texto (*.txt, *.csv)", "*.txt", "*.csv"))
        val arquivo = escolha.showOpenDialog(null)
        if (arquivo.exists())
            processa(arquivo)
    }

    private fun processa(arquivo: File) {
        try {
            var linha = ""
            var kanji = ""
            var splt: List<String>
            val br = BufferedReader(FileReader(arquivo.absolutePath))
            service = EstatisticaServices()
            while (br.ready()) {
                linha = br.readLine()
                if (!linha.isEmpty()) {
                    splt = linha.split(PIPE)
                    kanji = splt[0]
                    val doc = Jsoup.parse(TITULO_HTML + splt[1] + RODAPE_HTML)
                    val linhaTabela = doc.getElementsByTag("tr")

                    // Linha
                    val max: Int = linhaTabela.size
                    var corSequencial = 0
                    var corSelecao = ""
                    for (i in 0 until max) {
                        val colunaTabela = linhaTabela.get(i).getElementsByTag("td")
                        val tipo = colunaTabela.get(0)
                        if (!tipo.text().equals("Total", ignoreCase = true)) {

                            val leitura = colunaTabela[1]
                            val quantidade = colunaTabela[2]
                            val percentual = colunaTabela[3]
                            val media = colunaTabela[4]
                            val pecentMedia = colunaTabela[5]

                            if (!corSelecao.equals(tipo.text(), ignoreCase = true)) {
                                corSelecao = tipo.text()
                                corSequencial = 0
                            }
                            corSequencial++
                            estatisticas.add(
                                Estatistica(
                                    null, kanji, tipo.text(), leitura.text(), quantidade.text().toDouble(),
                                    percentual.text().replace("%", "").toFloat(), media.text().toDouble(),
                                    pecentMedia.text().replace("%", "").toFloat(), corSequencial
                                )
                            )
                        }
                    }
                }
            }
            service!!.insert(estatisticas)
            Notificacoes.notificacao(Notificacao.SUCESSO, "Concluido", "Importação concluida com sucesso.")
            br.close()
        } catch (e: FileNotFoundException) {
            LOGGER.error(e.message, e)
            Notificacoes.notificacao(Notificacao.ERRO, "Arquivo não encontrado", "Não foi possível carregar o arquivo para importação.")
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            Notificacoes.notificacao(Notificacao.ERRO, "Erro ao procesar o arquivo", e.message!!)
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar os dados", e.message!!)
        }
    }
}