package org.jisho.textosJapones.fileparse;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.jisho.textosJapones.util.NaturalOrderComparator;
import org.jisho.textosJapones.util.Util;

import java.io.*;
import java.util.*;

public class TarParse implements Parse {

	private List<TarEntry> mEntradas;
	private List<TarEntry> mLegendas;

	private class TarEntry {
		final TarArchiveEntry entry;
		final byte[] bytes;

		public TarEntry(TarArchiveEntry entry, byte[] bytes) {
			this.entry = entry;
			this.bytes = bytes;
		}
	}

	@Override
	public void parse(File file) throws IOException {
		mEntradas = new ArrayList<>();

		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
		TarArchiveInputStream is = new TarArchiveInputStream(fis);
		TarArchiveEntry entry = is.getNextTarEntry();
		while (entry != null) {
			if (entry.isDirectory()) {
				continue;
			}
			if (Util.isImage(entry.getName())) {
				mEntradas.add(new TarEntry(entry, Util.toByteArray(is)));
			}
			entry = is.getNextTarEntry();
		}

		Collections.sort(mEntradas, new NaturalOrderComparator<TarEntry>() {
			@Override
			public String stringValue(TarEntry o) {
				return o.entry.getName();
			}
		}.thenComparing(new NaturalOrderComparator<TarEntry>() {
			@Override
			public String stringValue(TarEntry o) {
				return o.entry.getName();
			}
		}));
	}

	@Override
	public int getSize() {
		return mEntradas.size();
	}

	@Override
	public InputStream getPagina(int num) throws IOException {
		return new ByteArrayInputStream(mEntradas.get(num).bytes);
	}

	@Override
	public String getTipo() {
		return "tar";
	}

	@Override
	public void destroir() throws IOException {

	}

	@Override
	public List<String> getLegenda() {
		List<String> legendas = new ArrayList<String>();
		mLegendas.forEach((it) -> {
			InputStream sub = new ByteArrayInputStream(it.bytes);
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(sub, "UTF-8"));
				StringBuilder content = new StringBuilder();

				var line = reader.readLine();
				while (line != null) {
					content.append(line);
					line = reader.readLine();
				}

				legendas.add(content.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return legendas;
	}

	@Override
	public Map<String, Integer> getLegendaNomes() {
		Map<String, Integer> arquivos = new HashMap<String, Integer>();

		for (var i = 0; i < mLegendas.size(); i++) {
			String path = Util.getNome(getName(mLegendas.get(i)));
			if (!path.isEmpty() && !arquivos.containsKey(path))
				arquivos.put(path, i);
		}

		return arquivos;
	}

	private String getName(TarEntry obj) {
		return obj.entry.getName();
	}

	@Override
	public String getPaginaPasta(Integer num) {
		if (mEntradas.size() < num)
            return null;
        return getName(mEntradas.get(num));
	}

	@Override
	public Map<String, Integer> getPastas() {
		Map<String, Integer> pastas = new HashMap<String, Integer>();

		for (var i = 0; i < mEntradas.size(); i++) {
			String path = Util.getPasta(getName(mEntradas.get(i)));
			if (!path.isEmpty() && !pastas.containsKey(path))
				pastas.put(path, i);
		}

        return pastas;
	}

}
