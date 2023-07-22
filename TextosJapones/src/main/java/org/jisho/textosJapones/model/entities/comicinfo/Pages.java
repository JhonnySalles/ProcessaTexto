package org.jisho.textosJapones.model.entities.comicinfo;

import org.jisho.textosJapones.model.enums.comicinfo.ComicPageType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Page")
public class Pages {

	@XmlAttribute(name = "Bookmark")
	private String bookmark;
	@XmlAttribute(name = "Image")
	private Integer image;
	@XmlAttribute(name = "ImageHeight")
	private Integer imageHeight;
	@XmlAttribute(name = "ImageWidth")
	private Integer imageWidth;
	@XmlAttribute(name = "ImageSize")
	private Long imageSize;
	@XmlAttribute(name = "Type")
	private ComicPageType type;
	@XmlAttribute(name = "DoublePage")
	private Boolean doublePage;
	@XmlAttribute(name = "Key")
	private String key;

	public String getBookmark() {
		return bookmark;
	}

	public void setBookmark(String bookmark) {
		this.bookmark = bookmark;
	}

	public Integer getImage() {
		return image;
	}

	public void setImage(Integer image) {
		this.image = image;
	}

	public Integer getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(Integer imageHeight) {
		this.imageHeight = imageHeight;
	}

	public Integer getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(Integer imageWidth) {
		this.imageWidth = imageWidth;
	}

	public Long getImageSize() {
		return imageSize;
	}

	public void setImageSize(Long imageSize) {
		this.imageSize = imageSize;
	}

	public ComicPageType getType() {
		return type;
	}

	public void setType(ComicPageType type) {
		this.type = type;
	}

	public Boolean getDoublePage() {
		return doublePage;
	}

	public void setDoublePage(Boolean doublePage) {
		this.doublePage = doublePage;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Pages() {

	}

	public Pages(String bookmark, Integer image, Integer imageHeight, Integer imageWidth, Long imageSize,
			ComicPageType type, Boolean doublePage, String key) {
		this.bookmark = bookmark;
		this.image = image;
		this.imageHeight = imageHeight;
		this.imageWidth = imageWidth;
		this.imageSize = imageSize;
		this.type = type;
		this.doublePage = doublePage;
		this.key = key;
	}

}
