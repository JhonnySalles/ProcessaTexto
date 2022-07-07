package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jisho.textosJapones.model.dao.VincularDao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.configuration.Configuracao;
import org.jisho.textosJapones.util.mysql.DB;

public class VincularDaoJDBC implements VincularDao {

	private Connection conn;
	private String BASE_MANGA;

	final private String EXIST_TABELA_VOCABULARIO = "SELECT Table_Name AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_vinculo%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ";

	final private String SELECT_TABELAS = "SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' " 	
			+ " AND Table_Name LIKE '%%_volumes%%' AND Table_Name GROUP BY Tabela ";

	final private String CREATE_TABELA_VINCULO = "CREATE TABLE %s_vinculo (" + "  id INT(11) NOT NULL AUTO_INCREMENT,"
			+ "  original_arquivo VARCHAR(250) DEFAULT NULL," + "  original_linguagem VARCHAR(4) DEFAULT NULL,"
			+ "  id_volume_original INT(11) DEFAULT NULL," + "  vinculado_arquivo VARCHAR(250) DEFAULT NULL,"
			+ "  vinculado_linguagem VARCHAR(4) DEFAULT NULL," + "  id_volume_vinculado INT(11) DEFAULT NULL,"
			+ "  data_criacao DATETIME DEFAULT NULL," + "  ultima_alteracao DATETIME DEFAULT NULL,"
			+ "  PRIMARY KEY (id)," + "  KEY %s_vinculo_original_fk (id_volume_original),"
			+ "  KEY %s_vinculo_vinculado_fk (id_volume_vinculado),"
			+ "  CONSTRAINT %s_vinculo_original_fk FOREIGN KEY (id_volume_original) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculo_vinculado_fk FOREIGN KEY (id_volume_vinculado) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_TABELA_VINCULO_PAGINA = "CREATE TABLE %s_vinculo_pagina ("
			+ "  id INT(11) NOT NULL AUTO_INCREMENT," + "  id_vinculo INT(11) DEFAULT NULL,"
			+ "  original_nome VARCHAR(250) DEFAULT NULL," + "  original_pasta VARCHAR(500) DEFAULT NULL,"
			+ "  original_pagina INT(11) DEFAULT NULL," + "  original_paginas INT(11) DEFAULT NULL,"
			+ "  original_pagina_dupla TINYINT(1) DEFAULT NULL," + "  id_original_pagina INT(11) DEFAULT NULL,"
			+ "  vinculado_direita_nome VARCHAR(250) DEFAULT NULL,"
			+ "  vinculado_direita_pasta VARCHAR(500) DEFAULT NULL,"
			+ "  vinculado_direita_pagina INT(11) DEFAULT NULL," + "  vinculado_direita_paginas INT(11) DEFAULT NULL,"
			+ "  vinculado_direita_pagina_dupla TINYINT(1) DEFAULT NULL,"
			+ "  id_vinculado_direita_pagina INT(11) DEFAULT NULL,"
			+ "  vinculado_esquerda_nome VARCHAR(250) DEFAULT NULL,"
			+ "  vinculado_esquerda_pasta VARCHAR(500) DEFAULT NULL,"
			+ "  vinculado_esquerda_pagina INT(11) DEFAULT NULL," + "  vinculado_esquerda_paginas INT(11) DEFAULT NULL,"
			+ "  vinculado_esquerda_pagina_dupla TINYINT(1) DEFAULT NULL,"
			+ "  id_vinculado_esquerda_paginas INT(11) DEFAULT NULL," + "  imagem_dupla TINYINT(1) DEFAULT NULL,"
			+ "  PRIMARY KEY (id)," + "  KEY %s_vinculo_fk (id_vinculo),"
			+ "  KEY %s_vinculado_original_pagina_fk (id_original_pagina),"
			+ "  KEY %s_vinculado_direita_pagina_fk (id_vinculado_direita_pagina),"
			+ "  KEY %s_vinculado_esquerda_pagina_fk (id_vinculado_esquerda_paginas),"
			+ "  CONSTRAINT %s_vinculado_direita_pagina_fk FOREIGN KEY (id_vinculado_direita_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculado_esquerda_pagina_fk FOREIGN KEY (id_vinculado_esquerda_paginas) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculado_original_pagina_fk FOREIGN KEY (id_original_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculo_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_TABELA_NAO_VINCULADOS = "CREATE TABLE %s_vinculo_pagina_nao_vinculado ("
			+ "  id INT(11) NOT NULL AUTO_INCREMENT," + "  id_vinculo INT(11) DEFAULT NULL,"
			+ "  nome VARCHAR(250) DEFAULT NULL," + "  pasta VARCHAR(500) DEFAULT NULL,"
			+ "  pagina INT(11) DEFAULT NULL," + "  paginas INT(11) DEFAULT NULL,"
			+ "  pagina_dupla TINYINT(1) DEFAULT NULL," + "  PRIMARY KEY (id),"
			+ "  KEY %s_vinculo_nao_vinculado_fk (id_vinculo),"
			+ "  CONSTRAINT %s_vinculo_nao_vinculado_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private static String INSERT = "INSERT IGNORE INTO Vinculo (kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	final private static String UPDATE = "UPDATE Vinculo SET tipo = ?, quantidade = ?, percentual = ?, media = ?, percMedia = ?, corSequencial = ? WHERE kanji = ? AND leitura = ? ;";
	final private static String DELETE = "DELETE FROM Vinculo WHERE kanji = ? AND leitura = ? ;";
	final private static String SELECT = "SELECT kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial FROM Vinculo WHERE kanji = ? AND leitura = ? ;";
	final private static String SELECT_KANJI = "SELECT kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial FROM Vinculo WHERE kanji = ? ;";
	final private static String SELECT_ALL = "SELECT kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial FROM Vinculo WHERE 1 > 0;";

	final private static String PESQUISA = "SELECT sequencia, word, readInfo, frequency, tabela FROM words_kanji_info WHERE word LIKE ";

	public VincularDaoJDBC(Connection conn) {
		this.conn = conn;
		Properties props = Configuracao.loadProperties();
		BASE_MANGA = props.getProperty("dataBase_manga") + ".";
	}

	@Override
	public Long insert(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			/*
			 * st = conn.prepareStatement(String.format(INSERT_TEXTO, BASE_MANGA + base),
			 * Statement.RETURN_GENERATED_KEYS);
			 * 
			 * st.setLong(1, idPagina); st.setInt(2, obj.getSequencia()); st.setString(3,
			 * obj.getTexto()); st.setInt(4, obj.getX1()); st.setInt(5, obj.getY1());
			 * st.setInt(6, obj.getX2()); st.setInt(7, obj.getY2());
			 */

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					return rs.getLong(1);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
		return null;
	}

	@Override
	public Vinculo select(String base, String manga, Integer volume, Language original, String arquivoOriginal,
			Language vinculado, String arquivoVinculado) throws ExcessaoBd {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vinculo select(String base, String manga, Integer volume, String original, String vinculado)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT);
			/*
			 * st.setString(1, kanji); st.setString(2, leitura);
			 */
			rs = st.executeQuery();

			/*
			 * if (rs.next()) { return new Vinculo(rs.getString("kanji"),
			 * rs.getString("tipo"), rs.getString("leitura"), rs.getDouble("quantidade"),
			 * rs.getFloat("percentual"), rs.getDouble("media"), rs.getFloat("percMedia"),
			 * rs.getInt("corSequencial")); }
			 */
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public Vinculo select(String base, String manga, Integer volume, Language original, Language vinculado)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT);
			/*
			 * st.setString(1, kanji); st.setString(2, leitura);
			 */
			rs = st.executeQuery();

			/*
			 * if (rs.next()) { return new Vinculo(rs.getString("kanji"),
			 * rs.getString("tipo"), rs.getString("leitura"), rs.getDouble("quantidade"),
			 * rs.getFloat("percentual"), rs.getDouble("media"), rs.getFloat("percMedia"),
			 * rs.getInt("corSequencial")); }
			 */
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public void delete(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(DELETE);

			// st.setString(1, obj.getKanji());
			// st.setString(2, obj.getLeitura());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public void update(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			/*
			 * st = conn.prepareStatement(String.format(UPDATE_PROCESSADO, BASE_MANGA +
			 * base, tabela), Statement.RETURN_GENERATED_KEYS);
			 */

			// st.setLong(1, id);
			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public Boolean createTabelas(String nome) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(EXIST_TABELA_VOCABULARIO, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), nome));
			rs = st.executeQuery();

			if (rs.next())
				return true;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		st = null;
		try {
			st = conn.prepareStatement(String.format(CREATE_TABELA_VINCULO, BASE_MANGA + nome, nome, nome, nome,
					BASE_MANGA + nome, nome, BASE_MANGA + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		st = null;
		try {
			st = conn.prepareStatement(String.format(CREATE_TABELA_VINCULO_PAGINA, BASE_MANGA + nome, nome, nome, nome,
					nome, nome, BASE_MANGA + nome, nome, BASE_MANGA + nome, nome, BASE_MANGA + nome, nome,
					BASE_MANGA + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		st = null;
		try {
			st = conn.prepareStatement(
					String.format(CREATE_TABELA_NAO_VINCULADOS, BASE_MANGA + nome, nome, nome, BASE_MANGA + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		return false;
	}

	@Override
	public List<String> getTabelas() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, BASE_MANGA.substring(0, BASE_MANGA.length() - 1)));
			rs = st.executeQuery();

			List<String> list = new ArrayList<>();

			while (rs.next())
				list.add(rs.getString("Tabela"));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}
