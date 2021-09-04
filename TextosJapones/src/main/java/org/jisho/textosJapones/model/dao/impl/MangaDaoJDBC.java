package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jisho.textosJapones.model.dao.MangaDao;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.configuration.Configuracao;
import org.jisho.textosJapones.util.mysql.DB;

public class MangaDaoJDBC implements MangaDao {

	private Connection conn;
	private String BASE_MANGA;

	final private String UPDATE_VOLUMES = "UPDATE %s_volumes SET manga = ?, volume = ?, linguagem = ?, vocabulario = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_CAPITULOS = "UPDATE %s_capitulos SET manga = ?, volume = ?, capitulo = ?, linguagem = ?, scan = ?, vocabulario = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_PAGINAS = "UPDATE %s_paginas SET nome = ?, numero = ?, hash_pagina = ?, vocabulario = ?, is_processado = ? WHERE id = ?";
	final private String UPDATE_TEXTO = "UPDATE %s_textos SET sequencia = ?, texto = ?, posicao_x1 = ?, posicao_y1 = ?, posicao_x2 = ?, posicao_y2 = ? WHERE id = ?";

	final private String INSERT_VOLUMES = "UPDATE %s_volumes (manga, volume, linguagem, vocabulario, is_processado) VALUES (?,?,?,?,?)";
	final private String INSERT_CAPITULOS = "UPDATE %s_capitulos (id_volume, manga, volume, capitulo, linguagem, scan, is_extra, is_raw, is_processado, vocabulario) VALUES (?,?,?,?,?,?,?,?,?,?)";
	final private String INSERT_PAGINAS = "UPDATE %s_paginas (id_capitulo, nome, numero, hash_pagina, is_processado, vocabulario) VALUES (?,?,?,?,?,?)";
	final private String INSERT_TEXTO = "UPDATE %s_textos (id_pagina, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2) VALUES (?,?,?,?,?,?,?)";

	final private String UPDATE_VOLUMES_VOCABULARIO = "UPDATE %s_volumes SET vocabulario = ? WHERE id = ?;";
	final private String UPDATE_CAPITULOS_VOCABULARIO = "UPDATE %s_capitulos SET vocabulario = ? WHERE id = ?;";
	final private String UPDATE_PAGINAS_VOCABULARIO = "UPDATE %s_paginas SET vocabulario = ?, is_processado = ? WHERE id = ?;";

	final private String SELECT_VOLUMES = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.vocabulario, VOL.is_Processado FROM %s_volumes VOL %s WHERE VOL.linguagem = 'ja' AND %s GROUP BY VOL.id";
	final private String SELECT_CAPITULOS = "SELECT CAP.id, CAP.manga, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.scan, CAP.is_extra, CAP.is_raw, CAP.is_processado, "
			+ "CAP.vocabulario FROM %s_capitulos CAP %s WHERE CAP.linguagem = 'ja' AND id_volume = ? AND %s GROUP BY CAP.id";
	final private String SELECT_PAGINAS = "SELECT id, nome, numero, hash_pagina, is_processado, vocabulario FROM %s_paginas WHERE id_capitulo = ? AND %s ";
	final private String SELECT_TEXTOS = "SELECT id, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2 FROM %s_textos WHERE id_pagina = ? ";

	final private String INNER_CAPITULOS = "INNER JOIN %s_paginas PAG ON CAP.id = PAG.id_capitulo AND PAG.is_processado = 0";
	final private String INNER_VOLUMES = "INNER JOIN %s_capitulos CAP ON VOL.id = CAP.id_volume "
			+ "INNER JOIN %s_paginas PAG ON CAP.id = PAG.id_capitulo AND PAG.is_processado = 0";

	final private String SELECT_TABELAS = "SELECT REPLACE(REPLACE(REPLACE(Table_Name, '_paginas', ''), '_capitulos', ''), '_volumes', '') AS Tabela "
			+ "FROM information_schema.tables WHERE table_schema = 'manga_extractor' AND (Table_Name NOT LIKE '%%_textos%%' AND Table_Name NOT LIKE '%%exemplo%%') "
			+ "AND %s GROUP BY Tabela ";

	public MangaDaoJDBC(Connection conn) {
		this.conn = conn;
		Properties props = Configuracao.loadProperties();
		BASE_MANGA = props.getProperty("dataBase_manga") + ".";
	}

	@Override
	public void updateVocabularioVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_VOLUMES_VOCABULARIO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setLong(2, obj.getId());

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
	public void updateVocabularioCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_CAPITULOS_VOCABULARIO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setLong(2, obj.getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
			;
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}

	}

	@Override
	public void updateVocabularioPagina(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PAGINAS_VOCABULARIO, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setBoolean(2, obj.getProcessado());
			st.setLong(3, obj.getId());

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
	public void updateVolume(String base, MangaVolume obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_VOLUMES, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getManga());
			st.setInt(2, obj.getVolume());
			st.setString(3, obj.getLingua().toString());
			st.setString(4, obj.getVocabulario());
			st.setBoolean(5, obj.getProcessado());
			st.setLong(6, obj.getId());

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
			st.setString(4, obj.getLingua().toString());
			st.setString(5, obj.getScan());
			st.setString(6, obj.getVocabulario());
			st.setBoolean(7, obj.getProcessado());
			st.setLong(8, obj.getId());

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
	public void updatePagina(String base, MangaPagina obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(UPDATE_PAGINAS, BASE_MANGA + base),
					Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getNomePagina());
			st.setInt(2, obj.getNumero());
			st.setString(3, obj.getHash());
			st.setString(4, obj.getVocabulario());
			st.setBoolean(5, obj.getProcessado());
			st.setLong(6, obj.getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			}
			;
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

	public List<MangaVolume> selectVolumesTransferir(String baseOrigem) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_VOLUMES, baseOrigem, "1>0"));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("vocabulario"),
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

			st = conn.prepareStatement(String.format(SELECT_VOLUMES, BASE_MANGA + base, inner, "1>0"));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("vocabulario"),
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

	public List<MangaVolume> selectVolumes(String base, Boolean todos, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_VOLUMES, BASE_MANGA + base, BASE_MANGA + base);

			String condicao = "1>0";

			if (manga != null && !manga.trim().isEmpty())
				condicao += " AND VOL.manga = " + '"' + manga.trim() + '"';

			if (volume != null && volume > 0)
				condicao += " AND VOL.volume = " + String.valueOf(volume);

			st = conn.prepareStatement(String.format(SELECT_VOLUMES, BASE_MANGA + base, inner, condicao));
			rs = st.executeQuery();

			List<MangaVolume> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaVolume(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						Language.getEnum(rs.getString("linguagem")), rs.getString("vocabulario"),
						selectCapitulos(base, todos, rs.getLong("id"), capitulo)));
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
			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, baseOrigem, "1>0"));
			st.setLong(1, idVolume);
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getString("vocabulario"), rs.getBoolean("is_processado"),
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

			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, BASE_MANGA + base, inner, "1>0"));
			st.setLong(1, idVolume);
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getString("vocabulario"), rs.getBoolean("is_processado"),
						selectPaginas(base, todos, rs.getLong("id"))));

			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	public List<MangaCapitulo> selectCapitulos(String base, Boolean todos, Long idVolume, Float capitulo)
			throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String inner = "";
			if (!todos)
				inner = String.format(INNER_CAPITULOS, BASE_MANGA + base);

			String condicao = "1>0";

			if (capitulo != null && capitulo > 0)
				condicao += " AND CAP.capitulo = " + String.valueOf(capitulo);

			st = conn.prepareStatement(String.format(SELECT_CAPITULOS, BASE_MANGA + base, inner, condicao));
			st.setLong(1, idVolume);
			rs = st.executeQuery();

			List<MangaCapitulo> list = new ArrayList<>();

			while (rs.next())
				list.add(new MangaCapitulo(rs.getLong("id"), rs.getString("manga"), rs.getInt("volume"),
						rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem")), rs.getString("scan"),
						rs.getString("vocabulario"), rs.getBoolean("is_processado"),
						selectPaginas(base, todos, rs.getLong("id"))));

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

	public List<MangaPagina> selectPaginas(String base, Boolean todos, Long idPagina) throws ExcessaoBd {
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
						selectTextos(base, rs.getLong("id"))));

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

	public List<MangaTexto> selectTextos(String base, Long idPagina) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(String.format(SELECT_TEXTOS, BASE_MANGA + base));
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
	public List<MangaVolume> selectAll(String base, String manga, Integer volume, Float capitulo) throws ExcessaoBd {
		return selectVolumes(base, true, manga, volume, capitulo);
	}

	@Override
	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT_TABELAS, "1>0"));
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

			st = conn.prepareStatement(String.format(SELECT_TABELAS, condicao));
			rs = st.executeQuery();

			List<MangaTabela> list = new ArrayList<>();

			while (rs.next()) {
				List<MangaVolume> volumes = selectVolumes(rs.getString("Tabela"), todos, manga, volume, capitulo);
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
			st.setString(3, obj.getLingua().toString());
			st.setString(4, obj.getVocabulario());
			st.setBoolean(5, obj.getProcessado());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					return rs.getLong(1);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
		return null;
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
			st.setString(5, obj.getLingua().toString());
			st.setString(6, obj.getScan());
			st.setBoolean(7, obj.getExtra());
			st.setBoolean(8, obj.getRaw());
			st.setBoolean(9, obj.getProcessado());
			st.setString(10, obj.getVocabulario());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					return rs.getLong(1);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
		return null;
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
			st.setString(6, obj.getVocabulario());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					return rs.getLong(1);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
		return null;
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
				throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
			} else {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next())
					return rs.getLong(1);
			}
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
		} finally {
			DB.closeStatement(st);
		}
		return null;
	}

	@Override
	public List<MangaVolume> selectTransferir(String baseOrigem) throws ExcessaoBd {
		return selectVolumesTransferir(baseOrigem);
	}

}
