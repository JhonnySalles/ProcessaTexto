package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jisho.textosJapones.model.dao.MangaDao;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.MangaVocabulario;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.configuration.Configuracao;
import org.jisho.textosJapones.util.mysql.DB;

public class MangaDaoJDBC implements MangaDao {

	private Connection conn;
	private String BASE_MANGA;

	final private String CREATE_VOLUMES = "CREATE TABLE %s_volumes (id int(11) NOT NULL AUTO_INCREMENT, manga varchar(250) DEFAULT NULL, "
			+ "  volume int(4) DEFAULT NULL, linguagem varchar(4) DEFAULT NULL, vocabulario longtext, is_processado tinyint(1) DEFAULT '0', "
			+ "  PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	final private String CREATE_CAPITULOS = "CREATE TABLE %s_capitulos (id INT(11) NOT NULL AUTO_INCREMENT, id_volume INT(11) DEFAULT NULL, "
			+ "  manga LONGTEXT COLLATE utf8mb4_unicode_ci NOT NULL, volume INT(4) NOT NULL, "
			+ "  capitulo DOUBLE NOT NULL, linguagem VARCHAR(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL, scan VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL, "
			+ "  is_extra TINYINT(1) DEFAULT NULL, is_raw TINYINT(1) DEFAULT NULL, is_processado TINYINT(1) DEFAULT '0', vocabulario LONGTEXT COLLATE utf8mb4_unicode_ci, "
			+ "  PRIMARY KEY (id), KEY %s_volumes_fk (id_volume), "
			+ "  CONSTRAINT %s_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE "
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
	final private String CREATE_PAGINAS = "CREATE TABLE %s_paginas (id INT(11) NOT NULL AUTO_INCREMENT, id_capitulo INT(11) NOT NULL, "
			+ "  nome VARCHAR(250) DEFAULT NULL, numero INT(11) DEFAULT NULL, hash_pagina VARCHAR(250) DEFAULT NULL, is_processado TINYINT(1) DEFAULT '0', "
			+ "  vocabulario LONGTEXT, PRIMARY KEY (id), KEY %s_capitulos_fk (id_capitulo), "
			+ "  CONSTRAINT %s_capitulos_paginas_fk FOREIGN KEY (id_capitulo) REFERENCES %s_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE "
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8";
	final private String CREATE_TEXTO = "CREATE TABLE %s_textos (id INT(11) NOT NULL AUTO_INCREMENT, id_pagina INT(11) NOT NULL, "
			+ "  sequencia INT(4) DEFAULT NULL, texto LONGTEXT COLLATE utf8mb4_unicode_ci, posicao_x1 DOUBLE DEFAULT NULL, "
			+ "  posicao_y1 DOUBLE DEFAULT NULL, posicao_x2 DOUBLE DEFAULT NULL, posicao_y2 DOUBLE DEFAULT NULL, versaoApp INT(11) DEFAULT '0', PRIMARY KEY (id), "
			+ "  KEY %s_paginas_fk (id_pagina), "
			+ "  CONSTRAINT %s_paginas_textos_fk FOREIGN KEY (id_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE "
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_VOCABULARIO = "CREATE TABLE %s_vocabulario (" + "  id INT(11) NOT NULL AUTO_INCREMENT,"
			+ "  id_volume INT(11) DEFAULT NULL," + "  id_capitulo INT(11) DEFAULT NULL,"
			+ "  id_pagina INT(11) DEFAULT NULL," + "  palavra VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  significado LONGTEXT COLLATE utf8mb4_unicode_ci," + " revisado tinyint(1) DEFAULT 1," + "  PRIMARY KEY (id),"
			+ "  KEY %s_vocab_volume_fk (id_volume)," + "  KEY %s_vocab_capitulo_fk (id_capitulo),"
			+ "  KEY %s_vocab_pagina_fk (id_pagina),"
			+ "  CONSTRAINT %s_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES %s_capitulos (id),"
			+ "  CONSTRAINT %s_vocab_pagina_fk FOREIGN KEY (id_pagina) REFERENCES %s_paginas (id),"
			+ "  CONSTRAINT %s_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES %s_volumes (id)"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String UPDATE_VOLUMES = "UPDATE %s_volumes SET manga = ?, volume = ?, linguagem = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_CAPITULOS = "UPDATE %s_capitulos SET manga = ?, volume = ?, capitulo = ?, linguagem = ?, scan = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_PAGINAS = "UPDATE %s_paginas SET nome = ?, numero = ?, hash_pagina = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_TEXTO = "UPDATE %s_textos SET sequencia = ?, texto = ?, posicao_x1 = ?, posicao_y1 = ?, posicao_x2 = ?, posicao_y2 = ? WHERE id = ?";
	final private String UPDATE_PAGINAS_CANCEL = "UPDATE %s_paginas SET is_processado = 0 WHERE id = ?";

	final private String INSERT_VOLUMES = "INSERT INTO %s_volumes (manga, volume, linguagem, is_processado) VALUES (?,?,?,?)";
	final private String INSERT_CAPITULOS = "INSERT INTO %s_capitulos (id_volume, manga, volume, capitulo, linguagem, scan, is_extra, is_raw, is_processado) VALUES (?,?,?,?,?,?,?,?,?)";
	final private String INSERT_PAGINAS = "INSERT INTO %s_paginas (id_capitulo, nome, numero, hash_pagina, is_processado) VALUES (?,?,?,?,?)";
	final private String INSERT_TEXTO = "INSERT INTO %s_textos (id_pagina, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2) VALUES (?,?,?,?,?,?,?)";

	final private String SELECT_VOLUMES = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.is_Processado FROM %s_volumes VOL %s WHERE %s GROUP BY VOL.id";
	final private String SELECT_CAPITULOS = "SELECT CAP.id, CAP.manga, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.scan, CAP.is_extra, CAP.is_raw, CAP.is_processado "
			+ "FROM %s_capitulos CAP %s WHERE id_volume = ? AND %s GROUP BY CAP.id";
	final private String SELECT_PAGINAS = "SELECT id, nome, numero, hash_pagina, is_processado FROM %s_paginas WHERE id_capitulo = ? AND %s ";
	final private String SELECT_TEXTOS = "SELECT id, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2 FROM %s_textos WHERE id_pagina = ? ";

	final private String INNER_CAPITULOS = "INNER JOIN %s_paginas PAG ON CAP.id = PAG.id_capitulo AND PAG.is_processado = 0";
	final private String INNER_VOLUMES = "INNER JOIN %s_capitulos CAP ON VOL.id = CAP.id_volume "
			+ "INNER JOIN %s_paginas PAG ON CAP.id = PAG.id_capitulo AND PAG.is_processado = 0";

	final private String SELECT_TABELAS = "SELECT REPLACE(REPLACE(REPLACE(Table_Name, '_paginas', ''), '_capitulos', ''), '_volumes', '') AS Tabela "
			+ "FROM information_schema.tables WHERE table_schema = '%s' AND (Table_Name NOT LIKE '%%_textos%%' AND Table_Name NOT LIKE '%%exemplo%%' "
			+ "AND Table_Name NOT LIKE '%%vocabulario%%') AND %s GROUP BY Tabela ";

	final private String EXIST_TABELA_VOCABULARIO = "SELECT Table_Name AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_vocabulario%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ";

	final private String DELETE_VOCABULARIO = "DELETE FROM %s_vocabulario WHERE %s;";
	final private String INSERT_VOCABULARIO = "INSERT INTO %s_vocabulario (%s, palavra, significado, revisado) "
			+ " VALUES (?,?,?,?);";
	final private String SELECT_VOCABUALARIO = "SELECT id, palavra, significado, revisado FROM %s_vocabulario WHERE %s ";
	final private String UPDATE_PROCESSADO = "UPDATE %s_%s SET is_processado = 1 WHERE id = ?";

	public MangaDaoJDBC(Connection conn) {
		this.conn = conn;
		Properties props = Configuracao.loadProperties();
		BASE_MANGA = props.getProperty("dataBase_manga") + ".";
	}

	@Override
	public void updateVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_VOLUMES, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setString(3, obj.getLingua().getSigla());
			st.setBoolean(4, obj.getProcessado());
			st.setLong(5, obj.getId());

			insertVocabulario(base, obj.getId(), null, null, obj.getVocabulario());

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
	public void updateCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_CAPITULOS, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setFloat(3, obj.getCapitulo());
			st.setString(4, obj.getLingua().getSigla());
			st.setString(5, obj.getScan());
			st.setBoolean(6, obj.getProcessado());
			st.setLong(7, obj.getId());

			insertVocabulario(base, null, obj.getId(), null, obj.getVocabulario());

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
	public void insertVocabulario(String base, Long idVolume, Long idCapitulo, Long idPagina,
			Set<MangaVocabulario> vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			if (idVolume == null && idCapitulo == null && idPagina == null)
				return;

			String where = idVolume != null ? ("id_volume = " + idVolume)
					: idCapitulo != null ? ("id_capitulo = " + idCapitulo) : ("id_pagina = " + idPagina);
			clearVocabulario(base, where);

			String campo = idVolume != null ? "id_volume" : idCapitulo != null ? "id_capitulo" : "id_pagina";
			Long id = idVolume != null ? idVolume : idCapitulo != null ? idCapitulo : idPagina;

			for (MangaVocabulario vocab : vocabulario) {
				st = conn.prepareStatement(String.format(INSERT_VOCABULARIO, BASE_MANGA + base, campo),
						Statement.RETURN_GENERATED_KEYS);

				st.setLong(1, id);
				st.setString(2, vocab.getPalavra());
				st.setString(3, vocab.getSignificado());
				st.setBoolean(4, vocab.getRevisado());

				st.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	private void clearVocabulario(String base, String where) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(DELETE_VOCABULARIO, BASE_MANGA + base, where),
					Statement.RETURN_GENERATED_KEYS);

			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	private Set<MangaVocabulario> selectVocabulario(String base, String where) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VOCABUALARIO, BASE_MANGA + base, where));
			rs = st.executeQuery();

			Set<MangaVocabulario> list = new HashSet<MangaVocabulario>();

			while (rs.next())
				list.add(new MangaVocabulario(rs.getLong("id"), rs.getString("palavra"), rs.getString("significado"), rs.getBoolean("revisado")));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public void updatePagina(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PAGINAS, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getNomePagina());
			st.setInt(2, obj.getNumero());
			st.setString(3, obj.getHash());
			st.setBoolean(4, obj.getProcessado());
			st.setLong(5, obj.getId());

			insertVocabulario(base, null, null, obj.getId(), obj.getVocabulario());

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
	public void updateTexto(String base, MangaTexto obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_TEXTO, BASE_MANGA + base), Statement.RETURN_GENERATED_KEYS);

			st.setInt(1, obj.getSequencia());
			st.setString(2, obj.getTexto());
			st.setInt(3, obj.getX1());
			st.setInt(4, obj.getY1());
			st.setInt(5, obj.getX2());
			st.setInt(6, obj.getY2());
			st.setLong(7, obj.getId());

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
	public void updateCancel(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PAGINAS_CANCEL, BASE_MANGA + base));
			st.setLong(1, obj.getId());
			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE_CANCEL);
		} finally {
			DB.closeStatement(st);
		}
	}

	public List<MangaVolume> selectVolumesTransferir(String baseOrigem) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VOLUMES, baseOrigem, "", "1>0"));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")),
						selectCapitulosTransferir(baseOrigem, rs.getLong("id"))));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaVolume> selectVolumes(String base, Boolean todos) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_VOLUMES, base, base);

			st = conn.prepareStatement(
					String.format(SELECT_VOLUMES, BASE_MANGA + base, inner, "VOL.linguagem = 'ja' AND 1>0"));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")),
						selectVocabulario(base, "id_volume = " + rs.getLong("id")),
						selectCapitulos(base, todos, rs.getLong("id"))));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaVolume> selectVolumes(String base, Boolean todos, String manga, Integer volume, Float capitulo,
			Language linguagem, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_VOLUMES, BASE_MANGA + base, BASE_MANGA + base);

			String condicao = " 1>0 ";

			if (linguagem != null)
				condicao += " AND VOL.linguagem = '" + linguagem.getSigla() + "' ";

			if (manga != null && !manga.trim().isEmpty())
				condicao += " AND VOL.manga = " + '"' + manga.trim() + '"';

			if (volume != null && volume > 0)
				condicao += " AND VOL.volume = " + String.valueOf(volume);

			st = conn.prepareStatement(String.format(SELECT_VOLUMES, BASE_MANGA + base, inner, condicao));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")),
						selectVocabulario(base, "id_volume = " + rs.getLong("id")),
						selectCapitulos(base, todos, rs.getLong("id"), capitulo, linguagem, inverterTexto)));
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaCapitulo> selectCapitulosTransferir(String baseOrigem, Long idVolume) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, baseOrigem, "", "1>0"));
			st.setLong(1, idVolume);
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectPaginasTransferir(baseOrigem, rs.getLong("id"))));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaCapitulo> selectCapitulos(String base, Boolean todos, Long idVolume) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_CAPITULOS, base);

			st = conn.prepareStatement(
					String.format(SELECT_CAPITULOS, BASE_MANGA + base, inner, "CAP.linguagem = 'ja' AND 1>0"));
			st.setLong(1, idVolume);
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectVocabulario(base, "id_capitulo = " + rs.getLong("id")),
						selectPaginas(base, todos, rs.getLong("id"), false)));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaCapitulo> selectCapitulos(String base, Boolean todos, Long idVolume, Float capitulo,
			Language linguagem, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_CAPITULOS, BASE_MANGA + base);

			String condicao = " 1>0 ";

			if (linguagem != null)
				condicao += " AND CAP.linguagem = '" + linguagem.getSigla() + "' ";

			if (capitulo != null && capitulo > 0)
				condicao += " CAP.linguagem = 'ja' AND CAP.capitulo = " + String.valueOf(capitulo);

			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, BASE_MANGA + base, inner, condicao));
			st.setLong(1, idVolume);
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getBoolean("is_extra"), rs.getBoolean("is_raw"), rs.getBoolean("is_processado"),
						selectVocabulario(base, "id_capitulo = " + rs.getLong("id")),
						selectPaginas(base, todos, rs.getLong("id"), inverterTexto)));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaPagina> selectPaginasTransferir(String baseOrigem, Long idPagina) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_PAGINAS, baseOrigem, "1>0"));
			st.setLong(1, idPagina);
			rs = st.executeQuery();

			List<MangaPagina> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaPagina(rs.getLong("id"), rs.getString("nome"), rs.getInt("numero"),
						rs.getString("hash_pagina"), rs.getBoolean("is_processado"),
						selectTextosTransferir(baseOrigem, rs.getLong("id"))));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaPagina> selectPaginas(String base, Boolean todos, Long idPagina, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(
					String.format(SELECT_PAGINAS, BASE_MANGA + base, (todos ? "1>0" : "is_Processado = 0")));
			st.setLong(1, idPagina);
			rs = st.executeQuery();

			List<MangaPagina> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaPagina(rs.getLong("id"), rs.getString("nome"), rs.getInt("numero"),
						rs.getString("hash_pagina"), rs.getBoolean("is_processado"),
						selectTextos(base, rs.getLong("id"), inverterTexto)));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaTexto> selectTextosTransferir(String baseOrigem, Long idPagina) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_TEXTOS, baseOrigem));
			st.setLong(1, idPagina);
			rs = st.executeQuery();

			List<MangaTexto> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaTexto(rs.getLong("id"), rs.getString("texto"), rs.getInt("sequencia"),
						rs.getInt("posicao_x1"), rs.getInt("posicao_y1"), rs.getInt("posicao_x2"),
						rs.getInt("posicao_y2")));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaTexto> selectTextos(String base, Long idPagina, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			
			String order = "";
			if (inverterTexto)
				order = " ORDER BY sequencia DESC";

			st = conn.prepareStatement(String.format(SELECT_TEXTOS, BASE_MANGA + base) + order);
			st.setLong(1, idPagina);
			rs = st.executeQuery();

			List<MangaTexto> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaTexto(rs.getLong("id"), rs.getString("texto"), rs.getInt("sequencia"),
						rs.getInt("posicao_x1"), rs.getInt("posicao_y1"), rs.getInt("posicao_x2"),
						rs.getInt("posicao_y2")));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<MangaVolume> selectAll(String base) throws ExcessaoBd {
		return selectVolumes(base, true);
	}

	@Override
	public List<MangaVolume> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem)
			throws ExcessaoBd {
		return selectVolumes(base, true, manga, volume, capitulo, linguagem, false);
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), "1>0"));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), todos);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga) throws ExcessaoBd {
		return selectTabelas(todos, base, manga, 0, 0F);
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume) throws ExcessaoBd {
		return selectTabelas(todos, base, manga, volume, 0F);
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String condicao = "1>0 ";

			if (base != null && !base.trim().isEmpty())
				condicao += " AND Table_Name LIKE '%" + base.trim() + "%'";

			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), condicao));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				createBaseVocabulario(rs.getString("Tabela"));
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), todos, manga, volume, capitulo,
						Language.JAPANESE, false);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public Long insertVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_VOLUMES, base), Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setString(3, obj.getLingua().getSigla());
			st.setBoolean(4, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				Long id = null;
				if (rs.next())
					id = rs.getLong(1);

				insertVocabulario(base, id, null, null, obj.getVocabulario());

				return id;
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Long insertCapitulo(String base, Long idVolume, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_CAPITULOS, base), Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, idVolume);
			st.setString(2, obj.getManga());
			st.setInt(3, obj.getVolume());
			st.setFloat(4, obj.getCapitulo());
			st.setString(5, obj.getLingua().getSigla());
			st.setString(6, obj.getScan());
			st.setBoolean(7, obj.isExtra());
			st.setBoolean(8, obj.isRaw());
			st.setBoolean(9, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				Long id = null;
				if (rs.next())
					id = rs.getLong(1);

				insertVocabulario(base, null, id, null, obj.getVocabulario());

				return id;
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Long insertPagina(String base, Long idCapitulo, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_PAGINAS, base), Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, idCapitulo);
			st.setString(2, obj.getNomePagina());
			st.setInt(3, obj.getNumero());
			st.setString(4, obj.getHash());
			st.setBoolean(5, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				Long id = null;
				if (rs.next())
					id = rs.getLong(1);

				insertVocabulario(base, null, null, id, obj.getVocabulario());
				return id;
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Long insertTexto(String base, Long idPagina, MangaTexto obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT_TEXTO, base), Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, idPagina);
			st.setInt(2, obj.getSequencia());
			st.setString(3, obj.getTexto());
			st.setInt(4, obj.getX1());
			st.setInt(5, obj.getY1());
			st.setInt(6, obj.getX2());
			st.setInt(7, obj.getY2());

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
	public List<MangaVolume> selectTransferir(String baseOrigem) throws ExcessaoBd {
		return selectVolumesTransferir(baseOrigem);
	}

	@Override
	public void createDatabase(String baseDestino) throws ExcessaoBd {
		String base = BASE_MANGA;
		String nome = baseDestino.trim();
		if (nome.contains(".")) {
			nome = baseDestino.substring(baseDestino.indexOf(".")).replace(".", "");
			base = baseDestino.substring(0, baseDestino.indexOf(".")) + ".";
		}

		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(CREATE_VOLUMES, base + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		try {
			st = conn.prepareStatement(String.format(CREATE_CAPITULOS, base + nome, nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		try {
			st = conn.prepareStatement(String.format(CREATE_PAGINAS, base + nome, nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		try {
			st = conn.prepareStatement(String.format(CREATE_TEXTO, base + nome, nome, nome, base + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public void createBaseVocabulario(String nome) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(EXIST_TABELA_VOCABULARIO, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), nome));
			rs = st.executeQuery();

			if (rs.next())
				return;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		st = null;
		try {
			st = conn.prepareStatement(String.format(CREATE_VOCABULARIO, BASE_MANGA + nome, nome, nome, nome, nome,
					BASE_MANGA + nome, nome, BASE_MANGA + nome, nome, BASE_MANGA + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void updateProcessado(String base, String tabela, Long id) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PROCESSADO, BASE_MANGA + base, tabela),
					Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, id);
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
	public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo,
			Language linguagem, Boolean inverterTexto) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String condicao = "1>0 ";

			if (base != null && !base.trim().isEmpty())
				condicao += " AND Table_Name LIKE '%" + base.trim() + "%'";

			st = conn.prepareStatement(
					String.format(SELECT_TABELAS, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), condicao));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				createBaseVocabulario(rs.getString("Tabela"));
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), true, manga, volume, capitulo,
						linguagem, inverterTexto);
				if (volumes.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), volumes));
			}

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
