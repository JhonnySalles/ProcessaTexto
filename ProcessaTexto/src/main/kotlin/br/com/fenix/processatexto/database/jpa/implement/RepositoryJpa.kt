package br.com.fenix.processatexto.database.jpa.implement

import br.com.fenix.processatexto.database.JpaFactory
import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao
import java.lang.reflect.ParameterizedType
import java.util.*
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional


open class RepositoryJpa<ID, E : EntityBase<ID, E>>(conexao: Conexao) : RepositoryJpaBase<ID, E> {

    private val clazzEntity: Class<E>

    init {
        val superclass = (javaClass.genericSuperclass as ParameterizedType)
        clazzEntity = superclass.actualTypeArguments[1] as Class<E>
    }

    protected val em: EntityManager = JpaFactory.getFactory(conexao).createEntityManager()

    /**
     * Busca o item pelo id da entidade
     * @param id id genérico dependendo da entidade
     * @return retorna preenchido caso encontre
     */
    override fun find(id: ID): Optional<E> = Optional.ofNullable(em.find(clazzEntity, id))

    /**
     * Verifica se a entidade se encontra salva no banco
     * @param entity entidade
     * @return true se encontrado
     */
    override fun exists(entity: E): Boolean = em.contains(entity)

    /**
     * Inicia a transação no banco de dados
     */
    override fun beginTransaction() = em.transaction.begin()

    /**
     * Confirma as altrações no banco
     */
    override fun commit() = em.transaction.commit()

    /**
     * Desfaz as alterações no banco
     */
    override fun rollBack() = em.transaction.rollback()

    /**
     * Busca todos os registros da tabela
     * @return um objeto opcional preenchido caso encontrado
     */
    override fun findAll(): List<E> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(clazzEntity)
        val rootEntry = cq.from(clazzEntity)
        val all = cq.select(rootEntry)
        val allQuery = em.createQuery(all)
        return allQuery.resultList
    }

    /**
     * Busca de uma entidade unica em uma consulta sql sem parâmetros
     * @param sql query em string para o java persistem
     * @return um objeto opcional preenchido caso encontrado
     * @throws IllegalArgumentException caso o sql esteja errado
     *
     * Para uma consulta sem retorno utilize a função abaixo
     * @see query
     */
    override fun queryEntity(@org.intellij.lang.annotations.Language("sql") sql: String): Optional<E> {
        val q = em.createQuery(sql, clazzEntity)
        return Optional.ofNullable(q.singleResult)
    }

    /**
     * Busca de uma entidade unica em uma consulta sql
     * @param sql query em string para o java persistem
     * @param params parametros da query
     * @return um objeto opcional preenchido caso encontrado
     * @throws IllegalArgumentException caso o sql esteja errado
     *
     * Para uma consulta sem retorno utilize a função abaixo
     * @see query
     */
    override fun queryEntity(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any>): Optional<E> {
        val q = em.createQuery(sql, clazzEntity)
        for (itm in params.keys)
            q.setParameter(itm, params[itm])
        return Optional.ofNullable(q.singleResult)
    }

    /**
     * Busca uma lista de entidade da query informada
     * @param sql query em string para o java persistem
     * @return uma lista com os objetos encontrado
     * @throws IllegalArgumentException caso o sql esteja errado
     *
     * Para uma consulta sem retorno utilize a função abaixo
     * @see query
     */
    override fun queryList(@org.intellij.lang.annotations.Language("sql") sql: String): List<E> {
        val q = em.createQuery(sql, clazzEntity)
        return q.resultList
    }

    /**
     * Busca uma lista de entidade da query informada
     * @param sql query em string para o java persistem
     * @param params parametros da query
     * @return uma lista com os objetos encontrado
     * @throws IllegalArgumentException caso o sql esteja errado
     *
     * Para uma consulta sem retorno utilize a função abaixo
     * @see query
     */
    override fun queryList(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any>): List<E> {
        val q = em.createQuery(sql, clazzEntity)
        for (itm in params.keys)
            q.setParameter(itm, params[itm])
        return q.resultList
    }

    /**
     * Executa uma query no banco, no qual poderá ser um insert ou update
     * @param sql query em string para o java persistem
     * @return uma lista com os objetos encontrado
     * @throws IllegalArgumentException caso o sql esteja errado
     *
     * Para uma consulta com retorno utilize as funções abaixo
     * @see queryList
     * @see queryEntity
     */
    override fun query(@org.intellij.lang.annotations.Language("sql") sql: String) {
        val q = em.createQuery(sql, clazzEntity)
        q.executeUpdate()
    }

    /**
     * Executa uma query no banco, no qual poderá ser um insert ou update
     * @param sql query em string para o java persistem
     * @param params parametros da query
     * @return uma lista com os objetos encontrado
     * @throws IllegalArgumentException caso o sql esteja errado
     *
     * Para uma consulta com retorno utilize as funções abaixo
     * @see queryList
     * @see queryEntity
     */
    override fun query(@org.intellij.lang.annotations.Language("sql") sql: String, params: Map<String, Any>) {
        val q = em.createQuery(sql, clazzEntity)
        for (itm in params.keys)
            q.setParameter(itm, params[itm])
        q.executeUpdate()
    }

    /**
     * Deleta a entidade do banco caso exista
     * @param entity entidade a ser apagada
     */
    @Transactional
    override fun delete(entity: E) {
        try {
            if (em.contains(entity)) {
                em.transaction.begin()
                em.remove(entity)
                em.transaction.commit()
            }
        } catch (e: Exception) {
            if (em.transaction.isActive)
                em.transaction.rollback()
            throw e
        }
    }

    /**
     * Deleta a entidade do banco caso exista
     * @param id id do registro a ser apagado
     */
    @Transactional
    override fun delete(id: ID) {
        val entity = find(id)

        try {
            if (entity.isPresent) {
                em.transaction.begin()
                em.remove(entity.get())
                em.transaction.commit()
            }
        } catch (e: Exception) {
            if (em.transaction.isActive)
                em.transaction.rollback()
            throw e
        }
    }

    /**
     * Salva a entidade no banco, caso o id seja nulo será salvo, caso contrario atualizado
     * @param entity entidade a ser salva no banco
     * @return retorna a entidade salva
     */
    @Transactional
    override fun save(entity: E): E {
        try {
            em.transaction.begin()
            val res = if (entity.getId() == null) {
                em.persist(entity)
                entity
            } else
                em.merge(entity)
            em.transaction.commit()

            return res
        } catch (e: Exception) {
            if (em.transaction.isActive)
                em.transaction.rollback()
            throw e
        }
    }

    /**
     * Salva uma lista de entidade no banco, caso o id seja nulo será salvo, caso contrario atualizado
     * @param Lista da entidade a ser salva no banco
     * @return retorna a lista de entidade salva
     */
    @Transactional
    override fun saveAll(list: List<E>): List<E> {
        val saved = mutableListOf<E>()

        try {
            em.transaction.begin()

            list.forEach {
                val res = if (it.getId() == null) {
                    em.persist(it)
                    it
                } else
                    em.merge(it)
                saved.add(res)
            }
            em.transaction.commit()
            return saved
        } catch (e: Exception) {
            if (em.transaction.isActive)
                em.transaction.rollback()
            throw e
        }
    }
}