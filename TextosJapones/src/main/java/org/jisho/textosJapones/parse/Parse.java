package org.jisho.textosJapones.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Parse {
	void parse(File file) throws IOException;

	void destroy() throws IOException;

	String getType();

	InputStream getPage(int num) throws IOException;

	int numPages();

	List<String> getSubtitles();

	Map<String, Integer> getSubtitlesNames();

	String getPagePath(Integer num);

	Map<String, Integer> getPagePaths();
}
