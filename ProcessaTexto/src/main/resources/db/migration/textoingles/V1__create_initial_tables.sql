CREATE TABLE Exclusao (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    palavra     VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    atualizacao DATETIME                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (palavra)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Revisar (
    id          VARCHAR(36)  DEFAULT NULL,
    vocabulario VARCHAR(250) NOT NULL,
    leitura     VARCHAR(250) DEFAULT NULL,
    portugues   LONGTEXT,
    revisado    TINYINT(1)   DEFAULT NULL,
    aparece     INT          DEFAULT NULL,
    isAnime     TINYINT(1)   DEFAULT '0',
    isManga     TINYINT(1)   DEFAULT '0',
    isNovel     TINYINT(1)   DEFAULT '0',
    atualizacao DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vocabulario),
    KEY Vocabulario (vocabulario)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Sincronizacao (
    id          INT                                                                                                                                  NOT NULL AUTO_INCREMENT,
    conexao     ENUM ('MANGA_EXTRACTOR','NOVEL_EXTRACTOR','TEXTO_INGLES','DECKSUBTITLE','FIREBASE') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    envio       DATETIME DEFAULT NULL,
    recebimento DATETIME DEFAULT NULL,
    PRIMARY KEY (id, conexao)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Valido (
    id          VARCHAR(36) DEFAULT NULL,
    palavra     VARCHAR(250) NOT NULL,
    atualizacao DATETIME    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (palavra)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Vocabulario (
    id          VARCHAR(36)  DEFAULT NULL,
    vocabulario VARCHAR(250) NOT NULL,
    leitura     VARCHAR(250) DEFAULT NULL,
    portugues   LONGTEXT,
    atualizacao DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vocabulario),
    KEY Vocabulario (vocabulario)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;