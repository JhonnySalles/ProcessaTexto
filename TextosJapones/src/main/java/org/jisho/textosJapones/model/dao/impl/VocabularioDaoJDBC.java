package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.util.mysql.DB;

public class VocabularioDaoJDBC implements VocabularioDao {

	private Connection conn;

	final private String insert = "INSERT INTO vocabulario (vocabulario, formaBasica, leitura, traducao) VALUES (?,?,?,?);";
	final private String update = "UPDATE vocabulario SET formaBasica = ?, leitura = ?, traducao = ? WHERE vocabulario = ?;";
	final private String delete = "DELETE FROM vocabulario WHERE vocabulario = ?;";
	final private String select = "SELECT vocabulario, formaBasica, leitura, traducao FROM vocabulario WHERE vocabulario = ?;";
	final private String exist = "SELECT vocabulario FROM vocabulario WHERE vocabulario = ?;";
	final private String selectAll = "SELECT vocabulario, formaBasica, leitura, traducao FROM vocabulario WHERE 1 > 0;";

	public VocabularioDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Vocabulario obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setString(2, obj.getFormaBasica());
			st.setString(3, obj.getLeitura());
			st.setString(4, obj.getTraducao());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println("Erro ao salvar os dados.");
				System.out.println(st.toString());
			}
			;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Vocabulario obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getFormaBasica());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getTraducao());
			st.setString(4, obj.getVocabulario());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println("Erro ao salvar os dados.");
				System.out.println(st.toString());
			}
			;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void delete(Vocabulario obj) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(delete);

			st.setString(1, obj.getVocabulario());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected < 1) {
				System.out.println("Erro ao salvar os dados.");
				System.out.println(st.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(st.toString());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Vocabulario select(String vocabulario) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(select);
			st.setString(1, vocabulario);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Vocabulario(rs.getString("vocabulario"), rs.getString("formaBasica"),
						rs.getString("leitura"), rs.getString("traducao"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return null;
	}

	@Override
	public List<Vocabulario> selectAll() {
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

}
