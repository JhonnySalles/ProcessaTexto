package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;

import java.sql.*;
import java.util.*;

public class VocabularioJaponesDaoJDBC implements VocabularioDao {

	private Connection conn;

	final private String INSERT = "INSERT IGNORE INTO vocabulario (id, vocabulario, forma_basica, leitura, portugues, ingles) VALUES (?, ?,?,?,?,?);";
	final private String UPDATE = "UPDATE vocabulario SET forma_basica = ?, leitura = ?, portugues = ?, ingles = ? WHERE vocabulario = ?;";
	final private String DELETE = "DELETE FROM vocabulario WHERE vocabulario = ?;";
	final private String SELECT = "SELECT id, vocabulario, forma_basica, leitura, portugues, ingles FROM vocabulario WHERE vocabulario = ? OR forma_basica = ?;";
	final private String SELECT_PALAVRA = "SELECT id, vocabulario, forma_basica, leitura, portugues, ingles FROM vocabulario WHERE vocabulario = ?;";
	final private String EXIST = "SELECT vocabulario FROM vocabulario WHERE vocabulario = ?;";
	final private String SELECT_ALL = "SELECT id, vocabulario, forma_basica, leitura, portugues, ingles FROM vocabulario WHERE forma_basica = '' OR leitura = '';";
	final private String INSERT_EXCLUSAO = "INSERT IGNORE INTO exclusao (palavra) VALUES (?)";
	final private String SELECT_ALL_EXCLUSAO = "SELECT palavra FROM exclusao";
	final private String SELECT_EXCLUSAO = "SELECT palavra FROM exclusao WHERE palavra = ? or palavra = ? ";

	public VocabularioJaponesDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Vocabulario obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getId().toString());
			st.setString(2, obj.getVocabulario());
			st.setString(3, obj.getFormaBasica());
			st.setString(4, obj.getLeitura());
			st.setString(5, obj.getPortugues());
			st.setString(6, obj.getIngles());

			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Vocabulario obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getFormaBasica());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getPortugues());
			st.setString(4, obj.getIngles());
			st.setString(5, obj.getVocabulario());

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
	public void delete(Vocabulario obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(DELETE);

			st.setString(1, obj.getVocabulario());

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
	public Vocabulario select(String vocabulario, String base) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT);
			st.setString(1, vocabulario);
			st.setString(2, base);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
						rs.getString("leitura"), rs.getString("ingles"), rs.getString("portugues"));
			}
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
	public Vocabulario select(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_PALAVRA);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
						rs.getString("leitura"), rs.getString("ingles"), rs.getString("portugues"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return new Vocabulario(vocabulario);
	}

	@Override
	public List<Vocabulario> selectAll() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_ALL);
			rs = st.executeQuery();

			List<Vocabulario> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
						rs.getString("leitura"), rs.getString("ingles"), rs.getString("portugues")));
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
	public boolean exist(String vocabulario) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(EXIST);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return false;
	}

	@Override
	public void insertExclusao(String palavra) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT_EXCLUSAO, Statement.RETURN_GENERATED_KEYS);
			st.setString(1, palavra);

			st.executeUpdate();
		} catch (SQLException e) {
			System.out.println(st.toString());
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Set<String> selectExclusao() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_ALL_EXCLUSAO);
			rs = st.executeQuery();

			Set<String> list = new HashSet<String>();

			while (rs.next())
				list.add(rs.getString("palavra"));

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
	public boolean existeExclusao(String palavra, String basico) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_EXCLUSAO);
			st.setString(1, palavra);
			st.setString(2, basico);
			rs = st.executeQuery();

			if (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return false;
	}

}
