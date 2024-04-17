package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.enums.Conexao;

import java.time.LocalDateTime;

public class Sincronizacao {

    private Conexao conexao;
    private LocalDateTime envio;
    private LocalDateTime recebimento;

    public Conexao getConexao() {
        return conexao;
    }

    public LocalDateTime getEnvio() {
        return envio;
    }

    public void setEnvio(LocalDateTime envio) {
        this.envio = envio;
    }

    public LocalDateTime getRecebimento() {
        return recebimento;
    }

    public void setRecebimento(LocalDateTime recebimento) {
        this.recebimento = recebimento;
    }

    public Sincronizacao(Conexao conexao, LocalDateTime envio, LocalDateTime recebimento) {
        this.conexao = conexao;
        this.envio = envio;
        this.recebimento = recebimento;
    }
}
