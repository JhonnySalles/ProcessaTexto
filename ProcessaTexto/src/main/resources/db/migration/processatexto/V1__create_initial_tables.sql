CREATE TABLE comicinfo (
    id                VARCHAR(36) NOT NULL,
    comic             VARCHAR(250) DEFAULT NULL,
    idmal             INT          DEFAULT NULL,
    series            VARCHAR(900) DEFAULT NULL,
    title             VARCHAR(900) DEFAULT NULL,
    publisher         VARCHAR(300) DEFAULT NULL,
    genre             varbinary(900) DEFAULT NULL,
    imprint           VARCHAR(300) DEFAULT NULL,
    seriesgroup       VARCHAR(900) DEFAULT NULL,
    storyarc          VARCHAR(900) DEFAULT NULL,
    maturityrating    VARCHAR(100) DEFAULT NULL,
    alternativeseries VARCHAR(900) DEFAULT NULL,
    language          varbinary(3) DEFAULT NULL,
    PRIMARY KEY ( id ),
    key               campos (comic, LANGUAGE)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb3;

CREATE TABLE conexoes (
    id       INT NOT NULL AUTO_INCREMENT,
    tipo     enum('PROCESSA_TEXTO','MANGA_EXTRACTOR','NOVEL_EXTRACTOR','TEXTO_INGLES','TEXTO_JAPONES','DECKSUBTITLE','FIREBASE') CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT 'PROCESSA_TEXTO',
    url      VARCHAR(250) DEFAULT NULL,
    username VARCHAR(250) DEFAULT NULL,
    password VARCHAR(250) DEFAULT NULL,
    base     VARCHAR(100) DEFAULT NULL,
    driver   enum('MYSQL','EXTERNO') CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT 'MYSQL',
    PRIMARY KEY ( id ),
    UNIQUE KEY tipo (tipo)
) ENGINE=INNODB DEFAULT CHARSET=latin1;

CREATE TABLE sincronizacao (
    conexao     enum('MANGA_EXTRACTOR','NOVEL_EXTRACTOR','TEXTO_INGLES','TEXTO_JAPONES','DECKSUBTITLE','FIREBASE') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    envio       datetime DEFAULT NULL,
    recebimento datetime DEFAULT NULL,
    PRIMARY KEY ( conexao )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;
