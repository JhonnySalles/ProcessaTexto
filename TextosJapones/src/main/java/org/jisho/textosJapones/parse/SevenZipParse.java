package org.jisho.textosJapones.parse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.jisho.textosJapones.util.NaturalOrderComparator;
import org.jisho.textosJapones.util.Util;

public class SevenZipParse implements Parse {

	private List<SevenZEntry> mEntrada;
	private List<SevenZEntry> mLegendas;

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
		mEntrada = new ArrayList<>();
		SevenZFile sevenZFile = new SevenZFile(file);
		try {
			SevenZArchiveEntry entry = sevenZFile.getNextEntry();
			while (entry != null) {
				if (entry.isDirectory()) {
					continue;
				}
				if (Util.isImage(entry.getName())) {
					byte[] content = new byte[(int) entry.getSize()];
					sevenZFile.read(content);
					mEntrada.add(new SevenZEntry(entry, content));
				}
				entry = sevenZFile.getNextEntry();
			}

			Collections.sort(mEntrada, new NaturalOrderComparator<SevenZEntry>() {
				@Override
				public String stringValue(SevenZEntry o) {
					return o.entry.getName();
				}
			});
		} finally {
			sevenZFile.close();
		}
	}

	@Override
	public int getSize() {
		return mEntrada.size();
	}

	@Override
	public InputStream getPagina(int num) throws IOException {
		return new ByteArrayInputStream(mEntrada.get(num).bytes);
	}

	@Override
	public String getTipo() {
		return "tar";
	}

	@Override
	public void destroi() throws IOException {

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

	private String getName(SevenZEntry obj) {
		return obj.entry.getName();
	}

	@Override
	public String getPaginaPasta(Integer num) {
		if (mEntrada.size() < num)
			return null;
		return getName(mEntrada.get(num));
	}

	@Override
	public Map<String, Integer> getPastas() {
		Map<String, Integer> pastas = new HashMap<String, Integer>();

		for (var i = 0; i < mEntrada.size(); i++) {
			String path = Util.getPasta(getName(mEntrada.get(i)));
			if (!path.isEmpty() && !pastas.containsKey(path))
				pastas.put(path, i);
		}

		return pastas;
	}

}
