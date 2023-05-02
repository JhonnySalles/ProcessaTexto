package org.jisho.textosJapones.processar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.FullTranslateUsage;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.concurrent.Task;
import javafx.util.Pair;

public class Amagami {

	final private static Boolean EXCLUIR = true;
	final private static Integer LIMIT = 1000;
	final private static String PASTA_ORIGEM = "D:\\Arquivos\\Amagami\\Programa extração\\MTL_Amagami_share2\\machineTranslatedXmlUTF8\\";
	final private static String PASTA_DESTINO = "D:\\Arquivos\\Amagami\\Programa extração\\MTL_Amagami_share2\\machineTranslatedXmlUTF8-pt";

	private static List<Conta> CONTAS = new ArrayList<Conta>(Arrays.asList(new Conta(Api.API_GOOGLE, 500),
			new Conta(Api.CONTA_SECUNDARIA, 0), new Conta(Api.CONTA_MIGRACAO_1, 0), new Conta(Api.CONTA_MIGRACAO_2, 0),
			new Conta(Api.CONTA_MIGRACAO_3, 0), new Conta(Api.CONTA_MIGRACAO_4, 0)));
	private static Conta CONTA = CONTAS.get(0);

	private static Date LAST_OPERATION = new Date();
	private static Thread threadTraduzir;
	private static Thread threadVerifica;

	private static void createTask() {
		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<String> traduzir = new Task<String>() {

			@Override
			protected String call() throws Exception {
				File pasta = new File(PASTA_ORIGEM);
				LAST_OPERATION = new Date();
				try {

					for (File item : pasta.listFiles()) {
						System.out.println("Processando item: " + item.getName());
						if (processa(item) && EXCLUIR)
							item.delete();
					}
				} catch (FullTranslateUsage e) {
					System.out.println(e.getMessage());
					threadVerifica.stop();
				}
				return "";
			}

			@Override
			protected void succeeded() {
			}
		};

		threadTraduzir = new Thread(traduzir);
		threadTraduzir.start();
	}

	public static void processa() {
		createTask();
		Task<String> verifica = new Task<String>() {

			@Override
			protected String call() throws Exception {
				while (1 > 0) {
					TimeUnit.SECONDS.sleep(5);

					File pasta = new File(PASTA_ORIGEM);
					if (pasta.listFiles().length <= 0)
						break;

					Calendar agora = Calendar.getInstance();
					agora.add(Calendar.MINUTE, -5);
					if (LAST_OPERATION.before(agora.getTime())) {
						System.out.println("Reiniciando thread de tradução.");
						threadTraduzir.stop();
						TimeUnit.SECONDS.sleep(10);
						createTask();
					}
				}
				return null;
			}

			@Override
			protected void succeeded() {
			}
		};

		threadVerifica = new Thread(verifica);
		threadVerifica.start();

	}

	private static Boolean processa(File file) throws FullTranslateUsage {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		ArrayList<Pair<Node, Node>> list = new ArrayList<>();
		Integer words = 0;

		try (InputStream is = new FileInputStream(file)) {

			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(is);

			NodeList texts = doc.getElementsByTagName("text");

			if (texts == null)
				return false;

			for (int i = 0; i < texts.getLength(); i++) {
				Node textContent = texts.item(i);
				if (textContent.getNodeType() == Node.ELEMENT_NODE && textContent.getChildNodes().getLength() > 1) {
					NodeList items = textContent.getChildNodes();
					Node original = null;
					textContent.getChildNodes().item(0);
					Node translate = null;
					textContent.getChildNodes().item(1);

					for (int x = 0; x < items.getLength(); x++) {
						Node item = items.item(x);
						if (item.getNodeType() == Node.ELEMENT_NODE) {
							if (item.getAttributes().getNamedItem("lang").getTextContent().equalsIgnoreCase("eng"))
								translate = item;
							else if (item.getAttributes().getNamedItem("lang").getTextContent().equalsIgnoreCase("jp"))
								original = item;
						}
					}

					if (original != null && original.getTextContent() != null && translate != null
							&& translate.getTextContent() != null) {
						if (!original.getTextContent().trim().isEmpty() && !translate.getTextContent().trim().isEmpty()
								&& !translate.getTextContent().equals(":") && !translate.getTextContent().equals("!")) {
							list.add(new Pair<Node, Node>(original, translate));
							words += original.getTextContent().length();
						}
					}

					if (words > LIMIT && list.size() > 0) {
						System.out.println("Iniciando a tradução da lista. Textos: " + i + "/" + texts.getLength());
						traduzir(list);
						words = 0;
					}
				}

			}

			if (list.size() > 0) {
				System.out.println("Tradução final.");
				traduzir(list);
			}

			try (FileOutputStream output = new FileOutputStream(PASTA_DESTINO + "\\" + file.getName())) {
				salvarXml(doc, output, file.getPath());
				return true;
			}

		} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void salvarXml(Document doc, OutputStream output, String file)
			throws TransformerException, UnsupportedEncodingException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(file));
		transformer.transform(source, result);

		StreamResult consoleResult = new StreamResult(output);
		transformer.transform(source, consoleResult);
	}

	private static void traduzir(ArrayList<Pair<Node, Node>> list) throws IOException, FullTranslateUsage {
		LAST_OPERATION = new Date();
		for (Pair<Node, Node> item : list)
			item.getValue().setTextContent(traduzir(item.getKey().getTextContent()));

		// String frases = "";
		/*
		 * for (Pair<Node, Node> item : list) frases += item.getKey().getTextContent() +
		 * "|";
		 * 
		 * frases = traduzir(frases);
		 * 
		 * int count = 0; for(int i = 0; i < frases.length(); i++) { if(frases.charAt(i)
		 * == '|') count++; }
		 * 
		 * if (count != list.size()) {
		 * System.out.println("Divergência de frases na tradução."); for (Pair<Node,
		 * Node> item : list)
		 * item.getValue().setTextContent(traduzir(item.getKey().getTextContent())); }
		 * else { String[] processados = frases.split("|"); for(int i = 0; i <
		 * processados.length; i++)
		 * list.get(i).getValue().setTextContent(processados[i]); }
		 */

		list.clear();
		System.out.println("Concluído.");
		System.out.println("-".repeat(100));
	}

	private static String traduzir(String text) throws IOException, FullTranslateUsage {
		try {
			if (CONTA.getValue() > 3000) {
				Conta novo = null;

				for (Conta item : CONTAS)
					if (item.getValue() < 3000) {
						novo = item;
						break;
					}

				if (novo == null) {
					throw new FullTranslateUsage("Tradução final no dia, recursos de tradução finalizada.");
				}

				CONTA = novo;
				System.out.println(
						"Nova conta selecionada: " + CONTA.getKey().toString() + " - Consultas: " + CONTA.getValue());
			} else if (CONTA.getValue() > 0 && CONTA.getValue() % 50 == 0) {
				try {
					// Pausa a cada 50
					System.out.println("Espera 1s.");
					TimeUnit.SECONDS.sleep(1);
					System.out.println("Continuando.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			CONTA.setValue(CONTA.getValue() + 1);
			System.out.println("Traduzindo: " + text);
			return ScriptGoogle.translate(Language.JAPANESE.getSigla(), Language.PORTUGUESE.getSigla(), text,
					CONTA.getKey());
		} catch (IOException ex) {
			if (ex.getMessage().contains("Server returned HTTP response code: 500 for")) {
				try {
					System.out.println("Resposta 500. " + ex.getMessage());
					TimeUnit.SECONDS.sleep(1);

					Boolean achou = false;
					int inicio = CONTAS.indexOf(CONTA);
					for (int i = inicio; i < CONTAS.size(); i++) {
						Conta item = CONTAS.get(i);
						if (!item.getKey().equals(CONTA.getKey()) && item.getValue() < 3000) {
							achou = true;
							CONTA = item;
							System.out.println("Nova conta selecionada: " + CONTA.getKey().toString() + " - Consultas: "
									+ CONTA.getValue());
							break;
						}
					}

					if (!achou)
						for (int i = 0; i < inicio; i++) {
							Conta item = CONTAS.get(i);
							if (!item.getKey().equals(CONTA.getKey()) && item.getValue() < 3000) {
								achou = true;
								CONTA = item;
								System.out.println("Nova conta selecionada: " + CONTA.getKey().toString()
										+ " - Consultas: " + CONTA.getValue());
								break;
							}
						}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return traduzir(text);
			} else
				throw ex;
		}
	}

	public static class Conta {
		private Api key;
		private Integer value;

		public Api getKey() {
			return key;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public Conta(Api key, Integer value) {
			this.key = key;
			this.value = value;
		}
	}

}
