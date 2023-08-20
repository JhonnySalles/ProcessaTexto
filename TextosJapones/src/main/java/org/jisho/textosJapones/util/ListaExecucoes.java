package org.jisho.textosJapones.util;

import javafx.fxml.FXMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Função para realizar a execução de tarefas em sequencia.
 * <p>
 * <em>NOTE</em> This interface has been superseded by automatic injection of
 * <code>location</code> and <code>resources</code> properties into the
 * controller. {@link FXMLLoader} will now automatically call any suitably
 * annotated no-arg <code>initialize()</code> method defined by the controller.
 * It is recommended that the injection approach be used whenever possible.
 * 
 * @since TextosJapones 0.9
 * 
 * @author Jhonny de Salles Noschang
 */
public class ListaExecucoes {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListaExecucoes.class);

	List<LambdaFunction> list = new ArrayList<LambdaFunction>();

	private Boolean abort = false;
	private Boolean isProcessed = false;

	/**
	 * <p>
	 * Funcão principal no qual adiciona as execuções na lista e começa a executar a
	 * primeira quando a mesma está vazia.
	 * </p>
	 * 
	 * <p>
	 * <b> Aviso </b> A função deverá informar o retorno pela <b>interface
	 * ({@link LambdaFunction})</b> , no qual irá definir se irá automáticmaente
	 * para a próxima execução ou deve aguardar, onde nesse caso será obrigatório a
	 * chamada da função {@link #endProcess} que irá informar o final da execução
	 * para dar sequencia na próxima quando existir.
	 * </p>
	 * <p>
	 * <i>Parâmetro necessário para a execuções em threads, onde o bloco de final é
	 * diferente do bloco de execução. </i>
	 * </p>
	 * 
	 * @param action Parametro com a função a ser executada, no qual entrará na fila
	 *               e será executada por ultimo. Caso a fução seja nulo ocorrerá a
	 *               excessão {@code IllegalArgumentException}.
	 * 
	 * @author Jhonny de Salles Noschang
	 */
	public void addExecucao(LambdaFunction action) {
		abort = false;
		if (action == null)
			throw new IllegalArgumentException();

		this.list.add(action);

		if (!isProcessed)
			process();
	}

	public Boolean isProcessed() {
		return isProcessed;
	}

	/**
	 * <p>
	 * Funcão responsável por chamar o próximo procedimento da fila, será chamada
	 * automaticamente quando o resultado da função <b>{@link #addExecucao}</b> for
	 * false;
	 * </p>
	 * 
	 * @author Jhonny de Salles Noschang
	 */
	public void endProcess() {
		isProcessed = !list.isEmpty();
		if (isProcessed)
			process();
	}

	public void abortProcess() {
		abort = true;
		list.clear();
	}

	private void process() {
		if (!list.isEmpty()) {
			try {
				isProcessed = true;
				LambdaFunction action = list.remove(0);
				Boolean isAwait = action.call(abort);
				
				if (!isAwait)
					endProcess();
			} catch (Exception e) {
				
				LOGGER.error(e.getMessage(), e);
				isProcessed = false;
			}
		} else
			isProcessed = false;
	}

	/**
	 * <p>
	 * Interface responsável por fazer as chamadas em formato de Arrow Functions
	 * </p>
	 * 
	 * <p>
	 * {@link ListaExecucoes#endProcess}
	 * </p>
	 * 
	 * @return Deverá retornar um <b>Boolean</b> no qual quando false irá definir se
	 *         irá automaticamente para a próxima execução da fila ou quando true
	 *         deverá aguardar a informação do final da operação, o que será
	 *         obrigatório a chamada da função <b>endProcess.</b>
	 */
	public interface LambdaFunction {
		Boolean call(Boolean abort);
	}

}
