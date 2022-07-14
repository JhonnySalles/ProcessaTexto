package org.jisho.textosJapones.fileparse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Parse {
	void parse(File arquivo) throws IOException;

	void destroi() throws IOException;

	String getTipo();

	InputStream getPagina(int numero) throws IOException;

	int getSize();

	List<String> getLegenda();

	Map<String, Integer> getLegendaNomes();

	String getPaginaPasta(Integer num);

	Map<String, Integer> getPastas();
}
