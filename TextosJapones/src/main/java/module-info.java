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

	requires transitive java.sql;
	requires transitive com.jfoenix;
	requires javafx.base;
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires kuromoji.core;
	requires kuromoji.ipadic;
	requires transitive sudachi;
	requires AnimateFX;
	requires transitive org.controlsfx.controls;
	requires org.jsoup;
	requires com.google.common;
	requires nativejavafx.taskbar;
	requires javafx.web;
	requires com.google.gson;
	requires org.json;
	requires java.desktop;
	requires java.base;

	opens org.jisho.textosJapones.controller to javafx.fxml, javafx.graphics;
	opens org.jisho.textosJapones.model.entities to javafx.base, com.google.gson;
	opens org.jisho.textosJapones.util.processar to com.google.gson;
}