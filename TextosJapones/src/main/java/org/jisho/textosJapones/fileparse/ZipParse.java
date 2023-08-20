package org.jisho.textosJapones.fileparse;

import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipParse implements Parse {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipParse.class);

    private ZipFile mArquivoZip;
    private ArrayList<ZipEntry> mEntrada;
    private ArrayList<ZipEntry> mLegendas;

    @Override
    public void parse(File file) throws IOException {
        mArquivoZip = new ZipFile(file.getAbsolutePath());
        mEntrada = new ArrayList<ZipEntry>();

        Enumeration<? extends ZipEntry> e = mArquivoZip.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            if (!ze.isDirectory() && Util.isImage(ze.getName())) {
                mEntrada.add(ze);
            }
        }

        Collections.sort(mEntrada, new Comparator<ZipEntry>() {
            public int compare(ZipEntry a, ZipEntry b) {
                return Util.getCaminho(a.getName()).compareTo(Util.getCaminho(b.getName()));
            }
        }.thenComparing(new Comparator<ZipEntry>() {
            public int compare(ZipEntry a, ZipEntry b) {
                return Util.getNomeNormalizadoOrdenacao(a.getName()).compareTo(Util.getNomeNormalizadoOrdenacao(b.getName()));
            }
        }));
    }

    @Override
    public int getSize() {
        return mEntrada.size();
    }

    @Override
    public InputStream getPagina(int num) throws IOException {
        return mArquivoZip.getInputStream(mEntrada.get(num));
    }

    @Override
    public String getTipo() {
        return "zip";
    }

    @Override
    public void destroir() throws IOException {
        mArquivoZip.close();
    }

    @Override
    public List<String> getLegenda() {
        List<String> legendas = new ArrayList<String>();
        mLegendas.forEach((it) -> {
            InputStream sub;
            BufferedReader reader;
            try {
                sub = mArquivoZip.getInputStream(it);
                reader = new BufferedReader(new InputStreamReader(sub, StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();

                var line = reader.readLine();
                while (line != null) {
                    content.append(line);
                    line = reader.readLine();
                }

                legendas.add(content.toString());
            } catch (IOException e) {
                
                LOGGER.error(e.getMessage(), e);
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

    private String getName(ZipEntry entry) {
        return entry.getName();
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
