package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime


class MockSincronizacao(var conexao: Conexao) : MockJpaBase<Long, Sincronizacao>() {

    override fun mockEntity(): Sincronizacao = mockEntity(null)

    override fun randomId(): Long? = 1

    override fun updateEntity(input: Sincronizacao): Sincronizacao = updateEntityById(input.id)

    override fun updateEntityById(lastId: Long?) = Sincronizacao(lastId!!, conexao, LocalDateTime.now(), LocalDateTime.now())

    override fun mockEntity(id: Long?) = Sincronizacao(10, conexao, LocalDateTime.now(), LocalDateTime.now())

    override fun assertsService(input: Sincronizacao?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.id > 0)
        assertTrue(input.envio.isAfter(LocalDateTime.MIN))
        assertTrue(input.recebimento.isAfter(LocalDateTime.MIN))
    }

    override fun assertsService(oldObj: Sincronizacao?, newObj: Sincronizacao?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.id, newObj!!.id)
        assertEquals(oldObj.envio, newObj.envio)
        assertEquals(oldObj.recebimento, newObj.recebimento)
        assertTrue(oldObj.envio.isEqual(newObj.envio))
        assertTrue(oldObj.recebimento.isEqual(newObj.recebimento))
    }

}