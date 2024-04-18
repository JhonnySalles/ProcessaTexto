package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.SincronizacaoDao;
import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Sincronizacao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.enums.Conexao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SincronizacaoDaoJDBC implements SincronizacaoDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(SincronizacaoDaoJDBC.class);

    private final Connection conn;

    final private String UPDATE = "UPDATE sincronizacao SET envio = ?, recebimento = ? WHERE conexao = ?;";
    final private String SELECT = "SELECT conexao, envio, recebimento FROM sincronizacao WHERE conexao = ?;";

    public SincronizacaoDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void update(Sincronizacao obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

            int index = 0;
            st.setString(++index, Util.convertToString(obj.getEnvio()));
            st.setString(++index, Util.convertToString(obj.getRecebimento()));
            st.setString(++index, obj.getConexao().toString());

            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Sincronizacao select(Conexao tipo) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, tipo.toString());
            rs = st.executeQuery();

            if (rs.next())
                return new Sincronizacao(Conexao.valueOf(rs.getString("conexao")), Util.convertToDateTime(rs.getString("envio")), Util.convertToDateTime(rs.getString("recebimento")));
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return null;
    }

}
