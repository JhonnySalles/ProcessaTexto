package org.jisho.textosJapones.fileparse;

import java.io.File;
import java.io.IOException;

import org.jisho.textosJapones.util.Util;

public class ParseFactory {
	public static Parse create(String file) {
		return create(new File(file));
	}

	public static Parse create(File file) {
		Parse parser = null;
		String fileName = file.getAbsolutePath().toLowerCase();

		if (Util.isRar(fileName)) {
			parser = new RarParse();
		} else if (Util.isZip(fileName)) {
			parser = new ZipParse();
		} else if (Util.isSevenZ(fileName)) {
			parser = new SevenZipParse();
		} else if (Util.isTarball(fileName)) {
			parser = new TarParse();
		}
		return tryParse(parser, file);
	}

	private static Parse tryParse(Parse parse, File file) {
		if (parse == null) {
			return null;
		}
		try {
			parse.parse(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return parse;
	}
}
