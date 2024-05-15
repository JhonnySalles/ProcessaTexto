package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import org.jisho.textosJapones.model.entities.comicinfo.AgeRating
import org.junit.jupiter.api.Assertions.*
import java.util.*


class MockComicInfo : MockJpaBase<UUID?, ComicInfo>() {

    override fun mockEntity(): ComicInfo = mockEntity(null)

    override fun randomId(): UUID? = UUID.randomUUID()

    override fun updateEntity(input: ComicInfo): ComicInfo {
        input.comic += "---"
        input.title += "---"
        input.series += "---"
        input.publisher += "---"
        input.alternateSeries += "---"
        input.storyArc += "---"
        input.seriesGroup += "---"
        input.imprint += "---"
        input.genre += "---"
        input.languageISO += "---"

        input.idMal = 2
        input.ageRating = AgeRating.Adults

        return input
    }

    override fun updateEntityById(lastId: UUID?): ComicInfo {
        return ComicInfo(
            lastId, 2, "comic" + "---", "title" + "---", "series" + "---", "publisher" + "---",
            "alternativeSeries" + "---", "storyArc" + "---", "seriesGroup" + "---", "imprint" + "---",
            "genre" + "---", "language" + "---", AgeRating.Adults
        )
    }

    override fun mockEntity(id: UUID?): ComicInfo {
        return ComicInfo(
            id, 1, "comic", "title", "series", "publisher",
            "alternativeSeries", "storyArc", "seriesGroup", "imprint",
            "genre", "language", AgeRating.Adults
        )
    }

    override fun assertsService(input: ComicInfo?) {
        assertNotNull(input)
        assertNotNull(input!!.getId())

        assertTrue(input.comic!!.isNotEmpty())
        assertTrue(input.idMal!! > 0.0)
        assertTrue(input.series!!.isNotEmpty())
        assertTrue(input.title!!.isNotEmpty())
        assertTrue(input.publisher!!.isNotEmpty())
        assertTrue(input.genre!!.isNotEmpty())
        assertTrue(input.imprint!!.isNotEmpty())
        assertTrue(input.seriesGroup!!.isNotEmpty())
        assertTrue(input.storyArc!!.isNotEmpty())
        assertTrue(input.alternateSeries!!.isNotEmpty())
        assertNotNull(input.ageRating)
        assertNotNull(input.languageISO)
    }

    override fun assertsService(oldObj: ComicInfo?, newObj: ComicInfo?) {
        assertsService(oldObj)
        assertsService(newObj)

        assertEquals(oldObj!!.comic, newObj!!.comic)
        assertEquals(oldObj.idMal, newObj.idMal)
        assertEquals(oldObj.series, newObj.series)
        assertEquals(oldObj.title, newObj.title)
        assertEquals(oldObj.publisher, newObj.publisher)
        assertEquals(oldObj.genre, newObj.genre)
        assertEquals(oldObj.imprint, newObj.imprint)
        assertEquals(oldObj.seriesGroup, newObj.seriesGroup)
        assertEquals(oldObj.storyArc, newObj.storyArc)
        assertEquals(oldObj.alternateSeries, newObj.alternateSeries)
        assertEquals(oldObj.ageRating, newObj.ageRating)
        assertEquals(oldObj.languageISO, newObj.languageISO)
    }

}