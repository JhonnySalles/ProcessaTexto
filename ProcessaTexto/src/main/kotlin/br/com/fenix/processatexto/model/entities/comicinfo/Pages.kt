package br.com.fenix.processatexto.model.entities.comicinfo

import br.com.fenix.processatexto.model.enums.comicinfo.ComicPageType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Page")
data class Pages(
    @XmlAttribute(name = "Bookmark")
    var bookmark: String? = null,
    @XmlAttribute(name = "Image")
    var image: Int? = null,
    @XmlAttribute(name = "ImageHeight")
    var imageHeight: Int? = null,
    @XmlAttribute(name = "ImageWidth")
    var imageWidth: Int? = null,
    @XmlAttribute(name = "ImageSize")
    var imageSize: Long? = null,
    @XmlAttribute(name = "Type")
    var type: ComicPageType? = null,
    @XmlAttribute(name = "DoublePage")
    var doublePage: Boolean? = null,
    @XmlAttribute(name = "Key")
    var key: String? = null
) { }