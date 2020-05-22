package org.jisho.textosJapones.util.enuns;

public enum Modo {
	
	A("Modo A"), B("Modo B"), C("Modo C");

	private String modo;

	Modo(String modo) {
		this.modo = modo;
	}

	public String getDescricao() {
		return modo;
	}
	
	@Override      
	public String toString(){
	    return this.modo;
	} 

}
