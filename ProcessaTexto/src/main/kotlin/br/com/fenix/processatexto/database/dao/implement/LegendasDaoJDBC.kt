package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.LegendasDao
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.entities.processatexto.Processar
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.model.entities.subtitle.Legenda
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*


class LegendasDaoJDBC(conexao: Conexao, base: String) : LegendasDao, RepositoryDaoBase<UUID?, FilaSQL>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(LegendasDaoJDBC::class.java)

    companion object {
        private const val INSERT = "INSERT INTO %s (Episodio, Linguagem, TempoInicial, TempoFinal, Texto, Traducao, Vocabulario) VALUES (?, ?, ?, ?, ?, ?, ?);"
        private const val UPDATE = "UPDATE %s SET Episodio = ?, Linguagem = ?, TempoInicial = ?, TempoFinal = ?, Texto = ?, Traducao = ?, Vocabulario = ? WHERE id = ?;"
        private const val SELECT = "SELECT id, Episodio, Linguagem, TempoInicial, TempoFinal, Texto, Traducao, Vocabulario FROM %s WHERE 1 > 0 ;"
        private const val DELETE = "DELETE FROM %s WHERE Episodio = ? AND Linguagem = ?;"

        private const val SELECT_LISTA_TABELAS = ("SELECT Table_Name AS Tabela "
                + " FROM information_schema.tables WHERE table_schema = '%s' AND %s "
                + " AND Table_Name != '_sql' GROUP BY Tabela ")
        private const val CREATE_TABELA = "CALL create_table('%s');"
        private const val CREATE_TRIGGER_INSERT = "CREATE TRIGGER tr_%s_insert BEFORE INSERT ON %s" +
                "  FOR EACH ROW BEGIN" +
                "    IF (NEW.id IS NULL OR NEW.id = '') THEN" +
                "      SET new.id = UUID();" +
                "    END IF;" +
                "  END"
        private const val CREATE_TRIGGER_UPDATE = "CREATE TRIGGER tr_%s_update BEFORE UPDATE ON %s" +
                "  FOR EACH ROW BEGIN" +
                "    SET new.Atualizacao = NOW();" +
                "  END"
        private const val INSERT_FILA = "INSERT INTO fila_sql (id, select_sql, update_sql, delete_sql, vocabulario, linguagem, isExporta, isLimpeza) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
        private const val UPDATE_FILA = "UPDATE fila_sql SET select_sql = ?, update_sql = ?, delete_sql = ?, vocabulario = ?, linguagem = ?, isExporta = ?, isLimpeza = ? WHERE id = ?"
        private const val SELECT_FILA = "SELECT id, sequencial, select_sql, update_sql, delete_sql, vocabulario, linguagem, isExporta, isLimpeza FROM fila_sql"
        private const val EXISTS_FILA = "SELECT id, sequencial, select_sql, update_sql, delete_sql, vocabulario, linguagem, isExporta, isLimpeza FROM fila_sql WHERE delete_sql = ?"
    }

    private val connDeckSubtitle = JdbcFactory.getFactory(Conexao.DECKSUBTITLE)

    private val schema: String

    init {
        schema = base
    }

    @get:Throws(SQLException::class)
    @get:Override
    override val tabelas: List<String>
        get() {
            var st: PreparedStatement? = null
            var rs: ResultSet? = null
            return try {
                st = connDeckSubtitle.prepareStatement(
                    String.format(SELECT_LISTA_TABELAS, schema, "1 > 0")
                )
                rs = st.executeQuery()
                val list: MutableList<String> = mutableListOf()
                while (rs.next())
                    list.add(rs.getString("Tabela"))
                list
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_SELECT)
            } finally {
                JdbcFactory.closeStatement(st)
                JdbcFactory.closeResultSet(rs)
            }
        }

    @Throws(SQLException::class)
    override fun createTabela(base: String) {
        val nome: String = base.trim()
        var st: PreparedStatement? = null
        try {
            st = connDeckSubtitle.prepareStatement(String.format(CREATE_TABELA, nome))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
        try {
            st = connDeckSubtitle.prepareStatement(String.format(CREATE_TRIGGER_INSERT, nome, nome))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
        try {
            st = connDeckSubtitle.prepareStatement(String.format(CREATE_TRIGGER_UPDATE, nome, nome))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun delete(tabela: String, obj: Legenda) {
        var st: PreparedStatement? = null
        try {
            st = connDeckSubtitle.prepareStatement(String.format(DELETE, tabela))
            var index = 0
            st.setInt(++index, obj.episodio)
            st.setString(++index, obj.linguagem.sigla.uppercase(Locale.getDefault()))
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun insert(tabela: String, obj: Legenda) {
        var st: PreparedStatement? = null
        try {
            st = connDeckSubtitle.prepareStatement(String.format(INSERT, tabela), Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setInt(++index, obj.episodio)
            st.setString(++index, obj.linguagem.sigla.uppercase(Locale.getDefault()))
            st.setString(++index, obj.tempo)
            st.setNString(++index, null)
            st.setString(++index, obj.texto)
            st.setString(++index, obj.traducao)
            st.setString(++index, obj.vocabulario)
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun update(tabela: String, obj: Legenda) {
        var st: PreparedStatement? = null
        try {
            st = connDeckSubtitle.prepareStatement(String.format(UPDATE, tabela), Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setString(++index, obj.getId().toString())
            st.setInt(++index, obj.episodio)
            st.setString(++index, obj.linguagem.sigla.uppercase(Locale.getDefault()))
            st.setString(++index, obj.tempo)
            st.setNString(++index, null)
            st.setString(++index, obj.texto)
            st.setString(++index, obj.traducao)
            st.setString(++index, obj.vocabulario)
            st.setString(++index, obj.getId().toString())
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun comandoUpdate(update: String, obj: Processar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS)
            st.setString(1, obj.vocabulario)
            st.setString(2, obj.id)
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun comandoSelect(select: String): MutableList<Processar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(select)
            rs = st.executeQuery()
            val list: MutableList<Processar> = mutableListOf()
            while (rs.next())
                list.add(Processar(rs.getString(1), rs.getString(2)))
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun comandoInsert(fila: FilaSQL) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT_FILA, Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, UUID.randomUUID().toString())
            st.setString(++index, fila.select)
            st.setString(++index, fila.update)
            st.setString(++index, fila.delete)
            st.setString(++index, fila.vocabulario)
            st.setString(++index, fila.linguagem.sigla.uppercase(Locale.getDefault()))
            st.setBoolean(++index, fila.isExporta)
            st.setBoolean(++index, fila.isLimpeza)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun comandoUpdate(fila: FilaSQL) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(UPDATE_FILA, Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, fila.select)
            st.setString(++index, fila.update)
            st.setString(++index, fila.delete)
            st.setString(++index, fila.vocabulario)
            st.setString(++index, fila.linguagem.sigla.uppercase(Locale.getDefault()))
            st.setBoolean(++index, fila.isExporta)
            st.setBoolean(++index, fila.isLimpeza)
            st.setString(++index, fila.getId().toString())

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun existFila(deleteSql: String): Boolean {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(EXISTS_FILA)
            st.setString(1, deleteSql)
            rs = st.executeQuery()
            rs.next()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun comandoSelect(): MutableList<FilaSQL> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_FILA)
            rs = st.executeQuery()
            val list: MutableList<FilaSQL> = mutableListOf()
            while (rs.next())
                list.add(
                    FilaSQL(
                        UUID.fromString(rs.getString("id")), rs.getLong("sequencial"), rs.getString("select_sql"),
                        rs.getString("update_sql"), rs.getString("delete_sql"), rs.getString("vocabulario"),
                        Language.getEnum(rs.getString("linguagem").lowercase(Locale.getDefault()))!!,
                        rs.getBoolean("isExporta"), rs.getBoolean("isLimpeza")
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun comandoDelete(delete: String) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(delete, Statement.RETURN_GENERATED_KEYS)
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    override fun toEntity(rs: ResultSet): FilaSQL = FilaSQL(
        UUID.fromString(rs.getString("id")), rs.getLong("sequencial"), rs.getString("select_sql"),
        rs.getString("update_sql"), rs.getString("delete_sql"), rs.getString("vocabulario"),
        Language.getEnum(rs.getString("linguagem").lowercase(Locale.getDefault()))!!,
        rs.getBoolean("isExporta"), rs.getBoolean("isLimpeza")
    )
}