CREATE TABLE ComicInfo
(
    id                VARCHAR(36) NOT NULL,
    comic             VARCHAR(250) DEFAULT NULL,
    idMal             INT          DEFAULT NULL,
    series            VARCHAR(900) DEFAULT NULL,
    title             VARCHAR(900) DEFAULT NULL,
    publisher         VARCHAR(300) DEFAULT NULL,
    genre             VARBINARY(900) DEFAULT NULL,
    imprint           VARCHAR(300) DEFAULT NULL,
    seriesGroup       VARCHAR(900) DEFAULT NULL,
    storyArc          VARCHAR(900) DEFAULT NULL,
    maturityRating    VARCHAR(100) DEFAULT NULL,
    alternativeSeries VARCHAR(900) DEFAULT NULL,
    LANGUAGE          VARBINARY(3) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY               CAMPOS (comic, LANGUAGE)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb3;

CREATE TABLE Conexoes
(
    id       INT NOT NULL AUTO_INCREMENT,
    tipo     ENUM('PROCESSA_TEXTO','MANGA_EXTRACTOR','NOVEL_EXTRACTOR','TEXTO_INGLES','TEXTO_JAPONES','DECKSUBTITLE','FIREBASE') CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT 'PROCESSA_TEXTO',
    url      VARCHAR(250) DEFAULT NULL,
    username VARCHAR(250) DEFAULT NULL,
    PASSWORD VARCHAR(250) DEFAULT NULL,
    base     VARCHAR(100) DEFAULT NULL,
    driver   ENUM('MYSQL','EXTERNO') CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT 'MYSQL',
    PRIMARY KEY (id),
    UNIQUE KEY tipo (tipo)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

CREATE TABLE Fila_Sql
(
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    sequencial  INT NOT NULL AUTO_INCREMENT,
    select_sql  LONGTEXT CHARACTER SET latin1 COLLATE latin1_swedish_ci,
    update_sql  LONGTEXT CHARACTER SET latin1 COLLATE latin1_swedish_ci,
    delete_sql  LONGTEXT CHARACTER SET latin1 COLLATE latin1_swedish_ci,
    vocabulario LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    linguagem   VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    isExporta   TINYINT(1) DEFAULT '0',
    isLimpeza   TINYINT(1) DEFAULT '0',
    atualizacao DATETIME                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sequencial)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DELIMITER $$

DROP TRIGGER IF EXISTS tr_fila_sql_insert$$

CREATE
    TRIGGER tr_fila_sql_insert
    BEFORE INSERT
    ON Fila_Sql
    FOR EACH ROW
BEGIN
    IF (NEW.id IS NULL OR NEW.id = '') THEN
		SET new.id = UUID();
END IF;
END;
$$

DROP TRIGGER IF EXISTS  tr_fila_sql_update $$

CREATE
    TRIGGER tr_fila_sql_update
    BEFORE UPDATE
    ON Fila_Sql
    FOR EACH ROW
BEGIN
    SET new.atualizacao = NOW();
END;
$$

DELIMITER ;
