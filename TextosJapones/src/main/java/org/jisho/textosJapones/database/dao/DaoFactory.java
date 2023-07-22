package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.database.dao.implement.*;
import org.jisho.textosJapones.database.mysql.DB;

public class DaoFactory {

	public static VocabularioDao createVocabularioJaponesDao() {
		return new VocabularioJaponesDaoJDBC(DB.getConnection());
	}
	
	public static VocabularioDao createVocabularioInglesDao() {
		return new VocabularioInglesDaoJDBC(DB.getConnection());
	}

	public static EstatisticaDao createEstatisticaDao() {
		return new EstatisticaDaoJDBC(DB.getConnection());
	}

	public static RevisarDao createRevisarJaponesDao() {
		return new RevisarJaponesDaoJDBC(DB.getConnection());
	}
	
	public static RevisarDao createRevisarInglesDao() {
		return new RevisarInglesDaoJDBC(DB.getConnection());
	}

	public static ProcessarDao createProcessarDao() {
		return new ProcessarDaoJDBC(DB.getConnection());
	}

	public static MangaDao createMangaDao() {
		return new MangaDaoJDBC(DB.getConnection());
	}
	
	public static VincularDao createVincularDao() {
		return new VincularDaoJDBC(DB.getConnection());
	}


}
