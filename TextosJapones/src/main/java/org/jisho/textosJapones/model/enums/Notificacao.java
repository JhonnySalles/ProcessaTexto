package org.jisho.textosJapones.model.enums;

/**
 * <p>
 * Enuns utilizado pelo popup de notificações.
 * </p>
 * 
 * <p>
 * <b>ALERTA, AVISO, ERRO, SUCESSO</b>
 * </p>
 * 
 * @author Jhonny de Salles Noschang
 */
public enum Notificacao {

	ALERTA("Alerta"), AVISO("Aviso"), ERRO("Erro"), SUCESSO("Sucesso");

	private final String notificacao;

	Notificacao(String notificacao) {
		this.notificacao = notificacao;
	}

	public String getDescricao() {
		return notificacao;
	}

	// Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
	@Override
	public String toString() {
		return this.notificacao;
	}

}
