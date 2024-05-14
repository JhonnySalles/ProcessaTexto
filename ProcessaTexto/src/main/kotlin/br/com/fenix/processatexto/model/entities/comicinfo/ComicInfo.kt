package br.com.fenix.processatexto.model.entities.comicinfo

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.comicinfo.Manga
import br.com.fenix.processatexto.model.enums.comicinfo.YesNo
import org.jisho.textosJapones.model.entities.comicinfo.AgeRating
import java.util.*
import java.util.List
import javax.xml.bind.annotation.*

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ComicInfo")
data class ComicInfo(
    private var id: UUID? = null,
    var idMal: Long? = null,
    var comic: String? = null,
    @XmlElement(name = "Title")
    var title: String? = null,
    @XmlElement(name = "Series")
    var series: String? = null,
    @XmlElement(name = "Number")
    var number: Float? = null,
    @XmlElement(name = "Volume")
    var volume: Int? = null,
    @XmlElement(name = "Notes")
    var notes: String? = null,
    @XmlElement(name = "Year")
    var year: Int? = null,
    @XmlElement(name = "Month")
    var month: Int? = null,
    @XmlElement(name = "Day")
    var day: Int? = null,
    @XmlElement(name = "Writer")
    var writer: String? = null,
    @XmlElement(name = "Penciller")
    var penciller: String? = null,
    @XmlElement(name = "Inker")
    var inker: String? = null,
    @XmlElement(name = "CoverArtist")
    var coverArtist: String? = null,
    @XmlElement(name = "Colorist")
    var colorist: String? = null,
    @XmlElement(name = "Letterer")
    var letterer: String? = null,
    @XmlElement(name = "Publisher")
    var publisher: String? = null,
    @XmlElement(name = "Tags")
    var tags: String? = null,
    @XmlElement(name = "Web")
    var web: String? = null,
    @XmlElement(name = "Editor")
    var editor: String? = null,
    @XmlElement(name = "Translator")
    var translator: String? = null,
    @XmlElement(name = "PageCount")
    var pageCount: Int? = null,
    @XmlElementWrapper(name = "Pages")
    @XmlElement(name = "Page")
    var pages: List<Pages>? = null,
    @XmlElement(name = "Count")
    var count: Int? = null,
    @XmlElement(name = "AlternateSeries")
    var alternateSeries: String? = null,
    @XmlElement(name = "AlternateNumber")
    var alternateNumber: Float? = null,
    @XmlElement(name = "StoryArc")
    var storyArc: String? = null,
    @XmlElement(name = "StoryArcNumber")
    var storyArcNumber: String? = null,
    @XmlElement(name = "SeriesGroup")
    var seriesGroup: String? = null,
    @XmlElement(name = "AlternateCount")
    var alternateCount: Int? = null,
    @XmlElement(name = "Summary")
    var summary: String? = null,
    @XmlElement(name = "Imprint")
    var imprint: String? = null,
    @XmlElement(name = "Genre")
    var genre: String? = null,
    @XmlElement(name = "LanguageISO")
    var languageISO: String? = null,
    @XmlElement(name = "Format")
    var format: String? = null,
    @XmlElement(name = "AgeRating")
    var ageRating: AgeRating? = null,
    @XmlElement(name = "CommunityRating")
    var communityRating: Float? = null,
    @XmlElement(name = "BlackAndWhite")
    var blackAndWhite: YesNo? = null,
    @XmlElement(name = "Manga")
    var manga: Manga? = null,
    @XmlElement(name = "Characters")
    var characters: String? = null,
    @XmlElement(name = "Teams")
    var teams: String? = null,
    @XmlElement(name = "Locations")
    var locations: String? = null,
    @XmlElement(name = "ScanInformation")
    var scanInformation: String? = null,
    @XmlElement(name = "MainCharacterOrTeam")
    var mainCharacterOrTeam: String? = null,
    @XmlElement(name = "Review")
    var review: String? = null,
) : EntityBase<UUID?, ComicInfo>() {

    constructor(
        id: UUID?, idMal: Long?, comic: String?, title: String?, series: String?, publisher: String?, alternateSeries: String?,
        storyArc: String?, seriesGroup: String?, imprint: String?, genre: String?, languageISO: String?,
        ageRating: AgeRating?,
    ) : this(id, idMal, comic, title, series) {
        this.publisher = publisher
        this.alternateSeries = alternateSeries
        this.storyArc = storyArc
        this.seriesGroup = seriesGroup
        this.imprint = imprint
        this.genre = genre
        this.languageISO = languageISO
        this.ageRating = ageRating
    }

    override fun getId(): UUID? = id

    override fun create(id: UUID?): ComicInfo = ComicInfo(id)

    fun setId(id: UUID?) {
        this.id = id
    }

}