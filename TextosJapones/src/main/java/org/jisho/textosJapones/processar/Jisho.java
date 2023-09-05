package org.jisho.textosJapones.processar;

import com.google.gson.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Jisho {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jisho.class);

    static String japanese = "";

    public static String processa(String kanji) {
        String retorno = "";
        Jisho jisho = new Jisho();
        try {
            jisho.search(kanji);

            for (int i = 0; i < jisho.getNumEntries(); i++) {
                JSONObject json = new JSONObject(jisho.getEntry(i).toString());
                try {
                    String slug = json.get("slug").toString();

                    JSONArray japones = (JSONArray) json.get("japanese");

                    japanese = "";
                    japones.forEach(item -> {
                        JSONObject obj = (JSONObject) item;
                        String word = obj.get("word").toString();

                        if (word.equalsIgnoreCase(kanji)) {
                            japanese = word;
                            return;
                        }
                    });

                    if (slug.equalsIgnoreCase(kanji) || japanese.equalsIgnoreCase(kanji)) {
                        for (int x = 0; x < jisho.getSensesSize(i); x++)
                            for (int y = 0; y < jisho.getEnglishDefinitionsSize(i, x); y++)
                                retorno += jisho.getEnglishDefinitions(i, x, y) + ", ";

                        retorno = retorno.substring(0, retorno.lastIndexOf(", ")) + ".";
                        break;
                    }
                } catch (JSONException js) {
                    js.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return retorno;
        }
        return retorno;
    }

    /**
     * a variable to hold the JSON data
     */
    private String jsonString;

    /**
     * the desired word to search. Refer to Jisho.org regarding search methods
     */
    @SuppressWarnings("unused")
    private String searchWord;

    /**
     * HTTP response status code
     */
    private int responseCode;

    /**
     * A nested class instance which will store the JSON data into individual
     * entries
     */
    private JishoParseData parse;

    /**
     * A GSON instance to parse JSON
     */
    private Gson gson;

    /**
     * A constructor for a Jisho object which performs all necessary operations
     */
    public Jisho() {
        this.parse = null;
    }

    /**
     * Perform a search of a given word via Jisho.org. Refer to Jisho.org regarding
     * search methods
     *
     * @param searchWord the desired search word. Refer to Jisho.org regarding
     *                   search methods
     * @throws IOException
     */
    public void search(String searchWord) throws Exception {

        // variable for storing the url
        URL url;

        // storing search word, initializing jsonString
        this.searchWord = searchWord;
        this.jsonString = "";

        // Encode query string as searches with japanese characters are problematic
        String word = URLEncoder.encode(searchWord, StandardCharsets.UTF_8.toString());
        url = new URL("https://jisho.org/api/v1/search/words?keyword=" + word);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // create the connecion
        conn.setRequestMethod("GET");
        conn.connect();
        this.responseCode = conn.getResponseCode();

        // check that the connection was successful, throw error otherwise
        if (this.responseCode != 200)
            throw new Exception("HttpResponseCode: " + responseCode + '\n' + "url:" + url);

        // create a scanner, prevent it from removing whitespace from our JSON data
        Scanner input = new Scanner(url.openStream());
        input.useDelimiter(System.getProperty("line.separator")); // Prevent scanner from removing whitespace

        // read our JSON string into our jsonString variable
        while (input.hasNext()) {
            jsonString += input.next();
        }

        // close the input
        input.close();

        // Creating custom deserializer to keep each entry serialized and stored in an
        // array until it is needed
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(JishoParseData.class, deserializer);
        gson = builder.create();

        // initial parsing of the JSON string, separating into data and meta fields
        this.parse = gson.fromJson(this.jsonString, JishoParseData.class);
    }

    // An initial deserializer which separates the data and meta entries
    private final JsonDeserializer<JishoParseData> deserializer = new JsonDeserializer<JishoParseData>() {

        @Override
        public JishoParseData deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                          JsonDeserializationContext context) throws JsonParseException {

            JsonObject jsonObject = json.getAsJsonObject();

            return new JishoParseData(

                    // preserving our data and meta fields and JSON
                    jsonObject.get("data").getAsJsonArray(), jsonObject.get("meta").getAsJsonObject()

            );

        }
    };

    /**
     * Retrieve JSON data as a string
     *
     * @return JSON data as a string
     */
    public String getJsonString() {
        return this.jsonString;
    }

    /**
     * Retrieve HTTP response status code
     *
     * @return HTTP response status code as a string
     */
    public String getStatus() {
        String status = parse.meta.toString();
        return status.substring(10, status.length() - 1);

    }

    /**
     * The number of entries in the "data" list
     *
     * @return an integer which represents the number of entries in the "data" list
     */
    public int getNumEntries() {
        return parse.data.size();
    }

    // Potentially have each entry
    private Object getEntry(int entryNum) {
        return parse.data.get(entryNum);
    }

    /**
     * Retrieve the "slug" from a desired "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "slug"
     *                 from
     * @return A string representing the "slug" entry
     */
    public String getSlug(int entryNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.slug;
    }

    /**
     * Retrieve the value of the "is_common" field in a give "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the
     *                 "is_common" from
     * @return A boolean representing the "is_common" field
     */
    public boolean getIsCommon(int entryNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.is_common;
    }

    /**
     * Retrieve an integer representing the size of the "tags" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "tags"
     *                 size from
     * @return An integer representing the size of the "tags" field
     */
    public int getTagsSize(int entryNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.tags.size();
    }

    /**
     * Retrieve a given tag from the "tags" under a given "data" entry
     *
     * @param entryNum The desired "data" entry
     * @param tagNum   The desired "tags" entry
     * @return The string representing the "tags" entry
     */
    public String getTags(int entryNum, int tagNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.tags.get(tagNum);
    }

    /**
     * Retrieve an integer representing the size of the "jlpt" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "tags"
     *                 size from
     * @return An integer representing the size of the "jlpt" field
     */
    public int getJlptSize(int entryNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.jlpt.size();
    }

    /**
     * Retrieve a "jlpt" entry from a given "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "jlpt"
     *                 from
     * @param jlptNum  The desired "jlpt" entry
     * @return A string representing the "jlpt" entry
     */
    public String getJlpt(int entryNum, int jlptNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.jlpt.get(jlptNum);
    }

    /**
     * Retrieve an integer representing the size of the "japanese" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "japanese"
     *                 size from
     * @return An integer representing the size of the "japanese" field
     */
    public int getJapaneseSize(int entryNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.japanese.size();
    }

    private Object getJapanese(int entryNum, int japaneseNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.japanese.get(japaneseNum);
    }

    /**
     * Retrieve a "word" entry from a given "japanese" entry under a given "data"
     * entry
     *
     * @param entryNum    The desired "data" entry
     * @param japaneseNum The desired "jlpt" entry
     * @return A string representing the "word" entry
     */
    public String getJapaneseWord(int entryNum, int japaneseNum) {
        JishoParseJapanese tempParse = gson.fromJson(getJapanese(entryNum, japaneseNum).toString(),
                JishoParseJapanese.class);
        return tempParse.word;
    }

    /**
     * Retrieve a "reading" entry from a given "japanese" entry under a given "data"
     * entry
     *
     * @param entryNum    The desired "data" entry
     * @param japaneseNum The desired "jlpt" entry
     * @return A string representing the "reading" entry
     */
    public String getJapaneseReading(int entryNum, int japaneseNum) {
        JishoParseJapanese tempParse = gson.fromJson(getJapanese(entryNum, japaneseNum).toString(),
                JishoParseJapanese.class);
        return tempParse.reading;
    }

    /**
     * Retrieve an integer representing the size of the "senses" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "senses"
     *                 size from
     * @return An integer representing the size of the "senses" field
     */
    public int getSensesSize(int entryNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.senses.size();
    }

    private Object getSenses(int entryNum, int sensesNum) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.senses.get(sensesNum);
    }

    /**
     * Retrieve an integer representing the size of the "english_definitions" field
     * in a given "senses" entry under a given "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return An integer representing the size of the "english_definitions" field
     */
    public int getEnglishDefinitionsSize(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.english_definitions.size();
    }

    /**
     * Retrieve a "english_definitions" entry from a given "senses" entry under a
     * given "data" entry
     *
     * @param entryNum      The desired "data" entry
     * @param sensesNum     The desired "senses" entry
     * @param englishDefNum The desired "english_definitions" entry
     * @return A string representing the "english_definitions" entry
     */
    public String getEnglishDefinitions(int entryNum, int sensesNum, int englishDefNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.english_definitions.get(englishDefNum);
    }

    /**
     * Retrieve an integer representing the size of the "parts_of_speech" field in a
     * given "senses" entry under a given "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return An integer representing the size of the "parts_of_speech" field
     */
    public int getPartsOfSpeechSize(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.parts_of_speech.size();
    }

    /**
     * Retrieve a "parts_of_speech" entry from a given "senses" entry under a given
     * "data" entry
     *
     * @param entryNum         The desired "data" entry
     * @param sensesNum        The desired "senses" entry
     * @param partsOfSpeechNum The desired "parts_of_speech" entry
     * @return A string representing the "parts_of_speech" entry
     */
    public String getPartsOfSpeech(int entryNum, int sensesNum, int partsOfSpeechNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.parts_of_speech.get(partsOfSpeechNum);
    }

    /**
     * Retrieve an integer representing the size of the "links" field in a given
     * "senses" entry under a given "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return An integer representing the size of the "links" field
     */
    public int getLinksSize(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.links.size();
    }

    /**
     * Retrieve a "tags" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "tags" entry
     */
    public String getTagsSenses(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.tags.toString();
    }

    /**
     * Retrieve a "restrictions" entry from a given "senses" entry under a given
     * "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "restrictions" entry
     */
    public String getRestrictions(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.restrictions.toString();
    }

    /**
     * Retrieve a "see_also" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "see_also" entry
     */
    public String getSeeAlso(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.see_also.toString();
    }

    /**
     * Retrieve an "antonyms" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "antonyms" entry
     */
    public String getAntonyms(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.antonyms.toString();
    }

    /**
     * Retrieve a "source" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "source" entry
     */
    public String getSource(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.source.toString();
    }

    /**
     * Retrieve a "info" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "info" entry
     */
    public String getInfo(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.info.toString();
    }

    /**
     * Retrieve a "sentences" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "sentences" entry
     */
    public String getSentences(int entryNum, int sensesNum) {
        JishoParseSenses tempParse = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses.class);
        return tempParse.sentences.toString();
    }

    /**
     * Retrieve a "attribution" entry from a given "senses" entry under a given
     * "data" entry
     *
     * @param entryNum The desired "data" entry
     * @param key      The desired key: jmdict, jmnedict, dbpedia
     * @return A string representing the chosen "attribution"
     */
    public String getAttribution(int entryNum, String key) {
        JishoParseEntry tempParse = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry.class);
        return tempParse.attribution.get(key).toString();
    }

    // A nested class used to store our various JSON objects as Java objects.
    private class JishoParseData {

        public JsonArray data;

        public JsonObject meta;

        public JishoParseData(JsonArray asJsonArray, JsonObject jsonObject) {
            this.data = asJsonArray;
            this.meta = jsonObject;
        }

    }

    private class JishoParseEntry {

        public String slug;

        private boolean is_common;

        private ArrayList<String> tags;

        private ArrayList<String> jlpt;

        private ArrayList<Object> japanese;

        private JsonArray senses;

        private Map<Object, Object> attribution;

        @SuppressWarnings("unused")
        private String word;

        @SuppressWarnings("unused")
        private String reading;

    }

    private class JishoParseJapanese {

        public String word;

        public String reading;

    }

    private class JishoParseSenses {

        public ArrayList<String> english_definitions;

        public ArrayList<String> parts_of_speech;

        public ArrayList<Object> links;

        public Object tags;

        public Object restrictions;

        public Object see_also;

        public Object antonyms;

        public Object source;

        public Object info;

        public Object sentences;

    }

    @SuppressWarnings("unused")
    private class JishoParseLinks {

        public String text;

        public String url;

    }
}
