/**
 * @author Jhonny
 *
 */
module TextosJapones {
	exports org.jisho.textosJapones.controller;
	exports org.jisho.textosJapones.model.dao;
	exports org.jisho.textosJapones.model.dao.impl;
	exports org.jisho.textosJapones.util.mysql;
	exports org.jisho.textosJapones.util.animation;
	exports org.jisho.textosJapones.model.services;
	exports org.jisho.textosJapones.model.enums;
	exports org.jisho.textosJapones;
	exports org.jisho.textosJapones.model.entities;

	requires transitive com.jfoenix;
	requires transitive java.sql;
	requires javafx.base;
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires kuromoji.core;
	requires kuromoji.ipadic;
	requires transitive sudachi;
	requires java.desktop;
	requires AnimateFX;
	requires transitive org.controlsfx.controls;
	requires java.compiler;
	requires org.jsoup;
	requires com.google.common;
	requires nativejavafx.taskbar;
	//requires nativejavafx.taskbar;

	opens org.jisho.textosJapones.controller to javafx.fxml, javafx.graphics;
	opens org.jisho.textosJapones.model.entities to javafx.base;
}