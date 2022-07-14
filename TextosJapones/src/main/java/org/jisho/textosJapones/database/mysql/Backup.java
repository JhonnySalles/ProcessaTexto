package org.jisho.textosJapones.database.mysql;

import java.io.File;
import java.io.IOException;

import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.components.notification.Notificacoes;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.enums.Notificacao;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class Backup {
	private static String SEPARATOR = File.separator;
	private static String MYSQL_PATH;

	private static void criaPasta(String caminho) {
		File arquivo = new File(caminho);
		if (!arquivo.exists())
			arquivo.mkdir();
	}

	private static void carregaCampos() throws Exception {
		ConexaoMysql.getDadosConexao();
		if (ConexaoMysql.getDataBase().isEmpty())
			throw new Exception("Erro, não existe nenhuma base nos arquivos de configuração.");

		if (ConexaoMysql.getCaminhoMysql().isEmpty())
			throw new Exception("Erro, caminho do mysql não definido no arquivo de configuração.");

		File arquivo = new File(ConexaoMysql.getCaminhoMysql());
		if (!arquivo.exists())
			throw new Exception("Erro, caminho do mysql não encontrado.");

		MYSQL_PATH = ConexaoMysql.getCaminhoMysql() + SEPARATOR + "bin";
	}

	private static String processaExportacao(File caminho) throws IOException {
		criaPasta(caminho + SEPARATOR);
		String commando = MYSQL_PATH + SEPARATOR + "mysqldump.exe";
		long time1, time2, time;
		time1 = System.currentTimeMillis();
		ProcessBuilder pb = new ProcessBuilder(commando, "--user=" + ConexaoMysql.getUser(),
				"--password=" + ConexaoMysql.getPassword(), ConexaoMysql.getDataBase(),
				"--result-file=" + caminho.getPath() + SEPARATOR + ConexaoMysql.getDataBase() + ".sql");

		pb.start();
		time2 = System.currentTimeMillis();
		time = (time2 - time1) / 1000;

		return "Backup realizado com sucesso. Tempo decorrido " + time + "s.\n" + caminho.getPath() + SEPARATOR
				+ "Backup" + SEPARATOR + ConexaoMysql.getDataBase() + ".sql";
	}

	private static String processaImportacao(File arquivo) throws IOException {
		String commando = MYSQL_PATH + SEPARATOR + "mysql.exe";
		long time1, time2, time;
		time1 = System.currentTimeMillis();

		ProcessBuilder pb = new ProcessBuilder(commando, "--user=" + ConexaoMysql.getUser(),
				"--password=" + ConexaoMysql.getPassword(), ConexaoMysql.getDataBase(), "-e",
				" source " + arquivo.getAbsolutePath());

		pb.start();

		time2 = System.currentTimeMillis();
		time = (time2 - time1) / 1000;

		return "Backup importado com sucesso. Tempo decorrido " + time + "s.";
	}

	public static void importarBackup(MenuPrincipalController cnt) {
		FileChooser backup = new FileChooser();
		backup.setInitialDirectory(new File(System.getProperty("user.home")));
		backup.setTitle("Carregar o backup.");
		backup.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo de texto (*.sql)", "*.sql"));
		File arquivo = backup.showOpenDialog(null);

		if (arquivo == null)
			cnt.cancelaBackup();
		else {
			try {
				carregaCampos();
				String resultado = processaImportacao(arquivo);
				cnt.importaConcluido(false);
				Notificacoes.notificacao(Notificacao.SUCESSO, "Backup realizado com sucesso", resultado);
			} catch (IOException e) {
				e.printStackTrace();
				cnt.importaConcluido(true);
				AlertasPopup.ErroModal("Erro ao tentar gerar backup da base", e.getStackTrace().toString());
			} catch (Exception e) {
				e.printStackTrace();
				cnt.importaConcluido(true);
				Notificacoes.notificacao(Notificacao.ERRO, "Erro ao realizar o backup",
						"Arquivo de configução não encontrado ou nome da base não configurada.");
			}
		}
	}

	public static void exportarBackup(MenuPrincipalController cnt) {
		DirectoryChooser backup = new DirectoryChooser();
		backup.setInitialDirectory(new File(System.getProperty("user.home")));
		backup.setTitle("Carregar o backup.");
		File arquivo = backup.showDialog(null);

		if (arquivo == null)
			cnt.cancelaBackup();
		else {
			try {
				carregaCampos();
				String resultado = processaExportacao(arquivo);
				cnt.exportaConcluido(false);
				Notificacoes.notificacao(Notificacao.SUCESSO, "Backup realizado com sucesso", resultado);
			} catch (IOException e) {
				e.printStackTrace();
				cnt.exportaConcluido(true);
				AlertasPopup.ErroModal("Erro ao tentar gerar backup da base", e.getStackTrace().toString());
			} catch (Exception e) {
				e.printStackTrace();
				cnt.exportaConcluido(true);
				Notificacoes.notificacao(Notificacao.ERRO, "Erro ao realizar o backup",
						"Arquivo de configução não encontrado ou nome da base não configurada.");
			}
		}
	}

}
