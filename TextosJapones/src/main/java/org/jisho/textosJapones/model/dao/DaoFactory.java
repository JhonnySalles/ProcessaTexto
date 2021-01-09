package org.jisho.textosJapones.model.dao;

import org.jisho.textosJapones.model.dao.impl.EstatisticaDaoJDBC;
import org.jisho.textosJapones.model.dao.impl.RevisarDaoJDBC;
import org.jisho.textosJapones.model.dao.impl.VocabularioDaoJDBC;
import org.jisho.textosJapones.util.mysql.DB;

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

}
