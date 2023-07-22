package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.controller.EstatisticaController.Tabela;
import org.jisho.textosJapones.database.dao.EstatisticaDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Estatistica;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstatisticaDaoJDBC implements EstatisticaDao {

	private Connection conn;

	final private static String INSERT = "INSERT IGNORE INTO estatistica (kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	final private static String UPDATE = "UPDATE estatistica SET tipo = ?, quantidade = ?, percentual = ?, media = ?, percMedia = ?, corSequencial = ? WHERE kanji = ? AND leitura = ? ;";
	final private static String DELETE = "DELETE FROM estatistica WHERE kanji = ? AND leitura = ? ;";
	final private static String SELECT = "SELECT kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial FROM estatistica WHERE kanji = ? AND leitura = ? ;";
	final private static String SELECT_KANJI = "SELECT kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial FROM estatistica WHERE kanji = ? ;";
	final private static String SELECT_ALL = "SELECT kanji, leitura, tipo, quantidade, percentual, media, percMedia, corSequencial FROM estatistica WHERE 1 > 0;";

	final private static String PESQUISA = "SELECT sequencia, word, readInfo, frequency, tabela FROM words_kanji_info WHERE word LIKE ";

	public EstatisticaDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Estatistica obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getKanji());
			st.setString(2, obj.getLeitura());
			st.setString(3, obj.getTipo());
			st.setDouble(4, obj.getQuantidade());
			st.setFloat(5, obj.getPercentual());
			st.setDouble(6, obj.getMedia());
			st.setFloat(7, obj.getPercentMedia());
			st.setInt(8, obj.getCorSequencial());

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
	public void update(Estatistica obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

			st.setString(1, obj.getTipo());
			st.setDouble(2, obj.getQuantidade());
			st.setFloat(3, obj.getPercentual());
			st.setDouble(4, obj.getMedia());
			st.setFloat(5, obj.getPercentMedia());
			st.setInt(6, obj.getCorSequencial());
			st.setString(7, obj.getKanji());
			st.setString(8, obj.getLeitura());
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
	public void delete(Estatistica obj) throws ExcessaoBd {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(DELETE);

			st.setString(1, obj.getKanji());
			st.setString(2, obj.getLeitura());

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
	public Estatistica select(String kanji, String leitura) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(SELECT);
			st.setString(1, kanji);
			st.setString(2, leitura);
			rs = st.executeQuery();

			if (rs.next()) {
				return new Estatistica(rs.getString("kanji"), rs.getString("tipo"), rs.getString("leitura"),
						rs.getDouble("quantidade"), rs.getFloat("percentual"), rs.getDouble("media"),
						rs.getFloat("percMedia"), rs.getInt("corSequencial"));
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
	public List<Estatistica> selectAll() throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_ALL);
			rs = st.executeQuery();

			List<Estatistica> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Estatistica(rs.getString("kanji"), rs.getString("tipo"), rs.getString("leitura"),
						rs.getDouble("quantidade"), rs.getFloat("percentual"), rs.getDouble("media"),
						rs.getFloat("percMedia"), rs.getInt("corSequencial")));
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
	public List<Estatistica> select(String kanji) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(SELECT_KANJI);
			st.setString(1, kanji);
			rs = st.executeQuery();

			List<Estatistica> list = new ArrayList<>();

			while (rs.next()) {
				list.add(new Estatistica(rs.getString("kanji"), rs.getString("tipo"), rs.getString("leitura"),
						rs.getDouble("quantidade"), rs.getFloat("percentual"), rs.getDouble("media"),
						rs.getFloat("percMedia"), rs.getInt("corSequencial")));
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
	public List<Tabela> pesquisa(String pesquisa) throws ExcessaoBd {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {

			st = conn.prepareStatement(PESQUISA + "'%" + pesquisa + "%'");
			rs = st.executeQuery();

			List<Tabela> list = new ArrayList<>();

			while (rs.next())
				list.add(new Tabela(rs.getString("word"), rs.getString("readInfo"), rs.getString("tabela"), true));

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
