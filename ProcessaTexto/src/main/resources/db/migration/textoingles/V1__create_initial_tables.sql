CREATE TABLE exclusao (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    palavra     VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    atualizacao datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( palavra )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE revisar (
    id          VARCHAR(36)  DEFAULT NULL,
    vocabulario VARCHAR(250) NOT NULL,
    leitura     VARCHAR(250) DEFAULT NULL,
    portugues   longtext,
    revisado    tinyint(1) DEFAULT NULL,
    aparece     INT          DEFAULT NULL,
    isanime     tinyint(1) DEFAULT '0',
    ismanga     tinyint(1) DEFAULT '0',
    isnovel     tinyint(1) DEFAULT '0',
    atualizacao datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( vocabulario ),
    key         vocabulario (vocabulario)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE sincronizacao (
    id          INT NOT NULL AUTO_INCREMENT,
    conexao     enum ('MANGA_EXTRACTOR','NOVEL_EXTRACTOR','TEXTO_INGLES','DECKSUBTITLE','FIREBASE') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    envio       datetime DEFAULT NULL,
    recebimento datetime DEFAULT NULL,
    PRIMARY KEY ( id, conexao )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE valido (
    id          VARCHAR(36) DEFAULT NULL,
    palavra     VARCHAR(250) NOT NULL,
    atualizacao datetime    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( palavra )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE vocabulario (
    id          VARCHAR(36)  DEFAULT NULL,
    vocabulario VARCHAR(250) NOT NULL,
    leitura     VARCHAR(250) DEFAULT NULL,
    portugues   longtext,
    atualizacao datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( vocabulario ),
    key         vocabulario (vocabulario)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;