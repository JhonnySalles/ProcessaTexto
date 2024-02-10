package org.jisho.textosJapones.model.enums;

public enum Language {

	TODOS("all"),
	AFRIKAANS("af"),
	ALBANIAN("sq"),
	ARABIC("ar"),
	ARMENIAN("hy"),
	AZERBAIJANI("az"),
	BASQUE("eu"),
	BELARUSIAN("be"),
	BENGALI("bn"),
	BULGARIAN("bg"),
	CATALAN("ca"),
	CHINESE("zh-CN"),
	CROATIAN("hr"),
	CZECH("cs"),
	DANISH("da"),
	DUTCH("nl"),
	ENGLISH("en"),
	ESTONIAN("et"),
	FILIPINO("tl"),
	FINNISH("fi"),
	FRENCH("fr"),
	GALICIAN("gl"),
	GEORGIAN("ka"),
	GERMAN("de"),
	GREEK("el"),
	GUJARATI("gu"),
	HAITIAN_CREOLE("ht"),
	HEBREW("iw"),
	HINDI("hi"),
	HUNGARIAN("hu"),
	ICELANDIC("is"),
	INDONESIAN("id"),
	IRISH("ga"),
	ITALIAN("it"),
	JAPANESE("ja"),
	KANNADA("kn"),
	KOREAN("ko"),
	LATIN("la"),
	LATVIAN("lv"),
	LITHUANIAN("lt"),
	MACEDONIAN("mk"),
	MALAY("ms"),
	MALTESE("mt"),
	NORWEGIAN("no"),
	PERSIAN("fa"),
	POLISH("pl"),
	PORTUGUESE("pt"),
	PORTUGUESE_GOOGLE("pt-Glt"),
	ROMANIAN("ro"),
	RUSSIAN("ru"),
	SERBIAN("sr"),
	SLOVAK("sk"),
	SLOVENIAN("sl"),
	SPANISH("es"),
	SWAHILI("sw"),
	SWEDISH("sv"),
	TAMIL("ta"),
	TELUGU("te"),
	THAI("th"),
	TURKISH("tr"),
	UKRAINIAN("uk"),
	URDU("ur"),
	VIETNAMESE("vi"),
	WELSH("cy"),
	YIDDISH("yi"),
	CHINESE_SIMPLIFIED("zh-CN"),
	CHINESE_TRADITIONAL("zh-TW");
	
	private final String linguagem;

	Language(String linguagem){
		this.linguagem = linguagem;
	}

	public String getSigla() {
		return linguagem;
	}
	
	public static Language getEnum(String sigla){
        for(Language e : Language.values()){
            if(e.linguagem.equals(sigla)) 
            	return e;
        }
        return null;
    }

}
