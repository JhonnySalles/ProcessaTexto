package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.MangaDao;
import org.jisho.textosJapones.database.dao.VincularDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.configuration.Configuracao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class VincularDaoJDBC implements VincularDao {

	private Connection conn;
	private String BASE_MANGA;

	private MangaDao mangaDao = DaoFactory.createMangaDao();

	final private String EXIST_TABELA_VOCABULARIO = "SELECT Table_Name AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_vocabulario%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ";

	final private String EXIST_TABELA_VINCULO = "SELECT Table_Name AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_vinculo%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ";

	final private String SELECT_MANGAS = "SELECT Manga FROM %s_volumes WHERE linguagem = '%s' GROUP BY manga ORDER BY manga";

	final private String CREATE_TABELA_VINCULO = "CREATE TABLE %s_vinculo (" + "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,"
			+ "  volume int(11) DEFAULT NULL," + "  original_arquivo VARCHAR(250) DEFAULT NULL,"
			+ "  original_linguagem VARCHAR(4) DEFAULT NULL," + "  id_volume_original VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  vinculado_arquivo VARCHAR(250) DEFAULT NULL," + "  vinculado_linguagem VARCHAR(4) DEFAULT NULL,"
			+ "  id_volume_vinculado VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL," + "  data_criacao DATETIME DEFAULT NULL,"
			+ "  ultima_alteracao DATETIME DEFAULT NULL," + "  PRIMARY KEY (id),"
			+ "  KEY %s_original_volume_fk (id_volume_original),"
			+ "  KEY %s_vinculado_volume_fk (id_volume_vinculado),"
			+ "  CONSTRAINT %s_vinculo_original_volume_fk FOREIGN KEY (id_volume_original) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculo_vinculado_volume_fk FOREIGN KEY (id_volume_vinculado) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_TABELA_VINCULO_PAGINA = "CREATE TABLE %s_vinculo_pagina ("
			+ "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL," + "  id_vinculo VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  original_nome VARCHAR(250) DEFAULT NULL," + "  original_pasta VARCHAR(500) DEFAULT NULL,"
			+ "  original_pagina INT(11) DEFAULT NULL," + "  original_paginas INT(11) DEFAULT NULL,"
			+ "  original_pagina_dupla TINYINT(1) DEFAULT NULL," + "  id_original_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  vinculado_direita_nome VARCHAR(250) DEFAULT NULL,"
			+ "  vinculado_direita_pasta VARCHAR(500) DEFAULT NULL,"
			+ "  vinculado_direita_pagina INT(11) DEFAULT NULL," + "  vinculado_direita_paginas INT(11) DEFAULT NULL,"
			+ "  vinculado_direita_pagina_dupla TINYINT(1) DEFAULT NULL,"
			+ "  id_vinculado_direita_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  vinculado_esquerda_nome VARCHAR(250) DEFAULT NULL,"
			+ "  vinculado_esquerda_pasta VARCHAR(500) DEFAULT NULL,"
			+ "  vinculado_esquerda_pagina INT(11) DEFAULT NULL," + "  vinculado_esquerda_paginas INT(11) DEFAULT NULL,"
			+ "  vinculado_esquerda_pagina_dupla TINYINT(1) DEFAULT NULL,"
			+ "  id_vinculado_esquerda_paginas VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL," + "  imagem_dupla TINYINT(1) DEFAULT NULL,"
			+ "  PRIMARY KEY (id)," + "  KEY %s_vinculo_fk (id_vinculo),"
			+ "  KEY %s_original_pagina_fk (id_original_pagina),"
			+ "  KEY %s_direita_pagina_fk (id_vinculado_direita_pagina),"
			+ "  KEY %s_esquerda_pagina_fk (id_vinculado_esquerda_paginas),"
			+ "  CONSTRAINT %s_vinculado_direita_pagina_fk FOREIGN KEY (id_vinculado_direita_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculado_esquerda_pagina_fk FOREIGN KEY (id_vinculado_esquerda_paginas) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculado_original_pagina_fk FOREIGN KEY (id_original_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculo_vinculo_pagina_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_TABELA_NAO_VINCULADOS = "CREATE TABLE %s_vinculo_pagina_nao_vinculado ("
			+ "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL," + "  id_vinculo VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  nome VARCHAR(250) DEFAULT NULL," + "  pasta VARCHAR(500) DEFAULT NULL,"
			+ "  pagina INT(11) DEFAULT NULL," + "  paginas INT(11) DEFAULT NULL,"
			+ "  pagina_dupla TINYINT(1) DEFAULT NULL," + "  id_vinculado_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
			+ "  PRIMARY KEY (id)," + "  KEY %s_vinculado_pagina_fk (id_vinculado_pagina),"
			+ "  KEY %s_vinculo_fk (id_vinculo),"
			+ "  CONSTRAINT %s_vinculo_nao_vinculado_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_nao_vinculado_pagina_fk FOREIGN KEY (id_vinculado_pagina) REFERENCES %s_vinculo_pagina (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private static String INSERT_VINCULO = "INSERT INTO %s_vinculo (volume, original_arquivo, original_linguagem, id_volume_original, vinculado_arquivo, vinculado_linguagem, id_volume_vinculado, data_criacao, ultima_alteracao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	final private static String UPDATE_VINCULO = "UPDATE %s_vinculo SET volume = ?, original_arquivo = ?, original_linguagem = ?, id_volume_original = ?, vinculado_arquivo = ?, vinculado_linguagem = ?, id_volume_vinculado = ?, ultima_alteracao = ? WHERE id = ? ;";
	final private static String DELETE_VINCULO = "DELETE FROM %s_vinculo WHERE id = ? ;";

	final private static String SELECT_ID_VOLUME = "SELECT id FROM %s_volumes WHERE manga = '%s' AND volume = %s AND linguagem = '%s' LIMIT 1";
	final private static String SELECT_VINCULO_CAMPOS = "SELECT vi.id, vi.volume, vi.original_arquivo, vi.original_linguagem, vi.id_volume_original, vi.vinculado_arquivo, vi.vinculado_linguagem, vi.id_volume_vinculado, vi.data_criacao, vi.ultima_alteracao FROM %s_vinculo vi ";
	final private static String SELECT_VINCULO = SELECT_VINCULO_CAMPOS;
	final private static String SELECT_VINCULO_ARQUIVO = SELECT_VINCULO_CAMPOS
			+ "WHERE volume = ? AND original_arquivo = ? AND vinculado_arquivo = ? ;";
	final private static String SELECT_VINCULO_LINGUAGEM = SELECT_VINCULO_CAMPOS
			+ "WHERE volume = ? AND original_linguagem = ? AND vinculado_linguagem = ? ;";

	final private static String SELECT_VINCULO_INNER_VOLUME = SELECT_VINCULO_CAMPOS
			+ "INNER JOIN %s_volumes vo ON vo.id = vi.id_volume_original " + "WHERE 1 > 0 ";

	final private static String INSERT_PAGINA = "INSERT IGNORE INTO %s_vinculo_pagina (id_vinculo, original_nome, original_pasta, original_pagina, original_paginas, original_pagina_dupla, id_original_pagina, "
			+ "  vinculado_direita_nome, vinculado_direita_pasta, vinculado_direita_pagina, vinculado_direita_paginas, vinculado_direita_pagina_dupla, id_vinculado_direita_pagina, "
			+ "  vinculado_esquerda_nome, vinculado_esquerda_pasta, vinculado_esquerda_pagina, vinculado_esquerda_paginas, vinculado_esquerda_pagina_dupla, id_vinculado_esquerda_paginas, "
			+ "  imagem_dupla) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	final private static String UPDATE_PAGINA = "UPDATE %s_vinculo_pagina SET id_vinculo = ?, original_nome = ?, original_pasta = ?, original_pagina = ?, original_paginas = ?, original_pagina_dupla = ?, id_original_pagina = ?, "
			+ "  vinculado_direita_nome = ?, vinculado_direita_pasta = ?, vinculado_direita_pagina = ?, vinculado_direita_paginas = ?, vinculado_direita_pagina_dupla = ?, id_vinculado_direita_pagina = ?,"
			+ "  vinculado_esquerda_nome = ?, vinculado_esquerda_pasta = ?, vinculado_esquerda_pagina = ?, vinculado_esquerda_paginas = ?, vinculado_esquerda_pagina_dupla = ?, id_vinculado_esquerda_paginas = ?,"
			+ "  imagem_dupla = ? WHERE id = ? ;";
	final private static String DELETE_PAGINA = "DELETE FROM %s_vinculo_pagina WHERE id_vinculo = ? ;";
	final private static String SELECT_PAGINA = "SELECT id, original_nome, original_pasta, original_pagina, original_paginas, original_pagina_dupla, id_original_pagina, \n"
			+ "  vinculado_direita_nome, vinculado_direita_pasta, vinculado_direita_pagina, vinculado_direita_paginas, vinculado_direita_pagina_dupla, id_vinculado_direita_pagina,\n"
			+ "  vinculado_esquerda_nome, vinculado_esquerda_pasta, vinculado_esquerda_pagina, vinculado_esquerda_paginas, vinculado_esquerda_pagina_dupla, id_vinculado_esquerda_paginas,\n"
			+ "  imagem_dupla FROM %s_vinculo_pagina WHERE id_vinculo = ? ;";

	final private static String INSERT_PAGINA_NAO_VINCULADA = "INSERT IGNORE INTO %s_vinculo_pagina_nao_vinculado (id_vinculo, nome, pasta, pagina, paginas, pagina_dupla, id_vinculado_pagina) VALUES (?, ?, ?, ?, ?, ?, ?);";
	final private static String DELETE_PAGINA_NAO_VINCULADA = "DELETE FROM %s_vinculo_pagina_nao_vinculado WHERE id_vinculo = ? ;";
	final private static String SELECT_PAGINA_NAO_VINCULADA = "SELECT id, nome, pasta, pagina, paginas, pagina_dupla, id_vinculado_pagina FROM %s_vinculo_pagina_nao_vinculado WHERE id_vinculo = ? ;";

	final private String SELECT_TABELAS = "SELECT REPLACE(Table_Name, '_vinculo', '') AS Tabela "
			+ "FROM information_schema.tables WHERE table_schema = '%s' AND Table_Name NOT LIKE '%%exemplo%%' "
			+ "AND Table_Name LIKE '%%_vinculo' AND %s GROUP BY Tabela ";

	public VincularDaoJDBC(Connection conn) {
		this.conn = conn;
		Properties props = Configuracao.loadProperties();
		BASE_MANGA = props.getProperty("base_manga") + ".";
	}

	private void insertVinculados(String base, UUID idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(INSERT_PAGINA, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, idVinculo.toString());
			st.setString(2, pagina.getOriginalNomePagina());
			st.setString(3, pagina.getOriginalPathPagina());
			st.setInt(4, pagina.getOriginalPagina());
			st.setInt(5, pagina.getOriginalPaginas());
			st.setBoolean(6, pagina.isOriginalPaginaDupla);
			if (pagina.getMangaPaginaOriginal() != null)
				st.setString(7, pagina.getMangaPaginaOriginal().getId().toString());
			else
				st.setNString(7, null);

			st.setString(8, pagina.getVinculadoDireitaNomePagina());
			st.setString(9, pagina.getVinculadoDireitaPathPagina());
			st.setInt(10, pagina.getVinculadoDireitaPagina());
			st.setInt(11, pagina.getVinculadoDireitaPaginas());
			st.setBoolean(12, pagina.isVinculadoDireitaPaginaDupla);

			if (pagina.getMangaPaginaDireita() != null)
				st.setString(13, pagina.getMangaPaginaDireita().getId().toString());
			else
				st.setNString(13, null);

			st.setString(14, pagina.getVinculadoEsquerdaNomePagina());
			st.setString(15, pagina.getVinculadoEsquerdaPathPagina());
			st.setInt(16, pagina.getVinculadoEsquerdaPagina());
			st.setInt(17, pagina.getVinculadoEsquerdaPaginas());
			st.setBoolean(18, pagina.isVinculadoEsquerdaPaginaDupla);

			if (pagina.getMangaPaginaEsquerda() != null)
				st.setString(19, pagina.getMangaPaginaEsquerda().getId().toString());
			else
				st.setNString(19, null);

			st.setBoolean(20, pagina.isImagemDupla);

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					pagina.setId(rs.getLong(1));

			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	private void insertNaoVinculados(String base, UUID idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(INSERT_PAGINA_NAO_VINCULADA, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, idVinculo.toString());

			st.setString(2, pagina.getVinculadoEsquerdaNomePagina());
			st.setString(3, pagina.getVinculadoEsquerdaPathPagina());
			st.setInt(4, pagina.getVinculadoEsquerdaPagina());
			st.setInt(5, pagina.getVinculadoEsquerdaPaginas());
			st.setBoolean(6, pagina.isVinculadoEsquerdaPaginaDupla);

			if (pagina.getMangaPaginaEsquerda() != null)
				st.setString(7, pagina.getMangaPaginaEsquerda().getId().toString());
			else
				st.setNString(7, null);

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					pagina.setId(rs.getLong(1));

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
	public UUID insert(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			conn.setAutoCommit(false);
			conn.beginRequest();
			st = conn.prepareStatement(String.format(INSERT_VINCULO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setInt(1, obj.getVolume());
			st.setString(2, obj.getNomeArquivoOriginal());
			st.setString(3, obj.getLinguagemOriginal().getSigla());
			st.setString(4, obj.getVolumeOriginal().getId().toString());
			st.setString(5, obj.getNomeArquivoVinculado());
			st.setString(6, obj.getLinguagemVinculado().getSigla());
			st.setString(7, obj.getVolumeVinculado().getId().toString());
			st.setTimestamp(8, Util.convertToTimeStamp(obj.getDataCriacao()));
			st.setTimestamp(9, Util.convertToTimeStamp(obj.getUltimaAlteracao()));

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					obj.setId(UUID.fromString(rs.getString(1)));

					for (VinculoPagina pagina : obj.getVinculados())
						insertVinculados(base, obj.getId(), pagina);

					for (VinculoPagina pagina : obj.getNaoVinculados())
						insertNaoVinculados(base, obj.getId(), pagina);

					return obj.getId();
				}
			}

			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(st);
		}
		return null;
	}

	private MangaVolume volumeOriginal = null;
	private MangaVolume volumeVinculado = null;

	private MangaPagina selectPagina(String base, UUID id, MangaVolume volume) throws ExcessaoBd {
		if (base == null || id == null)
			return null;

		MangaPagina pagina = null;
		if (volume != null) {
			Optional<MangaCapitulo> capitulo = volume.getCapitulos().stream()
					.filter(cp -> cp.getPaginas().stream().anyMatch(pg -> pg.getId().compareTo(id) == 0)).findFirst();

			if (capitulo.isPresent())
				pagina = capitulo.get().getPaginas().stream().filter(pg -> pg.getId().compareTo(id) == 0).findFirst()
						.get();
		}

		if (pagina == null)
			pagina = mangaDao.selectPagina(base, id);

		return pagina;
	}

	private List<VinculoPagina> selectVinculados(String base, UUID idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_PAGINA, BASE_MANGA + base));
			st.setString(1, idVinculo.toString());
			rs = st.executeQuery();

			List<VinculoPagina> list = new ArrayList<VinculoPagina>();
			while (rs.next()) {
				MangaPagina mangaPaginaOriginal = selectPagina(base, UUID.fromString(rs.getString("id_original_pagina")), volumeOriginal);
				MangaPagina mangaPaginaDireita = selectPagina(base, UUID.fromString(rs.getString("id_vinculado_direita_pagina")),
						volumeVinculado);
				MangaPagina mangaPaginaEsquerda = selectPagina(base, UUID.fromString(rs.getString("id_vinculado_esquerda_paginas")),
						volumeVinculado);

				list.add(new VinculoPagina(rs.getLong("id"), rs.getString("original_nome"),
						rs.getString("original_pasta"), rs.getInt("original_pagina"), rs.getInt("original_paginas"),
						rs.getBoolean("original_pagina_dupla"), rs.getString("vinculado_direita_nome"),
						rs.getString("vinculado_direita_pasta"), rs.getInt("vinculado_direita_pagina"),
						rs.getInt("vinculado_direita_paginas"), rs.getBoolean("vinculado_direita_pagina_dupla"),
						rs.getString("vinculado_esquerda_nome"), rs.getString("vinculado_esquerda_pasta"),
						rs.getInt("vinculado_esquerda_pagina"), rs.getInt("vinculado_esquerda_paginas"),
						rs.getBoolean("vinculado_esquerda_pagina_dupla"), mangaPaginaOriginal, mangaPaginaDireita,
						mangaPaginaEsquerda, rs.getBoolean("imagem_dupla"), false));
			}
			return list;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	private List<VinculoPagina> selectNaoVinculados(String base, UUID idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_PAGINA_NAO_VINCULADA, BASE_MANGA + base));
			st.setString(1, idVinculo.toString());
			rs = st.executeQuery();

			List<VinculoPagina> list = new ArrayList<VinculoPagina>();
			while (rs.next())
				list.add(new VinculoPagina(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5),
						rs.getBoolean(6), selectPagina(base, UUID.fromString(rs.getString(7)), volumeVinculado), true));

			return list;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	private MangaVolume selectVolume(String base, UUID id) throws ExcessaoBd {
		if (base == null || id == null)
			return null;

		return mangaDao.selectVolume(base, id);
	}

	@Override
	public Vinculo select(String base, UUID id) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_VINCULO, BASE_MANGA + base) + " WHERE id = ? ");
			st.setString(1, id.toString());

			rs = st.executeQuery();

			if (rs.next()) {
				volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)));
				volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)));
				Vinculo obj = new Vinculo(UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, Util.convertToDateTime(rs.getTimestamp(9)),
						Util.convertToDateTime(rs.getTimestamp(10)));
				obj.setVinculados(selectVinculados(base, obj.getId()));
				obj.setNaoVinculados(selectNaoVinculados(base, obj.getId()));

				return obj;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			volumeOriginal = null;
			volumeVinculado = null;
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public Vinculo select(String base, Integer volume, String mangaOriginal, Language original, String arquivoOriginal,
			String mangaVinculado, Language vinculado, String arquivoVinculado) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String sql = String.format(SELECT_VINCULO, BASE_MANGA + base) + " WHERE volume = ? ";

			if (!mangaOriginal.isEmpty() && original != null)
				sql += " AND id_volume_original = ("
						+ String.format(SELECT_ID_VOLUME, BASE_MANGA + base, mangaOriginal, volume, original.getSigla())
						+ ")";

			if (!mangaVinculado.isEmpty() && vinculado != null)
				sql += " AND id_volume_vinculado = (" + String.format(SELECT_ID_VOLUME, BASE_MANGA + base,
						mangaVinculado, volume, vinculado.getSigla()) + ")";

			if (original != null)
				sql += " AND original_linguagem = '" + original.getSigla() + "'";

			if (vinculado != null)
				sql += " AND vinculado_linguagem = '" + vinculado.getSigla() + "'";

			if (!arquivoOriginal.isEmpty())
				sql += " AND original_arquivo = '" + arquivoOriginal + "'";

			if (!arquivoVinculado.isEmpty())
				sql += " AND vinculado_arquivo = '" + arquivoVinculado + "'";

			st = conn.prepareStatement(sql);

			st.setInt(1, volume);

			rs = st.executeQuery();

			if (rs.next()) {
				volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)));
				volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)));
				Vinculo obj = new Vinculo(UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, Util.convertToDateTime(rs.getTimestamp(9)),
						Util.convertToDateTime(rs.getTimestamp(10)));
				obj.setVinculados(selectVinculados(base, obj.getId()));
				obj.setNaoVinculados(selectNaoVinculados(base, obj.getId()));

				return obj;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			volumeOriginal = null;
			volumeVinculado = null;
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public Vinculo select(String base, Integer volume, String mangaOriginal, String arquivoOriginal,
			String mangaVinculado, String arquivoVinculado) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VINCULO_ARQUIVO, BASE_MANGA + base));

			st.setInt(1, volume);
			st.setString(2, arquivoOriginal);
			st.setString(3, arquivoVinculado);

			rs = st.executeQuery();

			if (rs.next()) {
				volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)));
				volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)));
				Vinculo obj = new Vinculo(UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, Util.convertToDateTime(rs.getTimestamp(9)),
						Util.convertToDateTime(rs.getTimestamp(10)));
				obj.setVinculados(selectVinculados(base, obj.getId()));
				obj.setNaoVinculados(selectNaoVinculados(base, obj.getId()));

				return obj;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			volumeOriginal = null;
			volumeVinculado = null;
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public Vinculo select(String base, Integer volume, String mangaOriginal, Language original, String mangaVinculado,
			Language vinculado) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VINCULO_LINGUAGEM, BASE_MANGA + base));

			st.setInt(1, volume);
			st.setString(2, original.getSigla());
			st.setString(3, vinculado.getSigla());

			rs = st.executeQuery();

			if (rs.next()) {
				volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)));
				volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)));
				Vinculo obj = new Vinculo(UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, Util.convertToDateTime(rs.getTimestamp(9)),
						Util.convertToDateTime(rs.getTimestamp(10)));
				obj.setVinculados(selectVinculados(base, obj.getId()));
				obj.setNaoVinculados(selectNaoVinculados(base, obj.getId()));

				return obj;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			volumeOriginal = null;
			volumeVinculado = null;
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	public List<Vinculo> select(String base, String manga, Integer volume, Float capitulo, Language linguagem,
			Boolean isValidaTabela) throws ExcessaoBd {
		if (isValidaTabela && !existTabelaVinculo(base))
			return new ArrayList<Vinculo>();

		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String sql = String.format(SELECT_VINCULO_INNER_VOLUME, BASE_MANGA + base, BASE_MANGA + base);

			if (manga != null && !manga.trim().isEmpty())
				sql += " AND vo.manga LIKE '" + manga + "'";

			if (volume != null && volume > 0)
				sql += " AND vo.volume = " + volume;

			/*
			 * if (capitulo != null && capitulo > -1) sql +=
			 * " AND vo.vinculado_linguagem = '" + vinculado.getSigla() + "'";
			 */

			if (linguagem != null)
				sql += " AND vo.linguagem = '" + linguagem.getSigla() + "'";

			st = conn.prepareStatement(sql);

			rs = st.executeQuery();

			List<Vinculo> list = new ArrayList<Vinculo>();
			while (rs.next()) {
				volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)));
				volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)));
				Vinculo obj = new Vinculo(UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, Util.convertToDateTime(rs.getTimestamp(9)),
						Util.convertToDateTime(rs.getTimestamp(10)));
				obj.setVinculados(selectVinculados(base, obj.getId()));
				obj.setNaoVinculados(selectNaoVinculados(base, obj.getId()));

				list.add(obj);
			}

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			volumeOriginal = null;
			volumeVinculado = null;
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private void deleteVinculado(String base, UUID idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(DELETE_PAGINA, BASE_MANGA + base));
			st.setString(1, idVinculo.toString());
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

	private void deleteNaoVinculado(String base, UUID idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(DELETE_PAGINA_NAO_VINCULADA, BASE_MANGA + base));
			st.setString(1, idVinculo.toString());
			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void delete(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			conn.setAutoCommit(false);
			conn.beginRequest();
			deleteVinculado(base, obj.getId());
			deleteNaoVinculado(base, obj.getId());

			st = conn.prepareStatement(String.format(DELETE_VINCULO, BASE_MANGA + base));

			st.setString(1, obj.getId().toString());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(st);
		}

	}

	private void updateVinculados(String base, UUID idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(UPDATE_PAGINA, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, idVinculo.toString());
			st.setString(2, pagina.getOriginalNomePagina());
			st.setString(3, pagina.getOriginalPathPagina());
			st.setInt(4, pagina.getOriginalPagina());
			st.setInt(5, pagina.getOriginalPaginas());
			st.setBoolean(6, pagina.isOriginalPaginaDupla);

			if (pagina.getMangaPaginaOriginal() != null)
				st.setString(7, pagina.getMangaPaginaOriginal().getId().toString());
			else
				st.setNString(7, null);

			st.setString(8, pagina.getVinculadoDireitaNomePagina());
			st.setString(9, pagina.getVinculadoDireitaPathPagina());
			st.setInt(10, pagina.getVinculadoDireitaPagina());
			st.setInt(11, pagina.getVinculadoDireitaPaginas());
			st.setBoolean(12, pagina.isVinculadoDireitaPaginaDupla);

			if (pagina.getMangaPaginaDireita() != null)
				st.setString(13, pagina.getMangaPaginaDireita().getId().toString());
			else
				st.setNString(13, null);

			st.setString(14, pagina.getVinculadoEsquerdaNomePagina());
			st.setString(15, pagina.getVinculadoEsquerdaPathPagina());
			st.setInt(16, pagina.getVinculadoEsquerdaPagina());
			st.setInt(17, pagina.getVinculadoEsquerdaPaginas());
			st.setBoolean(18, pagina.isVinculadoEsquerdaPaginaDupla);

			if (pagina.getMangaPaginaEsquerda() != null)
				st.setString(19, pagina.getMangaPaginaEsquerda().getId().toString());
			else
				st.setNString(19, null);

			st.setBoolean(20, pagina.isImagemDupla);
			st.setLong(21, pagina.getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					pagina.setId(rs.getLong(1));

			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
	}

	private void updateNaoVinculados(String base, UUID idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		insertNaoVinculados(base, idVinculo, pagina);
	}

	@Override
	public void update(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			conn.setAutoCommit(false);
			conn.beginRequest();
			st = conn.prepareStatement(String.format(UPDATE_VINCULO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setInt(1, obj.getVolume());
			st.setString(2, obj.getNomeArquivoOriginal());
			st.setString(3, obj.getLinguagemOriginal().getSigla());
			st.setString(4, obj.getVolumeOriginal().getId().toString());
			st.setString(5, obj.getNomeArquivoVinculado());
			st.setString(6, obj.getLinguagemVinculado().getSigla());
			st.setString(7, obj.getVolumeVinculado().getId().toString());
			st.setTimestamp(8, Util.convertToTimeStamp(obj.getUltimaAlteracao()));
			st.setString(9, obj.getId().toString());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			} else {
				for (VinculoPagina pagina : obj.getVinculados())
					updateVinculados(base, obj.getId(), pagina);

				deleteNaoVinculado(base, obj.getId());
				for (VinculoPagina pagina : obj.getNaoVinculados())
					updateNaoVinculados(base, obj.getId(), pagina);
			}

			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			DB.closeStatement(st);
		}

	}

	private Boolean existTabelaVinculo(String nome) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					String.format(EXIST_TABELA_VINCULO, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), nome));
			rs = st.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
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

			if (!rs.next())
				return false;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		if (existTabelaVinculo(nome))
			return false;

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
			st = conn.prepareStatement(String.format(CREATE_TABELA_NAO_VINCULADOS, BASE_MANGA + nome, nome, nome, nome,
					BASE_MANGA + nome, nome, BASE_MANGA + nome));
			st.execute();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		return true;
	}

	@Override
	public List<String> getTabelas() throws ExcessaoBd {
		return mangaDao.getTabelas();
	}

	@Override
	public List<String> getMangas(String base, Language linguagem) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_MANGAS, BASE_MANGA + base, linguagem.getSigla()));
			rs = st.executeQuery();

			List<String> list = new ArrayList<>();

			while (rs.next())
				list.add(rs.getString("Manga"));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private Integer sequencia;

	private List<MangaVinculo> selectVinculo(String base, String manga, Integer volume, Float capitulo,
                                             Language linguagem, Boolean isValidaTabela) throws ExcessaoBd {
		final List<Vinculo> vinculos = select(base, manga, volume, capitulo, linguagem, isValidaTabela);

		vinculos.stream().forEach(vol -> {
			vol.getVolumeOriginal().getCapitulos().parallelStream().forEach(c -> {
				c.getPaginas().parallelStream().forEach(p -> p.setSequencia(-1));
			});
			vol.getVolumeVinculado().getCapitulos().parallelStream().forEach(c -> {
				c.getPaginas().parallelStream().forEach(p -> p.setSequencia(-1));
			});
		});

		vinculos.stream().forEach(vol -> {
			sequencia = 0;
			vol.getVinculados().stream().forEach(pg -> {
				sequencia++;
				if (pg.getMangaPaginaOriginal() != null)
					pg.getMangaPaginaOriginal().setSequencia(sequencia);

				if (pg.getMangaPaginaEsquerda() != null)
					pg.getMangaPaginaEsquerda().setSequencia(sequencia);

				if (pg.getMangaPaginaDireita() != null)
					pg.getMangaPaginaDireita().setSequencia(sequencia);
			});
		});

		final List<MangaVinculo> mangas = new ArrayList<MangaVinculo>();
		final Set<MangaVolume> volumes = new HashSet<MangaVolume>();
		vinculos.parallelStream().forEach(it -> volumes.add(it.getVolumeOriginal()));

		volumes.parallelStream().forEach(vol -> {
			List<MangaVolume> vinculados = vinculos.parallelStream().filter(it -> it.getVolumeOriginal().equals(vol))
					.map(m -> m.getVolumeVinculado()).collect(Collectors.toList());

			mangas.add(new MangaVinculo(vol, vinculados));
		});

		return mangas;
	}

	@Override
	public List<MangaVinculo> selectVinculo(String base, String manga, Integer volume, Float capitulo,
                                            Language linguagem) throws ExcessaoBd {
		return selectVinculo(base, manga, volume, capitulo, linguagem, true);
	}

	@Override
	public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo,
                                               Language linguagem) throws ExcessaoBd {
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
				List<MangaVinculo> vinculo = selectVinculo(rs.getString("Tabela"), manga, volume, capitulo, linguagem,
						false);
				if (vinculo.size() > 0)
					list.add(new MangaTabela(rs.getString("Tabela"), null, vinculo));
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
