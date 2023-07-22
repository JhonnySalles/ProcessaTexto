package org.jisho.textosJapones.processar;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.tokenizers.SudachiTokenizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessarPalavra {

	public List<String> processarDesmembrar(String palavra, Dicionario dicionario, Modo modo) {
		try (Dictionary dict = new DictionaryFactory().create("",
				SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(dicionario))))) {
			tokenizer = dict.create();
			mode = SudachiTokenizer.getModo(modo);
			return processar(palavra);

		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	final private String pattern = ".*[\u4E00-\u9FAF].*";
	private Tokenizer tokenizer;
	private SplitMode mode;

	private List<String> processar(String palavra) {
		List<String> resultado = new ArrayList<>();
		for (Morpheme m : tokenizer.tokenize(mode, palavra))
			if (m.surface().matches(pattern))
				resultado.add(m.surface());

		return resultado;
	}

}
