package org.jisho.textosJapones.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jisho.textosJapones.util.Util;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class RarParse implements Parse {

	private ArrayList<FileHeader> mHeaders = new ArrayList<FileHeader>();
	private Archive mArchive;
	private File mCacheDir;
	private Boolean mSolidFileExtracted = false;
	private ArrayList<FileHeader> mSubtitles = new ArrayList<FileHeader>();

	@Override
	public void parse(File file) throws IOException {
		try {
			mArchive = new Archive(file);
		} catch (RarException e) {
			throw new IOException("unable to open archive");
		}

		FileHeader header = mArchive.nextFileHeader();
		while (header != null) {
			if (!header.isDirectory()) {
				String name = getName(header);
				if (Util.isImage(name)) {
					mHeaders.add(header);
				}
			}

			header = mArchive.nextFileHeader();
		}

		Collections.sort(mHeaders, new Comparator<FileHeader>() {
			public int compare(FileHeader a, FileHeader b) {
				return getName(a).compareTo(getName(b));
			}
		});
	}

	private String getName(FileHeader header) {
		return header.getFileName();
	}

	@Override
	public int numPages() {
		return mHeaders.size();
	}

	@Override
	public InputStream getPage(int num) throws IOException {
		try {
			FileHeader header = mHeaders.get(num);

			if (mCacheDir != null) {
				String name = getName(header);
				File cacheFile = new File(mCacheDir, Util.MD5(name));

				if (cacheFile.exists()) {
					return new FileInputStream(cacheFile);
				}

				synchronized (this) {
					if (!cacheFile.exists()) {
						FileOutputStream os = new FileOutputStream(cacheFile);
						try {
							mArchive.extractFile(header, os);
						} catch (Exception e) {
							os.close();
							cacheFile.delete();
							throw e;
						}
						os.close();
					}
				}
				return new FileInputStream(cacheFile);
			}
			return mArchive.getInputStream(header);
		} catch (RarException e) {
			throw new IOException("unable to parse rar");
		}
	}

	@Override
	public void destroy() throws IOException {
		if (mCacheDir != null) {
			for (File f : mCacheDir.listFiles()) {
				f.delete();
			}
			mCacheDir.delete();
		}
		mArchive.close();
	}

	@Override
	public String getType() {
		return "rar";
	}

	public void setCacheDirectory(File cacheDirectory) {
		mCacheDir = cacheDirectory;
		if (!mCacheDir.exists()) {
			mCacheDir.mkdir();
		}
		if (mCacheDir.listFiles() != null) {
			for (File f : mCacheDir.listFiles()) {
				f.delete();
			}
		}
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
