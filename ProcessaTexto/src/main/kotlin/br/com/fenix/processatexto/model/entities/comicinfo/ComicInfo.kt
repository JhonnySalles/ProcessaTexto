package br.com.fenix.processatexto.model.entities.comicinfo

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.comicinfo.Manga
import br.com.fenix.processatexto.model.enums.comicinfo.YesNo
import jakarta.persistence.*
import jakarta.xml.bind.annotation.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*
import java.util.List


@Entity
@Table(name = "comicinfo")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ComicInfo")
data class ComicInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ID", nullable = false, unique = true, length = 36)
    private var id: UUID? = null,
    @Column(name = "idMal", nullable = true)
    var idMal: Long? = null,
    @Column(name = "comic", length = 250, nullable = true)
    var comic: String? = null,
    @Column(name = "title", length = 900, nullable = true)
    @XmlElement(name = "Title")
    var title: String? = null,
    @Column(name = "series", length = 900, nullable = true)
    @XmlElement(name = "Series")
    var series: String? = null,
    @Transient
    @XmlElement(name = "Number")
    var number: Float? = null,
    @Transient
    @XmlElement(name = "Volume")
    var volume: Int? = null,
    @Transient
    @XmlElement(name = "Notes")
    var notes: String? = null,
    @Transient
    @XmlElement(name = "Year")
    var year: Int? = null,
    @Transient
    @XmlElement(name = "Month")
    var month: Int? = null,
    @Transient
    @XmlElement(name = "Day")
    var day: Int? = null,
    @Transient
    @XmlElement(name = "Writer")
    var writer: String? = null,
    @Transient
    @XmlElement(name = "Penciller")
    var penciller: String? = null,
    @Transient
    @XmlElement(name = "Inker")
    var inker: String? = null,
    @Transient
    @XmlElement(name = "CoverArtist")
    var coverArtist: String? = null,
    @Transient
    @XmlElement(name = "Colorist")
    var colorist: String? = null,
    @Transient
    @XmlElement(name = "Letterer")
    var letterer: String? = null,
    @Column(name = "publisher", length = 300, nullable = true)
    @XmlElement(name = "Publisher")
    var publisher: String? = null,
    @Transient
    @XmlElement(name = "Tags")
    var tags: String? = null,
    @Transient
    @XmlElement(name = "Web")
    var web: String? = null,
    @Transient
    @XmlElement(name = "Editor")
    var editor: String? = null,
    @Transient
    @XmlElement(name = "Translator")
    var translator: String? = null,
    @Transient
    @XmlElement(name = "PageCount")
    var pageCount: Int? = null,
    @Transient
    @XmlElementWrapper(name = "Pages")
    @XmlElement(name = "Page")
    var pages: List<Pages>? = null,
    @Transient
    @XmlElement(name = "Count")
    var count: Int? = null,
    @Column(name = "alternativeSeries", length = 900, nullable = true)
    @XmlElement(name = "AlternateSeries")
    var alternateSeries: String? = null,
    @Transient
    @XmlElement(name = "AlternateNumber")
    var alternateNumber: Float? = null,
    @Column(name = "storyArc", length = 900, nullable = true)
    @XmlElement(name = "StoryArc")
    var storyArc: String? = null,
    @Transient
    @XmlElement(name = "StoryArcNumber")
    var storyArcNumber: String? = null,
    @Column(name = "seriesGroup", length = 900, nullable = true)
    @XmlElement(name = "SeriesGroup")
    var seriesGroup: String? = null,
    @Transient
    @XmlElement(name = "AlternateCount")
    var alternateCount: Int? = null,
    @Transient
    @XmlElement(name = "Summary")
    var summary: String? = null,
    @Column(name = "imprint", length = 300, nullable = true)
    @XmlElement(name = "Imprint")
    var imprint: String? = null,
    @Column(name = "genre", length = 900, nullable = true)
    @XmlElement(name = "Genre")
    var genre: String? = null,
    @Column(name = "LANGUAGE", length = 3, nullable = true)
    @XmlElement(name = "LanguageISO")
    var languageISO: String? = null,
    @Transient
    @XmlElement(name = "Format")
    var format: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "maturityRating", length = 100, nullable = true)
    @XmlElement(name = "AgeRating")
    var ageRating: AgeRating? = null,
    @Transient
    @XmlElement(name = "CommunityRating")
    var communityRating: Float? = null,
    @Transient
    @XmlElement(name = "BlackAndWhite")
    var blackAndWhite: YesNo? = null,
    @Transient
    @XmlElement(name = "Manga")
    var manga: Manga? = null,
    @Transient
    @XmlElement(name = "Characters")
    var characters: String? = null,
    @Transient
    @XmlElement(name = "Teams")
    var teams: String? = null,
    @Transient
    @XmlElement(name = "Locations")
    var locations: String? = null,
    @Transient
    @XmlElement(name = "ScanInformation")
    var scanInformation: String? = null,
    @Transient
    @XmlElement(name = "MainCharacterOrTeam")
    var mainCharacterOrTeam: String? = null,
    @Transient
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