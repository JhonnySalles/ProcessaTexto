CREATE TABLE exclusao (
    id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    palavra VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( palavra )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE revisar (
    id VARCHAR(36) DEFAULT NULL,
    vocabulario VARCHAR(250) NOT NULL,
    leitura VARCHAR(250) DEFAULT NULL,
    portugues LONGTEXT,
    revisado TINYINT(1) DEFAULT '0',
    aparece INT DEFAULT '0',
    isanime TINYINT(1) DEFAULT '0',
    ismanga TINYINT(1) DEFAULT '0',
    isnovel TINYINT(1) DEFAULT '0',
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( vocabulario ),
    KEY vocabulario ( vocabulario )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE valido (
    id VARCHAR(36) DEFAULT NULL,
    palavra VARCHAR(250) NOT NULL,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( palavra )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE vocabulario (
    id VARCHAR(36) DEFAULT NULL,
    vocabulario VARCHAR(250) NOT NULL,
    leitura VARCHAR(250) DEFAULT NULL,
    portugues LONGTEXT,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( vocabulario ),
    KEY vocabulario ( vocabulario )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;