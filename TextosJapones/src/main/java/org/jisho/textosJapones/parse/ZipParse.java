package org.jisho.textosJapones.parse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.jisho.textosJapones.util.Util;

import java.util.zip.ZipEntry;

public class ZipParse implements Parse {

	private ZipFile mZipFile;
	private ArrayList<ZipEntry> mEntries;
	private ArrayList<ZipEntry> mSubtitles;

	@Override
	public void parse(File file) throws IOException {
		mZipFile = new ZipFile(file.getAbsolutePath());
		mEntries = new ArrayList<ZipEntry>();

		Enumeration<? extends ZipEntry> e = mZipFile.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = e.nextElement();
			if (!ze.isDirectory() && Util.isImage(ze.getName())) {
				mEntries.add(ze);
			}
		}

		Collections.sort(mEntries, new Comparator<ZipEntry>() {
			public int compare(ZipEntry a, ZipEntry b) {
				return a.getName().compareTo(b.getName());
			}
		});
	}

	@Override
	public int numPages() {
		return mEntries.size();
	}

	@Override
	public InputStream getPage(int num) throws IOException {
		return mZipFile.getInputStream(mEntries.get(num));
	}

	@Override
	public String getType() {
		return "zip";
	}

	@Override
	public void destroy() throws IOException {
		mZipFile.close();
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
