/**
 * @author Jhonny
 *
 */
module TextosJapones {
    exports org.jisho.textosJapones;
    exports org.jisho.textosJapones.controller;
    exports org.jisho.textosJapones.controller.legendas;
    exports org.jisho.textosJapones.controller.mangas;
    exports org.jisho.textosJapones.controller.novels;
    exports org.jisho.textosJapones.database.dao;
    exports org.jisho.textosJapones.database.dao.implement;
    exports org.jisho.textosJapones.database.mysql;
    exports org.jisho.textosJapones.components.animation;
    exports org.jisho.textosJapones.model.services;
    exports org.jisho.textosJapones.model.enums;
    exports org.jisho.textosJapones.model.entities;
    exports org.jisho.textosJapones.components.listener;
    exports org.jisho.textosJapones.fileparse;
    exports org.jisho.textosJapones.model.enums.comicinfo;
    exports org.jisho.textosJapones.model.entities.comicinfo;
    exports org.jisho.textosJapones.logback;

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
    requires junrar;
    requires org.apache.commons.compress;
    requires java.xml.bind;
    requires Mal4J;
    requires java.net.http;
    requires org.slf4j;
    requires logback.classic;
    requires logback.core;

    opens org.jisho.textosJapones.model.entities.comicinfo to java.xml.bind;
    opens org.jisho.textosJapones.model.enums.comicinfo to java.xml.bind;
    opens org.jisho.textosJapones.controller to javafx.fxml, javafx.graphics;
    opens org.jisho.textosJapones.controller.mangas to javafx.fxml, javafx.graphics;
    opens org.jisho.textosJapones.controller.legendas to javafx.fxml, javafx.graphics;
    opens org.jisho.textosJapones.controller.novels to javafx.fxml, javafx.graphics;
    opens org.jisho.textosJapones.model.entities to javafx.base, com.google.gson;
    opens org.jisho.textosJapones.processar to com.google.gson;
    exports org.jisho.textosJapones.model.entities.mangaextractor;
    opens org.jisho.textosJapones.model.entities.mangaextractor to com.google.gson, javafx.base;
}