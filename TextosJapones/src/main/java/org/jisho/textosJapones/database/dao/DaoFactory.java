package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.database.dao.implement.EstatisticaDaoJDBC;
import org.jisho.textosJapones.database.dao.implement.MangaDaoJDBC;
import org.jisho.textosJapones.database.dao.implement.ProcessarDaoJDBC;
import org.jisho.textosJapones.database.dao.implement.RevisarDaoJDBC;
import org.jisho.textosJapones.database.dao.implement.VincularDaoJDBC;
import org.jisho.textosJapones.database.dao.implement.VocabularioDaoJDBC;
import org.jisho.textosJapones.database.mysql.DB;

public class DaoFactory {

	public static VocabularioDao createVocabularioDao() {
		return new VocabularioDaoJDBC(DB.getConnection());
	}

	public static EstatisticaDao createEstatisticaDao() {
		return new EstatisticaDaoJDBC(DB.getConnection());
	}

	public static RevisarDao createRevisarDao() {
		return new RevisarDaoJDBC(DB.getConnection());
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