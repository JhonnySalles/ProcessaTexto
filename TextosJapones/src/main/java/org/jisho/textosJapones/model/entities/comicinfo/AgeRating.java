package org.jisho.textosJapones.model.entities.comicinfo;

public enum AgeRating {
	Unknown("Unknown"),
	Adults("Adults Only 18+"),
	Early("Early Childhood"),
	Everyone("Everyone"),
	Everyone10("Everyone 10+"),
	G("G"),
	Kids("Kids to Adults"),
	M("M"),
	MA15("MA15+"),
	Mature("Mature 17+"),
	PG("PG"),
	R18("R18+"),
	Pending("Rating Pending"),
	Teen("Teen"),
	X18("X18+");
	
	private final String api;

	AgeRating(String api) {
		this.api = api;
	}

	public String getDescricao() {
		return api;
	}

	// Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
	@Override
	public String toString() {
		return this.api;
	}
}
