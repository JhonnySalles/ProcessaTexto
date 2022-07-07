package org.jisho.textosJapones.parse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.controlsfx.tools.Utils;
import org.jisho.textosJapones.util.NaturalOrderComparator;
import org.jisho.textosJapones.util.Util;

public class SevenZipParse implements Parse {

	private List<SevenZEntry> mEntries;

	private class SevenZEntry {
		final SevenZArchiveEntry entry;
		final byte[] bytes;

		public SevenZEntry(SevenZArchiveEntry entry, byte[] bytes) {
			this.entry = entry;
			this.bytes = bytes;
		}
	}

	@Override
	public void parse(File file) throws IOException {
		mEntries = new ArrayList<>();
		SevenZFile sevenZFile = new SevenZFile(file);

		SevenZArchiveEntry entry = sevenZFile.getNextEntry();
		while (entry != null) {
			if (entry.isDirectory()) {
				continue;
			}
			if (Util.isImage(entry.getName())) {
				byte[] content = new byte[(int) entry.getSize()];
				sevenZFile.read(content);
				mEntries.add(new SevenZEntry(entry, content));
			}
			entry = sevenZFile.getNextEntry();
		}

		Collections.sort(mEntries, new NaturalOrderComparator() {
			@Override
			public String stringValue(Object o) {
				return ((SevenZEntry) o).entry.getName();
			}
		});
	}

	@Override
	public int numPages() {
		return mEntries.size();
	}

	@Override
	public InputStream getPage(int num) throws IOException {
		return new ByteArrayInputStream(mEntries.get(num).bytes);
	}

	@Override
	public String getType() {
		return "tar";
	}

	@Override
	public void destroy() throws IOException {

	}

	@Override
	public List<String> getSubtitles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> getSubtitlesNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPagePath(Integer num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> getPagePaths() {
		// TODO Auto-generated method stub
		return null;
	}

}
