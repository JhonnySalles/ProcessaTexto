package br.com.fenix.processatexto.model.entities.comicinfo

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.comicinfo.Manga
import br.com.fenix.processatexto.model.enums.comicinfo.YesNo
import jakarta.persistence.*
import jakarta.xml.bind.annotation.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@Entity
@Table(name = "comicinfo")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ComicInfo")
data class ComicInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ID", nullable = false, unique = true, length = 36)
    @field:XmlElement(name = "id")
    private var id: UUID? = null,
    @XmlTransient
    @Column(name = "idMal", nullable = true)
    var idMal: Long? = null,
    @field:XmlElement(name = "comic")
    @Column(name = "comic", length = 250, nullable = true)
    var comic: String = "",
    @Column(name = "title", length = 900, nullable = true)
    @field:XmlElement(name = "Title")
    var title: String = "",
    @Column(name = "series", length = 900, nullable = true)
    @field:XmlElement(name = "Series")
    var series: String = "",
    @Transient
    @field:XmlElement(name = "Number")
    var number: Float = 0f,
    @Transient
    @field:XmlElement(name = "Volume")
    var volume: Int = 0,
    @Transient
    @field:XmlElement(name = "Notes")
    var notes: String? = null,
    @Transient
    @field:XmlElement(name = "Year")
    var year: Int? = null,
    @Transient
    @field:XmlElement(name = "Month")
    var month: Int? = null,
    @Transient
    @field:XmlElement(name = "Day")
    var day: Int? = null,
    @Transient
    @field:XmlElement(name = "Writer")
    var writer: String? = null,
    @Transient
    @field:XmlElement(name = "Penciller")
    var penciller: String? = null,
    @Transient
    @field:XmlElement(name = "Inker")
    var inker: String? = null,
    @Transient
    @field:XmlElement(name = "CoverArtist")
    var coverArtist: String? = null,
    @Transient
    @field:XmlElement(name = "Colorist")
    var colorist: String? = null,
    @Transient
    @field:XmlElement(name = "Letterer")
    var letterer: String? = null,
    @Column(name = "publisher", length = 300, nullable = true)
    @field:XmlElement(name = "Publisher")
    var publisher: String? = null,
    @Transient
    @field:XmlElement(name = "Tags")
    var tags: String? = null,
    @Transient
    @field:XmlElement(name = "Web")
    var web: String? = null,
    @Transient
    @field:XmlElement(name = "Editor")
    var editor: String? = null,
    @Transient
    @field:XmlElement(name = "Translator")
    var translator: String? = null,
    @Transient
    @field:XmlElement(name = "PageCount")
    var pageCount: Int? = null,
    @Transient
    @XmlElementWrapper(name = "Pages")
    @field:XmlElement(name = "Page")
    var pages: List<Pages>? = null,
    @Transient
    @field:XmlElement(name = "Count")
    var count: Int? = null,
    @Column(name = "alternativeSeries", length = 900, nullable = true)
    @field:XmlElement(name = "AlternateSeries")
    var alternateSeries: String? = null,
    @Transient
    @field:XmlElement(name = "AlternateNumber")
    var alternateNumber: Float? = null,
    @Column(name = "storyArc", length = 900, nullable = true)
    @field:XmlElement(name = "StoryArc")
    var storyArc: String? = null,
    @Transient
    @field:XmlElement(name = "StoryArcNumber")
    var storyArcNumber: String? = null,
    @Column(name = "seriesGroup", length = 900, nullable = true)
    @field:XmlElement(name = "SeriesGroup")
    var seriesGroup: String? = null,
    @Transient
    @field:XmlElement(name = "AlternateCount")
    var alternateCount: Int? = null,
    @Transient
    @field:XmlElement(name = "Summary")
    var summary: String? = null,
    @Column(name = "imprint", length = 300, nullable = true)
    @field:XmlElement(name = "Imprint")
    var imprint: String? = null,
    @Column(name = "genre", length = 900, nullable = true)
    @field:XmlElement(name = "Genre")
    var genre: String? = null,
    @Column(name = "language", length = 3, nullable = true)
    @field:XmlElement(name = "LanguageISO")
    var languageISO: String = "",
    @Transient
    @field:XmlElement(name = "Format")
    var format: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "maturityRating", length = 100, nullable = true)
    @field:XmlElement(name = "AgeRating")
    var ageRating: AgeRating? = null,
    @Transient
    @field:XmlElement(name = "CommunityRating")
    var communityRating: Float? = null,
    @Transient
    @field:XmlElement(name = "BlackAndWhite")
    var blackAndWhite: YesNo? = null,
    @Transient
    @field:XmlElement(name = "Manga")
    var manga: Manga = Manga.Yes,
    @Transient
    @field:XmlElement(name = "Characters")
    var characters: String? = null,
    @Transient
    @field:XmlElement(name = "Teams")
    var teams: String? = null,
    @Transient
    @field:XmlElement(name = "Locations")
    var locations: String? = null,
    @Transient
    @field:XmlElement(name = "ScanInformation")
    var scanInformation: String? = null,
    @Transient
    @field:XmlElement(name = "MainCharacterOrTeam")
    var mainCharacterOrTeam: String? = null,
    @Transient
    @field:XmlElement(name = "Review")
    var review: String? = null,
) : EntityBase<UUID?, ComicInfo>() {

    constructor(
        id: UUID?, idMal: Long?, comic: String, title: String, series: String, publisher: String?, alternateSeries: String?,
        storyArc: String?, seriesGroup: String?, imprint: String?, genre: String?, languageISO: String,
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

    constructor(obj: HashMap<String, Any?>) : this(UUID.fromString(obj["id"] as String), null, obj["comic"] as String, obj["title"] as String, obj["series"] as String)   {
        id = UUID.fromString(obj["id"] as String)
        publisher = obj["publisher"] as String?
        alternateSeries = obj["alternateSeries"] as String?
        storyArc = obj["storyArc"] as String?
        seriesGroup = obj["seriesGroup"] as String?
        imprint = obj["imprint"] as String?
        genre = obj["genre"] as String?
        languageISO = obj["languageISO"] as String

        if (obj.containsKey("idMal"))
            idMal = (obj["idMal"] as Double).toLong()
        if (obj.containsKey("ageRating"))
            ageRating = AgeRating.valueOf((obj["ageRating"] as String))
    }

    override fun getId(): UUID? = id

    override fun create(id: UUID?): ComicInfo = ComicInfo(id)

    override fun setId(id: UUID?) {
        this.id = id
    }

    fun merge(comic: ComicInfo) {
        this.id = comic.id
        this.idMal = comic.idMal
        this.comic = comic.comic
        this.title = comic.title
        this.series = comic.series
        this.publisher = comic.publisher
        this.alternateSeries = comic.alternateSeries
        this.storyArc = comic.storyArc
        this.seriesGroup = comic.seriesGroup
        this.imprint = comic.imprint
        this.genre = comic.genre
        this.languageISO = comic.languageISO
        this.ageRating = comic.ageRating
    }

}