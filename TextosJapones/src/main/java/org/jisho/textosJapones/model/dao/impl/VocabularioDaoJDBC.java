package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.model.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.mysql.DB;

public class VocabularioDaoJDBC implements VocabularioDao {

	private Connection conn;

	final private String insert = "INSERT IGNORE INTO vocabulario (vocabulario, formaBasica, leitura, traducao) VALUES (?,?,?,?);";
	final private String update = "UPDATE vocabulario SET formaBasica = ?, leitura = ?, traducao = ? WHERE vocabulario = ?;";
	final private String delete = "DELETE FROM vocabulario WHERE vocabulario = ?;";
	final private String select = "SELECT vocabulario, formaBasica, leitura, traducao FROM vocabulario WHERE vocabulario = ? OR formaBasica = ?;";
	final private String selectPalavra = "SELECT vocabulario, formaBasica, leitura, traducao FROM vocabulario WHERE vocabulario = ?;";
	final private String exist = "SELECT vocabulario FROM vocabulario WHERE vocabulario = ?;";
	final private String selectAll = "SELECT vocabulario, formaBasica, leitura, traducao FROM vocabulario WHERE 1 > 0;";
	final private String insertExclusao = "INSERT INTO exclusao (palavra) VALUES (?);";
	final private String selectExclusao = "SELECT palavra FROM exclusao";

	public VocabularioDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Vocabulario obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setString(2, obj.getFormaBasica());
			st.setString(3, obj.getLeitura());
			st.setString(4, obj.getTraducao());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
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
	public void update(Vocabulario obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getFormaBasica());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getTraducao());
			st.setString(4, obj.getVocabulario());

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
			st = conn.prepareStatement(delete);

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
			st = conn.prepareStatement(select);
			st.setString(1, vocabulario);
			st.setString(2, base);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Vocabulario(rs.getString("vocabulario"), rs.getString("formaBasica"),
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
	public Vocabulario select(String vocabulario) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(selectPalavra);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Vocabulario(rs.getString("vocabulario"), rs.getString("formaBasica"),
						rs.getString("leitura"), rs.getString("traducao"));
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

			st = conn.prepareStatement(selectAll);
			rs = st.executeQuery();

			List<Vocabulario> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Vocabulario(rs.getString("vocabulario"), rs.getString("formaBasica"),
						rs.getString("leitura"), rs.getString("traducao")));
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
			st = conn.prepareStatement(exist);
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
			st = conn.prepareStatement(insertExclusao, Statement.RETURN_GENERATED_KEYS);
			st.setString(1, palavra);

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println(st.toString());
				throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
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
	public Set<String> selectExclusao() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(selectExclusao);
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

}
