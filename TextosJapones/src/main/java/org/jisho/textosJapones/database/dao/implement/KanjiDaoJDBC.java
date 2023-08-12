package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.KanjiDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Kanji;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class KanjiDaoJDBC implements KanjiDao {

    private Connection conn;
    final private String SELECT = "SELECT id, kanji, palavra, significado FROM kanjax_pt WHERE kanji = ?;";

    public KanjiDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Kanji select(String kanji) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, kanji);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Kanji(UUID.fromString(rs.getString("id")),
                        rs.getString("kanji"), rs.getString("palavra"), rs.getString("significado"));
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
}
