package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.util.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.*
import java.time.LocalDateTime
import java.util.*


abstract class RepositoryDao<ID, E : EntityBase<ID, E>>(conexao: Conexao) : RepositoryDaoBase<ID, E> {

    private val LOGGER: Logger = LoggerFactory.getLogger(RepositoryDao::class.java)

    protected val conn: Connection = JdbcFactory.getFactory(conexao)

    abstract fun toEntity(rs: ResultSet): E

    /**
     * Inicia a transação no banco de dados
     */
    override fun beginTransaction() = conn.beginRequest()

    /**
     * Confirma as altrações no banco
     */
    override fun commit() = conn.commit()

    /**
     * Desfaz as alterações no banco
     */
    override fun rollBack() = conn.rollback()

    private fun setParams(st: PreparedStatement, params: Map<String, Any?>) {
        var index = 0
        for (p in params.keys) {
            if (params[p] == null)
                st.setNString(++index, null)
            else
                when (params[p]) {
                    is Int -> st.setInt(++index, params[p] as Int)
                    is Float -> st.setFloat(++index, params[p] as Float)
                    is Double -> st.setDouble(++index, params[p] as Double)
                    is String -> st.setString(++index, params[p] as String)
                    is Boolean -> st.setBoolean(++index, params[p] as Boolean)
                    is UUID -> st.setString(++index, params[p].toString())
                    is LocalDateTime -> st.setString(++index, Utils.convertToString(params[p] as LocalDateTime))
                }
        }
    }

    /**
     * Executa uma query no banco, no qual poderá ser um insert ou update
     * @param sql query em string para o java
     * @throws SQLException caso o sql esteja errado ou não tenha nenhuma linha alterada
     */
    override fun query(@org.intellij.lang.annotations.Language("sql") sql: String) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            val rowsAffected = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(sql)
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(sql)
            throw SQLException(Mensagens.BD_ERRO_QUERRY)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    /**
     * Executa uma query no banco com parâmetros
     * @param sql query em string
     * @param sql parametros da consulta
     * @throws SQLException caso o sql esteja errado ou não tenha nenhuma linha alterada
     */
    override fun query(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any?>) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(sql)
            setParams(st, params)
            val rowsAffected = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(sql)
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_QUERRY)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    /**
     * Executa uma query no banco com parâmetros e retorno
     * @param sql query em string
     * @param sql parametros da consulta
     * @return uma lista com os objetos encontrado
     * @throws SQLException caso o sql esteja errado ou não tenha nenhuma linha alterada
     */
    override fun queryEntity(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any?>): Optional<E> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(sql)
            setParams(st, params)
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(toEntity(rs))
            else
                Optional.empty<E>()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_QUERRY)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    /**
     * Executa uma query no banco com parâmetros e retorno
     * @param sql query em string
     * @param sql parametros da consulta
     * @return uma lista com os objetos encontrado
     * @throws SQLException caso o sql esteja errado ou não tenha nenhuma linha alterada
     */
    override fun queryList(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any?>): List<E> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(sql)
            setParams(st, params)
            rs = st.executeQuery()
            val list: MutableList<E> = mutableListOf()
            while (rs.next())
                list.add(toEntity(rs))
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_QUERRY)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }
}