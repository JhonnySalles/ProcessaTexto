package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.enums.Conexao;

public class DadosConexao {
    private final Long id;
    private final Conexao tipo;
    private final String url;
    private final String base;

    private final String usuario;
    private final String senha;

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

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }

    public DadosConexao(Long id, Conexao tipo, String url, String base, String usuario, String senha) {
        this.id = id;
        this.tipo = tipo;
        this.url = url;
        this.base = base;
        this.usuario = usuario;
        this.senha = senha;
    }
}
