package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.MangaDao;
import org.jisho.textosJapones.model.dao.VincularDao;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.configuration.Configuracao;
import org.jisho.textosJapones.util.mysql.DB;

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

	final private String SELECT_TABELAS = "SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
			+ " FROM information_schema.tables WHERE table_schema = '%s' "
			+ " AND Table_Name LIKE '%%_volumes%%' GROUP BY Tabela ";

	final private String SELECT_MANGAS = "SELECT Manga FROM %s_volumes WHERE linguagem = '%s' GROUP BY manga ORDER BY manga";

	final private String CREATE_TABELA_VINCULO = "CREATE TABLE %s_vinculo (" + "  id INT(11) NOT NULL AUTO_INCREMENT,"
			+ "  volume int(11) DEFAULT NULL," + "  original_arquivo VARCHAR(250) DEFAULT NULL,"
			+ "  original_linguagem VARCHAR(4) DEFAULT NULL," + "  id_volume_original INT(11) DEFAULT NULL,"
			+ "  vinculado_arquivo VARCHAR(250) DEFAULT NULL," + "  vinculado_linguagem VARCHAR(4) DEFAULT NULL,"
			+ "  id_volume_vinculado INT(11) DEFAULT NULL," + "  data_criacao DATETIME DEFAULT NULL,"
			+ "  ultima_alteracao DATETIME DEFAULT NULL," + "  PRIMARY KEY (id),"
			+ "  KEY %s_original_volume_fk (id_volume_original),"
			+ "  KEY %s_vinculado_volume_fk (id_volume_vinculado),"
			+ "  CONSTRAINT %s_vinculo_original_volume_fk FOREIGN KEY (id_volume_original) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculo_vinculado_volume_fk FOREIGN KEY (id_volume_vinculado) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE"
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
			+ "  KEY %s_original_pagina_fk (id_original_pagina),"
			+ "  KEY %s_direita_pagina_fk (id_vinculado_direita_pagina),"
			+ "  KEY %s_esquerda_pagina_fk (id_vinculado_esquerda_paginas),"
			+ "  CONSTRAINT %s_vinculado_direita_pagina_fk FOREIGN KEY (id_vinculado_direita_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculado_esquerda_pagina_fk FOREIGN KEY (id_vinculado_esquerda_paginas) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculado_original_pagina_fk FOREIGN KEY (id_original_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_vinculo_vinculo_pagina_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private String CREATE_TABELA_NAO_VINCULADOS = "CREATE TABLE %s_vinculo_pagina_nao_vinculado ("
			+ "  id INT(11) NOT NULL AUTO_INCREMENT," + "  id_vinculo INT(11) DEFAULT NULL,"
			+ "  nome VARCHAR(250) DEFAULT NULL," + "  pasta VARCHAR(500) DEFAULT NULL,"
			+ "  pagina INT(11) DEFAULT NULL," + "  paginas INT(11) DEFAULT NULL,"
			+ "  pagina_dupla TINYINT(1) DEFAULT NULL," + "  id_vinculado_pagina int(11) DEFAULT NULL,"
			+ "  PRIMARY KEY (id)," + "  KEY %s_vinculado_pagina_fk (id_vinculado_pagina),"
			+ "  KEY %s_vinculo_fk (id_vinculo),"
			+ "  CONSTRAINT %s_vinculo_nao_vinculado_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE,"
			+ "  CONSTRAINT %s_nao_vinculado_pagina_fk FOREIGN KEY (id_vinculado_pagina) REFERENCES %s_vinculo_pagina (id) ON DELETE CASCADE ON UPDATE CASCADE"
			+ ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

	final private static String INSERT_VINCULO = "INSERT INTO %s_vinculo (volume, original_arquivo, original_linguagem, id_volume_original, vinculado_arquivo, vinculado_linguagem, id_volume_vinculado, data_criacao, ultima_alteracao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	final private static String UPDATE_VINCULO = "UPDATE %s_vinculo SET volume = ?, original_arquivo = ?, original_linguagem = ?, id_volume_original = ?, vinculado_arquivo = ?, vinculado_linguagem = ?, id_volume_vinculado = ?, ultima_alteracao = ? WHERE id = ? ;";
	final private static String DELETE_VINCULO = "DELETE FROM %s_vinculo WHERE id = ? ;";
	final private static String SELECT_VINCULO_CAMPOS = "SELECT id, volume, original_arquivo, original_linguagem, volume, id_volume_original, vinculado_arquivo, vinculado_linguagem, id_volume_vinculado, data_criacao, ultima_alteracao FROM %s_vinculo ";
	final private static String SELECT_VINCULO = SELECT_VINCULO_CAMPOS
			+ "WHERE volume = ? AND original_arquivo = ? AND original_linguagem = ? AND vinculado_arquivo = ? AND vinculado_linguagem = ? ;";
	final private static String SELECT_VINCULO_ARQUIVO = SELECT_VINCULO_CAMPOS
			+ "WHERE volume = ? AND original_arquivo = ? AND vinculado_arquivo = ? ;";
	final private static String SELECT_VINCULO_LINGUAGEM = SELECT_VINCULO_CAMPOS
			+ "WHERE volume = ? AND original_linguagem = ? AND vinculado_linguagem = ? ;";

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

	final private static String INSERT_PAGINA_NAO_VINCULADA = "INSERT IGNORE INTO Vinculo (id_vinculo, nome, pasta, pagina, paginas, pagina_dupla, id_vinculado_pagina) VALUES (?, ?, ?, ?, ?, ?, ?);";
	final private static String DELETE_PAGINA_NAO_VINCULADA = "DELETE FROM %s_vinculo_pagina_nao_vinculado WHERE id_vinculo = ? ;";
	final private static String SELECT_PAGINA_NAO_VINCULADA = "SELECT id, id_vinculo, nome, pasta, pagina, paginas, pagina_dupla, id_vinculado_pagina FROM Vinculo WHERE id_vinculo = ? ;";

	public VincularDaoJDBC(Connection conn) {
		this.conn = conn;
		Properties props = Configuracao.loadProperties();
		BASE_MANGA = props.getProperty("dataBase_manga") + ".";
	}

	private void insertVinculados(String base, Long idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(INSERT_PAGINA, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, idVinculo);
			st.setString(2, pagina.getOriginalNomePagina());
			st.setString(3, pagina.getOriginalPathPagina());
			st.setInt(4, pagina.getOriginalPagina());
			st.setInt(5, pagina.getOriginalPaginas());
			st.setBoolean(6, pagina.isOriginalPaginaDupla);
			st.setLong(7, pagina.getMangaPaginaOriginal().getId());

			st.setString(8, pagina.getVinculadoDireitaNomePagina());
			st.setString(9, pagina.getVinculadoDireitaPathPagina());
			st.setInt(10, pagina.getVinculadoDireitaPagina());
			st.setInt(11, pagina.getVinculadoDireitaPaginas());
			st.setBoolean(12, pagina.isVinculadoDireitaPaginaDupla);

			if (pagina.getMangaPaginaDireita() != null)
				st.setLong(13, pagina.getMangaPaginaDireita().getId());
			else
				st.setNString(13, null);

			st.setString(14, pagina.getVinculadoEsquerdaNomePagina());
			st.setString(15, pagina.getVinculadoEsquerdaPathPagina());
			st.setInt(16, pagina.getVinculadoEsquerdaPagina());
			st.setInt(17, pagina.getVinculadoEsquerdaPaginas());
			st.setBoolean(18, pagina.isVinculadoEsquerdaPaginaDupla);

			if (pagina.getMangaPaginaEsquerda() != null)
				st.setLong(19, pagina.getMangaPaginaEsquerda().getId());
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

	private void insertNaoVinculados(String base, Long idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(INSERT_PAGINA_NAO_VINCULADA, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, idVinculo);

			st.setString(2, pagina.getVinculadoEsquerdaNomePagina());
			st.setString(3, pagina.getVinculadoEsquerdaPathPagina());
			st.setInt(4, pagina.getVinculadoEsquerdaPagina());
			st.setInt(5, pagina.getVinculadoEsquerdaPaginas());
			st.setBoolean(6, pagina.isVinculadoEsquerdaPaginaDupla);

			if (pagina.getMangaPaginaEsquerda() != null)
				st.setLong(7, pagina.getMangaPaginaEsquerda().getId());
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
	public Long insert(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(INSERT_VINCULO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setInt(1, obj.getVolume());
			st.setString(2, obj.getNomeArquivoOriginal());
			st.setString(3, obj.getLinguagemOriginal().getSigla());
			st.setLong(4, obj.getVolumeOriginal().getId());
			st.setString(5, obj.getNomeArquivoVinculado());
			st.setString(6, obj.getLinguagemVinculado().getSigla());
			st.setLong(7, obj.getVolumeOriginal().getId());
			st.setTimestamp(8, Timestamp.valueOf(obj.getDataCriacao()));
			st.setTimestamp(9, Timestamp.valueOf(obj.getUltimaAlteracao()));

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					Long id = rs.getLong(1);

					for (VinculoPagina pagina : obj.getVinculados())
						insertVinculados(base, id, pagina);

					for (VinculoPagina pagina : obj.getNaoVinculados())
						insertNaoVinculados(base, id, pagina);

					return id;
				}
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

	private MangaVolume volumeOriginal = null;
	private MangaVolume volumeVinculado = null;

	private MangaPagina selectPagina(String base, Long id, MangaVolume volume) throws ExcessaoBd {
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

	private List<VinculoPagina> selectVinculados(String base, Long idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_PAGINA, BASE_MANGA + base));
			st.setLong(1, idVinculo);
			rs = st.executeQuery();

			List<VinculoPagina> list = new ArrayList<VinculoPagina>();
			while (rs.next()) {
				MangaPagina mangaPaginaOriginal = selectPagina(base, rs.getLong("id_original_pagina"), volumeOriginal);
				MangaPagina mangaPaginaDireita = selectPagina(base, rs.getLong("id_vinculado_direita_pagina"),
						volumeVinculado);
				MangaPagina mangaPaginaEsquerda = selectPagina(base, rs.getLong("id_vinculado_esquerda_paginas"),
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

	private List<VinculoPagina> selectNaoVinculados(String base, Long idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_PAGINA_NAO_VINCULADA, BASE_MANGA + base));
			st.setLong(1, idVinculo);
			rs = st.executeQuery();

			List<VinculoPagina> list = new ArrayList<VinculoPagina>();
			while (rs.next())
				list.add(new VinculoPagina(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5),
						rs.getBoolean(6), selectPagina(base, rs.getLong(7), volumeVinculado), true));

			return list;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	private MangaVolume selectVolume(String base, Long id) throws ExcessaoBd {
		if (base == null || id == null)
			return null;

		return mangaDao.selectVolume(base, id);
	}

	@Override
	public Vinculo select(String base, Integer volume, String mangaOriginal, Language original, String arquivoOriginal,
			String mangaVinculado, Language vinculado, String arquivoVinculado) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VINCULO, BASE_MANGA + base));

			st.setInt(1, volume);
			st.setString(2, arquivoOriginal);
			st.setString(3, original.getSigla());
			st.setString(4, arquivoVinculado);
			st.setString(5, vinculado.getSigla());

			rs = st.executeQuery();

			if (rs.next()) {
				volumeOriginal = selectVolume(base, rs.getLong(5));
				volumeVinculado = selectVolume(base, rs.getLong(8));
				Vinculo obj = new Vinculo(rs.getLong(1), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, LocalDateTime.parse(rs.getString(9)),
						LocalDateTime.parse(rs.getString(10)));
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
				volumeOriginal = selectVolume(base, rs.getLong(5));
				volumeVinculado = selectVolume(base, rs.getLong(8));
				Vinculo obj = new Vinculo(rs.getLong(1), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, LocalDateTime.parse(rs.getString(9)),
						LocalDateTime.parse(rs.getString(10)));
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
				volumeOriginal = selectVolume(base, rs.getLong(5));
				volumeVinculado = selectVolume(base, rs.getLong(8));
				Vinculo obj = new Vinculo(rs.getLong(1), base, rs.getInt(2), rs.getString(3),
						Language.getEnum(rs.getString(4)), volumeOriginal, rs.getString(6),
						Language.getEnum(rs.getString(7)), volumeVinculado, LocalDateTime.parse(rs.getString(9)),
						LocalDateTime.parse(rs.getString(10)));
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

	private void deleteVinculado(String base, Long idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(DELETE_PAGINA, BASE_MANGA + base));

			st.setLong(1, idVinculo);

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

	private void deleteNaoVinculado(String base, Long idVinculo) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(DELETE_PAGINA_NAO_VINCULADA, BASE_MANGA + base));

			st.setLong(1, idVinculo);

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
	public void delete(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			deleteVinculado(base, obj.getId());
			deleteNaoVinculado(base, obj.getId());

			st = conn.prepareStatement(String.format(DELETE_VINCULO, BASE_MANGA + base));

			st.setLong(1, obj.getId());

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

	private void updateVinculados(String base, Long idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		PreparedStatement st = null;
		try {

			st = conn.prepareStatement(String.format(UPDATE_PAGINA, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setLong(1, idVinculo);
			st.setString(2, pagina.getOriginalNomePagina());
			st.setString(3, pagina.getOriginalPathPagina());
			st.setInt(4, pagina.getOriginalPagina());
			st.setInt(5, pagina.getOriginalPaginas());
			st.setBoolean(6, pagina.isOriginalPaginaDupla);
			st.setLong(7, pagina.getMangaPaginaOriginal().getId());

			st.setString(8, pagina.getVinculadoDireitaNomePagina());
			st.setString(9, pagina.getVinculadoDireitaPathPagina());
			st.setInt(10, pagina.getVinculadoDireitaPagina());
			st.setInt(11, pagina.getVinculadoDireitaPaginas());
			st.setBoolean(12, pagina.isVinculadoDireitaPaginaDupla);

			if (pagina.getMangaPaginaDireita() != null)
				st.setLong(13, pagina.getMangaPaginaDireita().getId());
			else
				st.setNString(13, null);

			st.setString(14, pagina.getVinculadoEsquerdaNomePagina());
			st.setString(15, pagina.getVinculadoEsquerdaPathPagina());
			st.setInt(16, pagina.getVinculadoEsquerdaPagina());
			st.setInt(17, pagina.getVinculadoEsquerdaPaginas());
			st.setBoolean(18, pagina.isVinculadoEsquerdaPaginaDupla);

			if (pagina.getMangaPaginaEsquerda() != null)
				st.setLong(19, pagina.getMangaPaginaEsquerda().getId());
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

	private void updateNaoVinculados(String base, Long idVinculo, VinculoPagina pagina) throws ExcessaoBd {
		insertNaoVinculados(base, idVinculo, pagina);
	}

	@Override
	public void update(String base, Vinculo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_VINCULO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setInt(1, obj.getVolume());
			st.setString(2, obj.getNomeArquivoOriginal());
			st.setString(3, obj.getLinguagemOriginal().getSigla());
			st.setLong(4, obj.getVolumeOriginal().getId());
			st.setString(5, obj.getNomeArquivoVinculado());
			st.setString(6, obj.getLinguagemVinculado().getSigla());
			st.setLong(7, obj.getVolumeOriginal().getId());
			st.setTimestamp(8, Timestamp.valueOf(obj.getUltimaAlteracao()));
			st.setLong(9, obj.getId());

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

			if (!rs.next())
				return false;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
		} finally {
			DB.closeStatement(st);
		}

		try {
			st = conn.prepareStatement(
					String.format(EXIST_TABELA_VINCULO, BASE_MANGA.substring(0, BASE_MANGA.length() - 1), nome));
			rs = st.executeQuery();

			if (rs.next())
				return false;
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
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_TABELAS, BASE_MANGA.substring(0, BASE_MANGA.length() - 1)));
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

}
