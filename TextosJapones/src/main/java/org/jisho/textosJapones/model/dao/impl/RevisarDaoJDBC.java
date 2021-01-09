package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.dao.RevisarDao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.mysql.DB;

public class RevisarDaoJDBC implements RevisarDao {

	private Connection conn;

	final private String INSERT = "INSERT IGNORE INTO revisar (vocabulario, formaBasica, leitura, traducao, ingles, revisado) VALUES (?,?,?,?,?,?);";
	final private String UPDATE = "UPDATE revisar SET formaBasica = ?, leitura = ?, traducao = ?, ingles = ?, revisado = ? WHERE vocabulario = ?;";
	final private String DELETE = "DELETE FROM revisar WHERE vocabulario = ?;";
	final private String SELECT = "SELECT vocabulario, formaBasica, leitura, traducao, ingles, revisado FROM revisar WHERE vocabulario = ? OR formaBasica = ?;";
	final private String SELECT_PALAVRA = "SELECT vocabulario, formaBasica, leitura, traducao, ingles, revisado FROM revisar WHERE vocabulario = ?;";
	final private String EXIST = "SELECT vocabulario FROM revisar WHERE vocabulario = ?;";
	final private String SELECT_ALL = "SELECT vocabulario, formaBasica, leitura, traducao, ingles, revisado FROM revisar WHERE 1 > 0;";
	final private String SELECT_REVISA = "SELECT vocabulario, formaBasica, leitura, traducao, ingles, revisado FROM revisar WHERE revisado = false LIMIT 100";
	
	public RevisarDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Revisar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setString(2, obj.getFormaBasica());
			st.setString(3, obj.getLeitura());
			st.setString(4, obj.getTraducao());
			st.setString(5, obj.getIngles());
			st.setBoolean(6, obj.getRevisado().isSelected());

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
	public void update(Revisar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getFormaBasica());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getTraducao());
			st.setString(4, obj.getIngles());
			st.setBoolean(5, obj.getRevisado().isSelected());
			st.setString(6, obj.getVocabulario());

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
	public void delete(Revisar obj) throws ExcessaoBd {
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
	public Revisar select(String vocabulario, String base) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT);
			st.setString(1, vocabulario);
			st.setString(2, base);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"));
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
	public Revisar select(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT_PALAVRA);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return new Revisar(vocabulario);
	}

	@Override
	public List<Revisar> selectAll() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_ALL);
			rs = st.executeQuery();

			List<Revisar> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado")));
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
	public List<Revisar> selectRevisar() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_REVISA);
			rs = st.executeQuery();

			List<Revisar> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Revisar(rs.getString("vocabulario"), rs.getString("formaBasica"), rs.getString("leitura"),
						rs.getString("traducao"), rs.getString("ingles"), rs.getBoolean("revisado")));
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
	public List<String> selectFrases(String select) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(select);
			rs = st.executeQuery();

			List<String> list = new ArrayList<>();

			while (rs.next())
				list.add(rs.getString(1));

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
