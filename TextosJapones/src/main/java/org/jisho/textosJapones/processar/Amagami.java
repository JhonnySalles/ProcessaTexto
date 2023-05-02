package org.jisho.textosJapones.processar;

import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.FullTranslateUsage;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javafx.util.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Amagami {
		
	final private static Boolean EXCLUIR = false;
	final private static Integer LIMIT = 1000;
	final private static String PASTA_ORIGEM = "D:\\Arquivos\\Amagami\\Programa extração\\MTL_Amagami_share2\\machineTranslatedXmlUTF8\\";
	final private static String PASTA_DESTINO = "D:\\Arquivos\\Amagami\\Programa extração\\MTL_Amagami_share2\\machineTranslatedXmlUTF8-pt";

	private static Integer TRANSLATED = 0;
	private static Api CONTA = Api.API_GOOGLE;
	
	
	public static void processa() {
		File pasta = new File(PASTA_ORIGEM);
		
		try {
			for (File item : pasta.listFiles()) {
				System.out.println("Processando item: " + item.getName());
				if (processa(item) && EXCLUIR)
						item.delete();
			}
		} catch(FullTranslateUsage e) {
			System.out.println(e.getMessage());
		}
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
					Node original = null; textContent.getChildNodes().item(0);
					Node translate = null; textContent.getChildNodes().item(1);
					
					for (int x=0; x < items.getLength(); x++) {
						Node item = items.item(x);
						if(item.getNodeType() == Node.ELEMENT_NODE) {
							if (item.getAttributes().getNamedItem("lang").getTextContent().equalsIgnoreCase("eng"))
								translate = item;
							else if (item.getAttributes().getNamedItem("lang").getTextContent().equalsIgnoreCase("jp"))
								original = item;
						}
					}
			
					if (original != null && translate != null) {
						list.add(new Pair<Node, Node>(original, translate));
						words += original.getTextContent().length();
					}

					if (words > LIMIT && list.size() > 0)
						traduzir(list);
				}

			}

			if (list.size() > 0)
				traduzir(list);

			try (FileOutputStream output = new FileOutputStream(PASTA_DESTINO + "\\" + file.getName())) {
				salvarXml(doc, output, new File(PASTA_DESTINO + "\\" + file.getName() + ".xslt"));
				return true;
			}

		} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void salvarXml(Document doc, OutputStream output, File file)
			throws TransformerException, UnsupportedEncodingException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer(new StreamSource(file));

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);

		transformer.transform(source, result);
	}

	private static void traduzir(ArrayList<Pair<Node, Node>> list) throws IOException, FullTranslateUsage {
		System.out.println("Iniciando a tradução da lista.");
		
		for(Pair<Node, Node> item : list)
			item.getValue().setTextContent(traduzir(item.getKey().getTextContent()));
		
		//String frases = "";
		/*for (Pair<Node, Node> item : list)
			frases += item.getKey().getTextContent() + "|";

		frases = traduzir(frases);
		
		int count = 0;
		for(int i = 0; i < frases.length(); i++) {    
            if(frases.charAt(i) == '|')    
                count++;    
        }
		
		if (count != list.size()) {
			System.out.println("Divergência de frases na tradução.");
			for (Pair<Node, Node> item : list)
				item.getValue().setTextContent(traduzir(item.getKey().getTextContent()));
		} else {
			String[] processados = frases.split("|");
			for(int i = 0; i < processados.length; i++)
				list.get(i).getValue().setTextContent(processados[i]);
 		}*/
			
			
		list.clear();
		System.out.println("Concluído.");
		System.out.println("-".repeat(100));
	}
	
	private static String traduzir(String text) throws IOException, FullTranslateUsage {
		try {
			if (TRANSLATED > 3000) {
				switch (CONTA) {
				case CONTA_PRINCIPAL:
					CONTA = Api.CONTA_SECUNDARIA;
					break;
				case CONTA_SECUNDARIA:
					CONTA = Api.CONTA_MIGRACAO_1;
					break;
				case CONTA_MIGRACAO_1:
					CONTA = Api.CONTA_MIGRACAO_2;
					break;
				case CONTA_MIGRACAO_2:
					CONTA = Api.CONTA_MIGRACAO_3;
					break;
				case CONTA_MIGRACAO_3:
					CONTA = Api.CONTA_MIGRACAO_4;
					break;
				case CONTA_MIGRACAO_4:
					throw new FullTranslateUsage("Tradução final no dia, recursos de tradução finalizada.");
				}
				TRANSLATED = 0;
			} else if (TRANSLATED % 50 == 0) {
				try {
					//Pausa a cada 50
					System.out.println("Espera 1s.");
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			TRANSLATED++;
			
			return ScriptGoogle.translate(Language.JAPANESE.getSigla(), Language.PORTUGUESE.getSigla(), text, CONTA);
		} catch(IOException ex) {
			if (ex.getMessage().contains("Server returned HTTP response code: 500 for")) {
				try {
					System.out.println("Resposta 500. " + ex.getMessage());
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return traduzir(text);
			} else
				throw ex;
		}
	}

}
