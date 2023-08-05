package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.enums.Conexao;

public class DadosConexao {
    private Long id;
    private Conexao tipo;
    private String url;
    private String base;

    public Long getId() {
        return id;
    }

    public Conexao getTipo() {
        return tipo;
    }

    public String getUrl() {
        return url;
    }

    public String getBase() {
        return base;
    }

    public DadosConexao(Long id, Conexao tipo, String url, String base) {
        this.id = id;
        this.tipo = tipo;
        this.url = url;
        this.base = base;
    }
}
