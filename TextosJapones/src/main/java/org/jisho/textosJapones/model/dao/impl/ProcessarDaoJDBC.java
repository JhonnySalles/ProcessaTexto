package org.jisho.textosJapones.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.dao.ProcessarDao;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.mysql.DB;

public class ProcessarDaoJDBC implements ProcessarDao {

	private Connection conn;

	final private String INSERT_EXCLUSAO = "INSERT IGNORE INTO exclusao (palavra) VALUES (?);";
	final private String INSERT_FILA = "INSERT INTO fila_sql (selectSQL, updateSQL, vocabulario ) VALUES (?, ?, ?);";
	final private String UPDATE_FILA = "UPDATE fila_sql SET selectSQL = ?, updateSQL = ?, vocabulario = ? WHERE sequencial = ?";
	final private String SELECT_FILA = "SELECT sequencial, selectSQL, updateSQL, vocabulario FROM fila_sql";

	public ProcessarDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void update(String update, Processar obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getVocabulario());
			st.setString(2, obj.getId());

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
	public List<Processar> select(String select) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(select);
			rs = st.executeQuery();

			List<Processar> list = new ArrayList<>();

			while (rs.next())
				list.add(new Processar(rs.getString(1), rs.getString(2)));

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
	public void exclusao(String exclusao) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT_EXCLUSAO, Statement.RETURN_GENERATED_KEYS);
			st.setString(1, exclusao);

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
	public void insert(FilaSQL fila) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT_FILA, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, fila.getSelect());
			st.setString(2, fila.getUpdate());
			st.setString(3, fila.getVocabulario());

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
	public void update(FilaSQL fila) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(UPDATE_FILA, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, fila.getSelect());
			st.setString(2, fila.getUpdate());
			st.setString(3, fila.getVocabulario());
			st.setLong(4, fila.getId());

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
	public List<FilaSQL> select() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_FILA);
			rs = st.executeQuery();

			List<FilaSQL> list = new ArrayList<>();

			while (rs.next())
				list.add(new FilaSQL(rs.getLong("sequencial"), rs.getString("selectSQL"), rs.getString("updateSQL"),
						rs.getString("vocabulario")));

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
