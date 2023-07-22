package org.jisho.textosJapones.fileparse;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.jisho.textosJapones.util.Util;

import java.io.*;
import java.util.*;

public class RarParse implements Parse {

	private Archive mArquivo;
	private File mPastaCache;
	private ArrayList<FileHeader> mCabecalhos = new ArrayList<FileHeader>();
	private ArrayList<FileHeader> mLegendas = new ArrayList<FileHeader>();

	@Override
	public void parse(File file) throws IOException {
		try {
			mArquivo = new Archive(file);
		} catch (RarException e) {
			e.printStackTrace();
			throw new IOException("unable to open archive");
		}

		FileHeader cabecalho = mArquivo.nextFileHeader();
		while (cabecalho != null) {
			if (!cabecalho.isDirectory()) {
				String name = getName(cabecalho);
				if (Util.isImage(name)) {
					mCabecalhos.add(cabecalho);
				}
			}

			cabecalho = mArquivo.nextFileHeader();
		}

		Collections.sort(mCabecalhos, new Comparator<FileHeader>() {
			public int compare(FileHeader a, FileHeader b) {
				return Util.getCaminho(getName(a)).compareTo(Util.getCaminho(getName(b)));
			}
		}.thenComparing(new Comparator<FileHeader>() {
			public int compare(FileHeader a, FileHeader b) {
				return Util.getNomeNormalizadoOrdenacao(getName(a)).compareTo(Util.getNomeNormalizadoOrdenacao(getName(b)));
			}
		}));
	}

	@Override
	public int getSize() {
		return mCabecalhos.size();
	}

	@Override
	public InputStream getPagina(int num) throws IOException {
		try {
			FileHeader cabecalho = mCabecalhos.get(num);

			if (mPastaCache != null) {
				String name = getName(cabecalho);
				File cacheFile = new File(mPastaCache, Util.MD5(name));

				if (cacheFile.exists()) {
					return new FileInputStream(cacheFile);
				}

				synchronized (this) {
					if (!cacheFile.exists()) {
						FileOutputStream os = new FileOutputStream(cacheFile);
						try {
							mArquivo.extractFile(cabecalho, os);
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
			return mArquivo.getInputStream(cabecalho);
		} catch (RarException e) {
			throw new IOException("unable to parse rar");
		}
	}

	@Override
	public void destroir() throws IOException {
		if (mPastaCache != null) {
			for (File f : mPastaCache.listFiles()) {
				f.delete();
			}
			mPastaCache.delete();
		}
		mArquivo.close();
	}

	@Override
	public String getTipo() {
		return "rar";
	}

	public void setCacheDirectory(File cacheDirectory) {
		mPastaCache = cacheDirectory;
		if (!mPastaCache.exists())
			mPastaCache.mkdirs();
		
		if (mPastaCache.listFiles() != null) {
			for (File f : mPastaCache.listFiles())
				f.delete();
		}
	}

	@Override
	public List<String> getLegenda() {
		List<String> legendas = new ArrayList<String>();
		mLegendas.forEach((it) -> {
			InputStream sub;
			BufferedReader reader;
			try {
				sub = mArquivo.getInputStream(it);
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

	private String getName(FileHeader header) {
		return header.getFileName();
	}

	@Override
	public String getPaginaPasta(Integer num) {
		if (mCabecalhos.size() < num)
			return null;
		return getName(mCabecalhos.get(num));
	}

	@Override
	public Map<String, Integer> getPastas() {
		Map<String, Integer> pastas = new HashMap<String, Integer>();

		for (var i = 0; i < mCabecalhos.size(); i++) {
			String path = Util.getPasta(getName(mCabecalhos.get(i)));
			if (!path.isEmpty() && !pastas.containsKey(path))
				pastas.put(path, i);
		}

		return pastas;
	}
}
