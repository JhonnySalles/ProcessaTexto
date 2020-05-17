package org.jisho.textosJapones.model.dao;

import org.jisho.textosJapones.model.dao.impl.VocabularioDaoJDBC;
import org.jisho.textosJapones.util.mysql.DB;

public class DaoFactory {

	public static VocabularioDao createVocabularioDao() {
		return new VocabularioDaoJDBC(DB.getConnection());
	}

}
