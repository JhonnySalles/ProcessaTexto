package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.enums.Conexao
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import kotlin.random.Random


class MockSincronizacao() : MockJpaBase<Conexao, Sincronizacao>() {

    val conexoes = arrayListOf(Conexao.FIREBASE, Conexao.TEXTO_INGLES, Conexao.TEXTO_JAPONES)

    override fun mockEntity(): Sincronizacao = mockEntity(null)

    override fun randomId(): Conexao = conexoes[Random.nextInt(0, 2)]

    override fun mockEntityList(): List<Sincronizacao> {
        val list: MutableList<Sincronizacao> = mutableListOf()
        for (i in 0..2)
            list.add(mockEntity(conexoes[i]))
        return list
    }

    override fun updateEntity(input: Sincronizacao): Sincronizacao = updateEntityById(input.conexao)

    override fun updateEntityById(conexao: Conexao?) = Sincronizacao(conexao!!, LocalDateTime.now(), LocalDateTime.now())

    override fun mockEntity(conexao: Conexao?) = Sincronizacao(conexao!!, LocalDateTime.now(), LocalDateTime.now())

    override fun assertsService(input: Sincronizacao?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.envio.isAfter(LocalDateTime.MIN))
        assertTrue(input.recebimento.isAfter(LocalDateTime.MIN))
    }

    override fun assertsService(oldObj: Sincronizacao?, newObj: Sincronizacao?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.conexao, newObj!!.conexao)
        assertEquals(oldObj.envio, newObj.envio)
        assertEquals(oldObj.recebimento, newObj.recebimento)
        assertTrue(oldObj.envio.isEqual(newObj.envio))
        assertTrue(oldObj.recebimento.isEqual(newObj.recebimento))
    }

}