package org.jisho.textosJapones.processar;

import javafx.concurrent.Task;
import javafx.util.Pair;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.FullTranslateUsage;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Amagami {

    private static final Logger LOGGER = LoggerFactory.getLogger(Amagami.class);

    final private static Boolean EXCLUIR = true;
    final private static Integer LIMIT = 1000;
    final private static String PASTA_ORIGEM = "D:\\Arquivos\\Amagami\\Programa extração\\MTL_Amagami_share2\\machineTranslatedXmlUTF8\\";
    final private static String PASTA_DESTINO = "D:\\Arquivos\\Amagami\\Programa extração\\MTL_Amagami_share2\\machineTranslatedXmlUTF8-pt";

    private static final List<Conta> CONTAS = new ArrayList<Conta>(Arrays.asList(new Conta(Api.API_GOOGLE, 0),
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
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private static void salvarXml(Document doc, OutputStream output, String file)
            throws TransformerException, UnsupportedEncodingException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(file));
        transformer.transform(source, result);

        StreamResult consoleResult = new StreamResult(output);
        transformer.transform(source, consoleResult);
    }


    private static final Map<String, String> TRADUZIDO = new HashMap<String, String>();

    private static void traduzir(ArrayList<Pair<Node, Node>> list) throws IOException, FullTranslateUsage {
        LAST_OPERATION = new Date();
        for (Pair<Node, Node> item : list) {
            if (TRADUZIDO.containsKey(item.getKey().getTextContent()))
                item.getValue().setTextContent(TRADUZIDO.get(item.getKey().getTextContent()));
            else {
                String frase = traduzir(item.getKey().getTextContent());
                item.getValue().setTextContent(frase);
                TRADUZIDO.put(item.getKey().getTextContent(), frase);
            }
        }

        list.clear();
        System.out.println("Concluído.");
        System.out.println("-".repeat(100));
    }

    private static Conta proximaConta() throws FullTranslateUsage {
        Conta novo = null;

        int inicio = CONTAS.indexOf(CONTA);
        for (int i = inicio; i < CONTAS.size(); i++) {
            Conta item = CONTAS.get(i);
            if (!item.getKey().equals(CONTA.getKey()) && item.getValue() < 3000) {
                novo = item;
                break;
            }
        }

        if (novo == null)
            for (int i = 0; i < inicio; i++) {
                Conta item = CONTAS.get(i);
                if (!item.getKey().equals(CONTA.getKey()) && item.getValue() < 3000) {
                    novo = item;
                    break;
                }
            }

        if (novo == null)
            for (int i = 0; i < CONTAS.size(); i++) {
                Conta item = CONTAS.get(i);
                if (item.getValue() < 3000) {
                    novo = item;
                    break;
                }
            }

        if (novo == null)
            throw new FullTranslateUsage("Tradução final no dia, recursos de tradução finalizada.");

        CONTA = novo;
        System.out.println("Nova conta selecionada: " + CONTA.getKey().toString() + " - Consultas: "
                + CONTA.getValue());

        return novo;
    }

    private static String traduzir(String text) throws IOException, FullTranslateUsage {
        try {
            if (CONTA.getValue() > 3000) {
                proximaConta();
            } else if (CONTA.getValue() > 0 && CONTA.getValue() % 50 == 0) {
                try {
                    // Pausa a cada 50
                    System.out.println("Espera 1s.");
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("Continuando.");
                } catch (InterruptedException e) {
                    
                    LOGGER.error(e.getMessage(), e);
                }
            }

            CONTA.setValue(CONTA.getValue() + 1);
            System.out.println("Traduzindo: " + text);
            String traducao = ScriptGoogle.translate(Language.JAPANESE.getSigla(), Language.PORTUGUESE.getSigla(), text,
                    CONTA.getKey());

            if (traducao.toLowerCase().contains("exception: serviço chamado muitas vezes no mesmo dia: translate.")) {
                System.out.println("Identificado limite máximo excedido no dia de hoje. Ignorando a api.");
                CONTA.setValue(5000);
                return traduzir(text);
            } else
                return traducao;
        } catch (IOException ex) {
            if (ex.getMessage().contains("Server returned HTTP response code: 500 for")) {
                try {
                    System.out.println("Resposta 500. " + ex.getMessage());
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    
                    LOGGER.error(e.getMessage(), e);
                }
                proximaConta();
                return traduzir(text);
            } else
                throw ex;
        }
    }

    public static class Conta {
        private final Api key;
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
