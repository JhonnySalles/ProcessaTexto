package org.jisho.textosJapones.model.entities.comicinfo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jisho.textosJapones.model.enums.comicinfo.Manga;
import org.jisho.textosJapones.model.enums.comicinfo.YesNo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ComicInfo")
public class ComicInfo {

	@XmlElement(name = "Title")
	private String title;
	@XmlElement(name = "Series")
	private String series;
	@XmlElement(name = "Number")
	private Float number;
	@XmlElement(name = "Volume")
	private Integer volume;
	@XmlElement(name = "Notes")
	private String notes;
	@XmlElement(name = "Year")
	private Integer year;
	@XmlElement(name = "Month")
	private Integer month;
	@XmlElement(name = "Day")
	private Integer day;
	@XmlElement(name = "Writer")
	private String writer;
	@XmlElement(name = "Penciller")
	private String penciller;
	@XmlElement(name = "Inker")
	private String inker;
	@XmlElement(name = "CoverArtist")
	private String coverArtist;
	@XmlElement(name = "Colorist")
	private String colorist;
	@XmlElement(name = "Letterer")
	private String letterer;
	@XmlElement(name = "Publisher")
	private String publisher;
	@XmlElement(name = "Tags")
	private String tags;
	@XmlElement(name = "Web")
	private String web;
	@XmlElement(name = "Editor")
	private String editor;
	@XmlElement(name = "Translator")
	private String translator;
	@XmlElement(name = "PageCount")
	private Integer pageCount;
	@XmlElementWrapper(name="Pages")
	@XmlElement(name="Page")
	private List<Pages> pages;
	@XmlElement(name = "Count")
	private Integer count;
	@XmlElement(name = "AlternateSeries")
	private String alternateSeries;
	@XmlElement(name = "AlternateNumber")
	private Float alternateNumber;
	@XmlElement(name = "StoryArc")
	private String storyArc;
	@XmlElement(name = "StoryArcNumber")
	private String storyArcNumber;
	@XmlElement(name = "SeriesGroup")
	private String seriesGroup;
	@XmlElement(name = "AlternateCount")
	private Integer alternateCount;
	@XmlElement(name = "Summary")
	private String summary;
	@XmlElement(name = "Imprint")
	private String imprint;
	@XmlElement(name = "Genre")
	private String genre;
	@XmlElement(name = "LanguageISO")
	private String languageISO;
	@XmlElement(name = "Format")
	private String format;
	@XmlElement(name = "AgeRating")
	private AgeRating ageRating;
	@XmlElement(name = "CommunityRating")
	private Float communityRating;
	@XmlElement(name = "BlackAndWhite")
	private YesNo blackAndWhite;
	@XmlElement(name = "Manga")
	private Manga manga;
	@XmlElement(name = "Characters")
	private String characters;
	@XmlElement(name = "Teams")
	private String teams;
	@XmlElement(name = "Locations")
	private String locations;
	@XmlElement(name = "ScanInformation")
	private String scanInformation;
	@XmlElement(name = "MainCharacterOrTeam")
	private String mainCharacterOrTeam;
	@XmlElement(name = "Review")
	private String review;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public Float getNumber() {
		return number;
	}

	public void setNumber(Float number) {
		this.number = number;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getPenciller() {
		return penciller;
	}

	public void setPenciller(String penciller) {
		this.penciller = penciller;
	}

	public String getInker() {
		return inker;
	}

	public void setInker(String inker) {
		this.inker = inker;
	}

	public String getCoverArtist() {
		return coverArtist;
	}

	public void setCoverArtist(String coverArtist) {
		this.coverArtist = coverArtist;
	}

	public String getColorist() {
		return colorist;
	}

	public void setColorist(String colorist) {
		this.colorist = colorist;
	}

	public String getLetterer() {
		return letterer;
	}

	public void setLetterer(String letterer) {
		this.letterer = letterer;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public String getTranslator() {
		return translator;
	}

	public void setTranslator(String translator) {
		this.translator = translator;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public List<Pages> getPages() {
		return pages;
	}

	public void setPages(List<Pages> pages) {
		this.pages = pages;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getAlternateSeries() {
		return alternateSeries;
	}

	public void setAlternateSeries(String alternateSeries) {
		this.alternateSeries = alternateSeries;
	}

	public Float getAlternateNumber() {
		return alternateNumber;
	}

	public void setAlternateNumber(Float alternateNumber) {
		this.alternateNumber = alternateNumber;
	}

	public String getStoryArc() {
		return storyArc;
	}

	public void setStoryArc(String storyArc) {
		this.storyArc = storyArc;
	}

	public String getStoryArcNumber() {
		return storyArcNumber;
	}

	public void setStoryArcNumber(String storyArcNumber) {
		this.storyArcNumber = storyArcNumber;
	}

	public String getSeriesGroup() {
		return seriesGroup;
	}

	public void setSeriesGroup(String seriesGroup) {
		this.seriesGroup = seriesGroup;
	}

	public Integer getAlternateCount() {
		return alternateCount;
	}

	public void setAlternateCount(Integer alternateCount) {
		this.alternateCount = alternateCount;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getImprint() {
		return imprint;
	}

	public void setImprint(String imprint) {
		this.imprint = imprint;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getLanguageISO() {
		return languageISO;
	}

	public void setLanguageISO(String languageISO) {
		this.languageISO = languageISO;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public AgeRating getAgeRating() {
		return ageRating;
	}

	public void setAgeRating(AgeRating ageRating) {
		this.ageRating = ageRating;
	}

	public Float getCommunityRating() {
		return communityRating;
	}

	public void setCommunityRating(Float communityRating) {
		this.communityRating = communityRating;
	}

	public YesNo getBlackAndWhite() {
		return blackAndWhite;
	}

	public void setBlackAndWhite(YesNo blackAndWhite) {
		this.blackAndWhite = blackAndWhite;
	}

	public Manga getManga() {
		return manga;
	}

	public void setManga(Manga manga) {
		this.manga = manga;
	}

	public String getCharacters() {
		return characters;
	}

	public void setCharacters(String characters) {
		this.characters = characters;
	}

	public String getTeams() {
		return teams;
	}

	public void setTeams(String teams) {
		this.teams = teams;
	}

	public String getLocations() {
		return locations;
	}

	public void setLocations(String locations) {
		this.locations = locations;
	}

	public String getScanInformation() {
		return scanInformation;
	}

	public void setScanInformation(String scanInformation) {
		this.scanInformation = scanInformation;
	}

	public String getMainCharacterOrTeam() {
		return mainCharacterOrTeam;
	}

	public void setMainCharacterOrTeam(String mainCharacterOrTeam) {
		this.mainCharacterOrTeam = mainCharacterOrTeam;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public ComicInfo() {
	}

	public ComicInfo(String title, String series, Float number, Integer volume, String notes, Integer year,
			Integer month, Integer day, String writer, String penciller, String inker, String coverArtist,
			String colorist, String letterer, String publisher, String tags, String web, String editor,
			String translator, Integer pageCount, List<Pages> pages, Integer count, String alternateSeries,
			Float alternateNumber, String storyArc, String storyArcNumber, String seriesGroup, Integer alternateCount,
			String summary, String imprint, String genre, String languageISO, String format, AgeRating ageRating,
			Float communityRating, YesNo blackAndWhite, Manga manga, String characters, String teams, String locations,
			String scanInformation, String mainCharacterOrTeam, String review) {
		this.title = title;
		this.series = series;
		this.number = number;
		this.volume = volume;
		this.notes = notes;
		this.year = year;
		this.month = month;
		this.day = day;
		this.writer = writer;
		this.penciller = penciller;
		this.inker = inker;
		this.coverArtist = coverArtist;
		this.colorist = colorist;
		this.letterer = letterer;
		this.publisher = publisher;
		this.tags = tags;
		this.web = web;
		this.editor = editor;
		this.translator = translator;
		this.pageCount = pageCount;
		this.pages = pages;
		this.count = count;
		this.alternateSeries = alternateSeries;
		this.alternateNumber = alternateNumber;
		this.storyArc = storyArc;
		this.storyArcNumber = storyArcNumber;
		this.seriesGroup = seriesGroup;
		this.alternateCount = alternateCount;
		this.summary = summary;
		this.imprint = imprint;
		this.genre = genre;
		this.languageISO = languageISO;
		this.format = format;
		this.ageRating = ageRating;
		this.communityRating = communityRating;
		this.blackAndWhite = blackAndWhite;
		this.manga = manga;
		this.characters = characters;
		this.teams = teams;
		this.locations = locations;
		this.scanInformation = scanInformation;
		this.mainCharacterOrTeam = mainCharacterOrTeam;
		this.review = review;
	}

}
