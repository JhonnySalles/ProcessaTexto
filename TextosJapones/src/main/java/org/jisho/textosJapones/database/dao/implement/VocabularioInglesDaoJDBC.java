package org.jisho.textosJapones.database.dao.implement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.configuration.Configuracao;

public class VocabularioInglesDaoJDBC implements VocabularioDao {

	private Connection conn;
	private String BASE_INGLES;

	final private String INSERT = "INSERT IGNORE INTO %svocabulario (vocabulario, leitura, traducao) VALUES (?,?,?,?);";
	final private String UPDATE = "UPDATE %svocabulario SET leitura = ?, traducao = ? WHERE vocabulario = ?;";
	final private String DELETE = "DELETE FROM %svocabulario WHERE vocabulario = ?;";
	final private String SELECT = "SELECT vocabulario, leitura, traducao FROM %svocabulario WHERE vocabulario = ?;";
	final private String EXIST = "SELECT vocabulario FROM %svocabulario WHERE vocabulario = ?;";
	final private String INSERT_EXCLUSAO = "INSERT IGNORE INTO %s.exclusao (palavra) VALUES (?)";
	final private String SELECT_ALL_EXCLUSAO = "SELECT palavra FROM %sexclusao";
	final private String SELECT_EXCLUSAO = "SELECT palavra FROM %sexclusao WHERE palavra = ? ";

	public VocabularioInglesDaoJDBC(Connection conn) {
		this.conn = conn;
		BASE_INGLES = Configuracao.loadProperties().getProperty("base_ingles") + ".";
	}

	@Override
	public void insert(Vocabulario obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(String.format(INSERT, BASE_INGLES), Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getTraducao());

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
			st = conn.prepareStatement(String.format(UPDATE, BASE_INGLES), Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getLeitura());
			st.setString(2, obj.getTraducao());
			st.setString(3, obj.getVocabulario());

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
			st = conn.prepareStatement(String.format(DELETE, BASE_INGLES));

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
		return null;
	}

	@Override
	public Vocabulario select(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(SELECT, BASE_INGLES));
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Vocabulario(rs.getString("vocabulario"), "",
						rs.getString("leitura"), rs.getString("traducao"));
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
	public boolean exist(String vocabulario) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(String.format(EXIST, BASE_INGLES));
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
			st = conn.prepareStatement(String.format(INSERT_EXCLUSAO, BASE_INGLES), Statement.RETURN_GENERATED_KEYS);
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
			st = conn.prepareStatement(String.format(SELECT_ALL_EXCLUSAO, BASE_INGLES));
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
			st = conn.prepareStatement(String.format(SELECT_EXCLUSAO, BASE_INGLES));
			st.setString(1, palavra);
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

	@Override
	public List<Vocabulario> selectAll() throws ExcessaoBd {
		return null;
	}

}
