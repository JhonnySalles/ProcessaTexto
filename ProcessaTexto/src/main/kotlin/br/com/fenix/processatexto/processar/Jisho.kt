package br.com.fenix.processatexto.processar

import com.google.gson.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * A constructor for a Jisho object which performs all necessary operations
 */
class Jisho {
    /**
     * Retrieve JSON data as a string
     *
     * @return JSON data as a string
     */
    /**
     * a variable to hold the JSON data
     */
    private var jsonString: String? = null

    /**
     * the desired word to search. Refer to Jisho.org regarding search methods
     */
    @SuppressWarnings("unused")
    private var searchWord: String? = null

    /**
     * HTTP response status code
     */
    private var responseCode = 0

    /**
     * A nested class instance which will store the JSON data into individual
     * entries
     */
    private var parse: JishoParseData? = null

    /**
     * A GSON instance to parse JSON
     */
    private lateinit var gson: Gson

    /**
     * Perform a search of a given word via Jisho.org. Refer to Jisho.org regarding
     * search methods
     *
     * @param searchWord the desired search word. Refer to Jisho.org regarding
     * search methods
     * @throws IOException
     */
    @Throws(Exception::class)
    fun search(searchWord: String?) {

        // variable for storing the url
        val url: URL

        // storing search word, initializing jsonString
        this.searchWord = searchWord
        jsonString = ""

        // Encode query string as searches with japanese characters are problematic
        val word: String = URLEncoder.encode(searchWord, StandardCharsets.UTF_8.toString())
        url = URL("https://jisho.org/api/v1/search/words?keyword=$word")
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

        // create the connecion
        conn.requestMethod = "GET"
        conn.connect()
        responseCode = conn.responseCode

        // check that the connection was successful, throw error otherwise
        if (responseCode != 200)
            throw Exception("HttpResponseCode: $responseCode\nurl:$url")

        // create a scanner, prevent it from removing whitespace from our JSON data
        val input = Scanner(url.openStream())
        input.useDelimiter(System.getProperty("line.separator")) // Prevent scanner from removing whitespace

        // read our JSON string into our jsonString variable
        while (input.hasNext()) {
            jsonString += input.next()
        }

        // close the input
        input.close()

        // Creating custom deserializer to keep each entry serialized and stored in an
        // array until it is needed
        val builder = GsonBuilder()
        builder.registerTypeAdapter(JishoParseData::class.java, deserializer)
        gson = builder.create()

        // initial parsing of the JSON string, separating into data and meta fields
        parse = gson.fromJson(jsonString, JishoParseData::class.java)
    }

    // An initial deserializer which separates the data and meta entries
    private val deserializer: JsonDeserializer<JishoParseData> = JsonDeserializer<JishoParseData> { json, _, _ ->
        val jsonObject: JsonObject = json.asJsonObject
        JishoParseData( // preserving our data and meta fields and JSON
            jsonObject.get("data").asJsonArray, jsonObject.get("meta").asJsonObject
        )
    }

    /**
     * Retrieve HTTP response status code
     *
     * @return HTTP response status code as a string
     */
    val status: String
        get() {
            val status: String = parse!!.meta.toString()
            return status.substring(10, status.length - 1)
        }

    /**
     * The number of entries in the "data" list
     *
     * @return an integer which represents the number of entries in the "data" list
     */
    val numEntries: Int get() = parse!!.data.size()

    // Potentially have each entry
    private fun getEntry(entryNum: Int): JsonElement? {
        return parse!!.data.get(entryNum)
    }

    /**
     * Retrieve the "slug" from a desired "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "slug"
     * from
     * @return A string representing the "slug" entry
     */
    fun getSlug(entryNum: Int): String? {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.slug
    }

    /**
     * Retrieve the value of the "is_common" field in a give "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the
     * "is_common" from
     * @return A boolean representing the "is_common" field
     */
    fun getIsCommon(entryNum: Int): Boolean {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.is_common
    }

    /**
     * Retrieve an integer representing the size of the "tags" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "tags"
     * size from
     * @return An integer representing the size of the "tags" field
     */
    fun getTagsSize(entryNum: Int): Int {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.tags!!.size
    }

    /**
     * Retrieve a given tag from the "tags" under a given "data" entry
     *
     * @param entryNum The desired "data" entry
     * @param tagNum   The desired "tags" entry
     * @return The string representing the "tags" entry
     */
    fun getTags(entryNum: Int, tagNum: Int): String {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.tags!![tagNum]
    }

    /**
     * Retrieve an integer representing the size of the "jlpt" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "tags"
     * size from
     * @return An integer representing the size of the "jlpt" field
     */
    fun getJlptSize(entryNum: Int): Int {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.jlpt!!.size
    }

    /**
     * Retrieve a "jlpt" entry from a given "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "jlpt"
     * from
     * @param jlptNum  The desired "jlpt" entry
     * @return A string representing the "jlpt" entry
     */
    fun getJlpt(entryNum: Int, jlptNum: Int): String {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.jlpt!![jlptNum]
    }

    /**
     * Retrieve an integer representing the size of the "japanese" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "japanese"
     * size from
     * @return An integer representing the size of the "japanese" field
     */
    fun getJapaneseSize(entryNum: Int): Int {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.japanese!!.size
    }

    private fun getJapanese(entryNum: Int, japaneseNum: Int): Any {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.japanese!![japaneseNum]
    }

    /**
     * Retrieve a "word" entry from a given "japanese" entry under a given "data"
     * entry
     *
     * @param entryNum    The desired "data" entry
     * @param japaneseNum The desired "jlpt" entry
     * @return A string representing the "word" entry
     */
    fun getJapaneseWord(entryNum: Int, japaneseNum: Int): String? {
        val tempParse: JishoParseJapanese = gson.fromJson(getJapanese(entryNum, japaneseNum).toString(), JishoParseJapanese::class.java)
        return tempParse.word
    }

    /**
     * Retrieve a "reading" entry from a given "japanese" entry under a given "data"
     * entry
     *
     * @param entryNum    The desired "data" entry
     * @param japaneseNum The desired "jlpt" entry
     * @return A string representing the "reading" entry
     */
    fun getJapaneseReading(entryNum: Int, japaneseNum: Int): String? {
        val tempParse: JishoParseJapanese = gson.fromJson(getJapanese(entryNum, japaneseNum).toString(), JishoParseJapanese::class.java)
        return tempParse.reading
    }

    /**
     * Retrieve an integer representing the size of the "senses" field in a given
     * "data" entry
     *
     * @param entryNum The respective "data" entry from which to pull the "senses"
     * size from
     * @return An integer representing the size of the "senses" field
     */
    fun getSensesSize(entryNum: Int): Int {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.senses!!.size()
    }

    private fun getSenses(entryNum: Int, sensesNum: Int): JsonElement? {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        return tempParse.senses!!.get(sensesNum)
    }

    /**
     * Retrieve an integer representing the size of the "english_definitions" field
     * in a given "senses" entry under a given "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return An integer representing the size of the "english_definitions" field
     */
    fun getEnglishDefinitionsSize(entryNum: Int, sensesNum: Int): Int {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.english_definitions!!.size
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
    fun getEnglishDefinitions(entryNum: Int, sensesNum: Int, englishDefNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.english_definitions!![englishDefNum]
    }

    /**
     * Retrieve an integer representing the size of the "parts_of_speech" field in a
     * given "senses" entry under a given "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return An integer representing the size of the "parts_of_speech" field
     */
    fun getPartsOfSpeechSize(entryNum: Int, sensesNum: Int): Int {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.parts_of_speech!!.size
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
    fun getPartsOfSpeech(entryNum: Int, sensesNum: Int, partsOfSpeechNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.parts_of_speech!![partsOfSpeechNum]
    }

    /**
     * Retrieve an integer representing the size of the "links" field in a given
     * "senses" entry under a given "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return An integer representing the size of the "links" field
     */
    fun getLinksSize(entryNum: Int, sensesNum: Int): Int {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.links!!.size
    }

    /**
     * Retrieve a "tags" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "tags" entry
     */
    fun getTagsSenses(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.tags.toString()
    }

    /**
     * Retrieve a "restrictions" entry from a given "senses" entry under a given
     * "data" entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "restrictions" entry
     */
    fun getRestrictions(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.restrictions.toString()
    }

    /**
     * Retrieve a "see_also" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "see_also" entry
     */
    fun getSeeAlso(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.see_also.toString()
    }

    /**
     * Retrieve an "antonyms" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "antonyms" entry
     */
    fun getAntonyms(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.antonyms.toString()
    }

    /**
     * Retrieve a "source" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "source" entry
     */
    fun getSource(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.source.toString()
    }

    /**
     * Retrieve a "info" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "info" entry
     */
    fun getInfo(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.info.toString()
    }

    /**
     * Retrieve a "sentences" entry from a given "senses" entry under a given "data"
     * entry
     *
     * @param entryNum  The desired "data" entry
     * @param sensesNum The desired "senses" entry
     * @return A string representing the "sentences" entry
     */
    fun getSentences(entryNum: Int, sensesNum: Int): String {
        val tempParse: JishoParseSenses = gson.fromJson(getSenses(entryNum, sensesNum).toString(), JishoParseSenses::class.java)
        return tempParse.sentences.toString()
    }

    /**
     * Retrieve a "attribution" entry from a given "senses" entry under a given
     * "data" entry
     *
     * @param entryNum The desired "data" entry
     * @param key      The desired key: jmdict, jmnedict, dbpedia
     * @return A string representing the chosen "attribution"
     */
    fun getAttribution(entryNum: Int, key: String): String {
        val tempParse: JishoParseEntry = gson.fromJson(getEntry(entryNum).toString(), JishoParseEntry::class.java)
        val attr = tempParse.attribution!![key as Any]
        return attr?.toString() ?: ""
    }

    // A nested class used to store our various JSON objects as Java objects.
    private inner class JishoParseData(asJsonArray: JsonArray, jsonObject: JsonObject) {
        var data: JsonArray
        var meta: JsonObject

        init {
            data = asJsonArray
            meta = jsonObject
        }
    }

    private inner class JishoParseEntry {
        var slug: String? = null
        val is_common = false
        val tags: ArrayList<String>? = null
        val jlpt: ArrayList<String>? = null
        val japanese: ArrayList<Any>? = null
        val senses: JsonArray? = null
        val attribution: Map<Any, Any>? = null

        @SuppressWarnings("unused")
        private val word: String? = null

        @SuppressWarnings("unused")
        private val reading: String? = null
    }

    private inner class JishoParseJapanese {
        var word: String? = null
        var reading: String? = null
    }

    private inner class JishoParseSenses {
        var english_definitions: ArrayList<String>? = null
        var parts_of_speech: ArrayList<String>? = null
        var links: ArrayList<Any>? = null
        var tags: Any? = null
        var restrictions: Any? = null
        var see_also: Any? = null
        var antonyms: Any? = null
        var source: Any? = null
        var info: Any? = null
        var sentences: Any? = null
    }

    @SuppressWarnings("unused")
    private inner class JishoParseLinks {
        var text: String? = null
        var url: String? = null
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Jisho::class.java)
        var japanese = ""
        fun processa(kanji: String?): String {
            var retorno = ""
            val jisho = Jisho()
            try {
                jisho.search(kanji)
                for (i in 0 until jisho.numEntries) {
                    val json = JSONObject(jisho.getEntry(i).toString())
                    try {
                        val slug: String = json.get("slug").toString()
                        val japones: JSONArray = json.get("japanese") as JSONArray
                        japanese = ""
                        japones.forEach { item ->
                            val obj: JSONObject = item as JSONObject
                            val word: String = obj.get("word").toString()
                            if (word.equals(kanji, true)) {
                                japanese = word
                                return@forEach
                            }
                        }
                        if (slug.equals(kanji, true) || japanese.equals(kanji, true)) {
                            for (x in 0 until jisho.getSensesSize(i))
                                for (y in 0 until jisho.getEnglishDefinitionsSize(i, x))
                                    retorno += jisho.getEnglishDefinitions(i, x, y) + ", "

                            retorno = retorno.substring(0, retorno.lastIndexOf(", ")) + "."
                            break
                        }
                    } catch (js: JSONException) {
                        js.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
                return retorno
            }
            return retorno
        }
    }
}