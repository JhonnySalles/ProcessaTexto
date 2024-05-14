package br.com.fenix.processatexto.util

import javafx.fxml.FXMLLoader
import org.slf4j.LoggerFactory


/**
 * Função para realizar a execução de tarefas em sequencia.
 *
 *
 * *NOTE* This interface has been superseded by automatic injection of
 * `location` and `resources` properties into the
 * controller. [FXMLLoader] will now automatically call any suitably
 * annotated no-arg `initialize()` method defined by the controller.
 * It is recommended that the injection approach be used whenever possible.
 *
 * @since TextosJapones 0.9
 *
 * @author Jhonny de Salles Noschang
 */
class ListaExecucoes {

    private val LOGGER = LoggerFactory.getLogger(ListaExecucoes::class.java)

    var list: MutableList<LambdaFunction> = mutableListOf()
    private var abort = false
    var isProcessed = false
        private set

    /**
     * Funcão principal no qual adiciona as execuções na lista e começa a executar a
     * primeira quando a mesma está vazia.
     *
     * ** Aviso ** A função deverá informar o retorno pela **interface
     * ([LambdaFunction])** , no qual irá definir se irá automáticmaente
     * para a próxima execução ou deve aguardar, onde nesse caso será obrigatório a
     * chamada da função [.endProcess] que irá informar o final da execução
     * para dar sequencia na próxima quando existir.
     *
     * *Parâmetro necessário para a execuções em threads, onde o bloco de final é
     * diferente do bloco de execução.
     *
     * @param action Parametro com a função a ser executada, no qual entrará na fila
     * e será executada por ultimo. Caso a fução seja nulo ocorrerá a
     * excessão `IllegalArgumentException`.
     *
     * @author Jhonny de Salles Noschang
     */
    fun addExecucao(action: LambdaFunction) {
        abort = false
        requireNotNull(action)
        list.add(action)
        if (!isProcessed)
            process()
    }

    /**
     * Funcão responsável por chamar o próximo procedimento da fila, será chamada
     * automaticamente quando o resultado da função **[.addExecucao]** for
     * false;
     *
     * @author Jhonny de Salles Noschang
     */
    fun endProcess() {
        isProcessed = !list.isEmpty()
        if (isProcessed)
            process()
    }

    fun abortProcess() {
        abort = true
        list.clear()
    }

    private fun process() {
        if (!list.isEmpty()) {
            try {
                isProcessed = true
                val action = list.removeAt(0)
                val isAwait = action.call(abort)
                if (!isAwait)
                    endProcess()
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
                isProcessed = false
            }
        } else isProcessed = false
    }

    /**
     * Interface responsável por fazer as chamadas em formato de Arrow Functions
     * [ListaExecucoes.endProcess]
     *
     * @return Deverá retornar um **Boolean** no qual quando false irá definir se
     * irá automaticamente para a próxima execução da fila ou quando true
     * deverá aguardar a informação do final da operação, o que será
     * obrigatório a chamada da função **endProcess.**
     */
    interface LambdaFunction {
        fun call(abort: Boolean): Boolean
    }

}