package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.database.dao.implement.*;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.enums.Conexao;

public class DaoFactory {

	public static VocabularioDao createVocabularioJaponesDao() {
		return new VocabularioJaponesDaoJDBC(DB.getLocalConnection());
	}
	
	public static VocabularioDao createVocabularioInglesDao() {
		return new VocabularioInglesDaoJDBC(DB.getConnection(Conexao.TEXTOINGLES));
	}

	public static EstatisticaDao createEstatisticaDao() {
		return new EstatisticaDaoJDBC(DB.getLocalConnection());
	}

	public static RevisarDao createRevisarJaponesDao() {
		return new RevisarJaponesDaoJDBC(DB.getLocalConnection());
	}
	
	public static RevisarDao createRevisarInglesDao() {
		return new RevisarInglesDaoJDBC(DB.getConnection(Conexao.TEXTOINGLES));
	}

	public static LegendasDao createLegendasDao() {
		return new LegendasDaoJDBC(DB.getLocalConnection(), DB.getConnection(Conexao.DECKSUBTITLE), DB.getDados(Conexao.DECKSUBTITLE).getBase());
	}

	public static MangaDao createMangaDao() {
		return new MangaDaoJDBC(DB.getConnection(Conexao.MANGAEXTRACTOR), DB.getDados(Conexao.MANGAEXTRACTOR).getBase());
	}

	public static NovelDao createNovelDao() {
		return new NovelDaoJDBC(DB.getConnection(Conexao.NOVELEXTRACTOR), DB.getDados(Conexao.NOVELEXTRACTOR).getBase());
	}
	
	public static VincularDao createVincularDao() {
		return new VincularDaoJDBC(DB.getConnection(Conexao.MANGAEXTRACTOR));
	}

	public static KanjiDao createKanjiDao() {
		return new KanjiDaoJDBC(DB.getLocalConnection());
	}

	public static ComicInfoDao createComicInfoDao() {
		return new ComicInfoJDBC(DB.getLocalConnection());
	}

}
