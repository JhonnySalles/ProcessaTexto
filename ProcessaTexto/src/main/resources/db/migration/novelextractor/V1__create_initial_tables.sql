CREATE TABLE _sql (
    id VARCHAR(36) NOT NULL,
    tipo ENUM ('INSERT','SELECT','CREATE','UPDATE','DELETE') DEFAULT NULL,
    descricao VARCHAR(250) DEFAULT NULL,
    texto LONGTEXT,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE _vocabularios (
    id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    base VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    manga VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    volume INT DEFAULT '0',
    capitulo DOUBLE DEFAULT '0',
    palavra VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    portugues LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    ingles LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    leitura LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado TINYINT(1) DEFAULT '1',
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE exemplo_volumes (
    id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    novel VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    serie VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    titulo VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    titulo_alternativo VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    descricao LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    arquivo VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    editora VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    autor VARCHAR(900) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    volume FLOAT DEFAULT NULL,
    linguagem VARCHAR(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    is_favorito TINYINT(1) DEFAULT '0',
    is_processado TINYINT(1) DEFAULT '0',
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE exemplo_capas (
    id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    novel VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    volume FLOAT NOT NULL,
    linguagem VARCHAR(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    arquivo VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    extensao VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    capa LONGBLOB,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    KEY exemplo_volumes_fk ( id_volume ),
    CONSTRAINT exemplo_volumes_capas_fk FOREIGN KEY ( id_volume ) REFERENCES exemplo_volumes ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE exemplo_capitulos (
    id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    novel VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    volume FLOAT NOT NULL,
    capitulo VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    descricao VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    sequencia INT DEFAULT NULL,
    linguagem VARCHAR(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    KEY exemplo_volumes_fk ( id_volume ),
    CONSTRAINT exemplo_volumes_capitulos_fk FOREIGN KEY ( id_volume ) REFERENCES exemplo_volumes ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE exemplo_textos (
    id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    sequencia INT DEFAULT NULL,
    texto LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    KEY exemplo_capitulos_fk ( id_capitulo ),
    CONSTRAINT exemplo_capitulos_textos_fk FOREIGN KEY ( id_capitulo ) REFERENCES exemplo_capitulos ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE exemplo_vocabularios (
    id_vocabulario VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id_vocabulario ),
    KEY exemplo_vocab_volume_fk ( id_volume ),
    KEY exemplo_vocab_capitulo_fk ( id_capitulo ),
    KEY exemplo_vocab_vocabularios_fk ( id_vocabulario ),
    CONSTRAINT exemplo_vocab_capitulo_fk FOREIGN KEY ( id_capitulo ) REFERENCES exemplo_capitulos ( id ),
    CONSTRAINT exemplo_vocab_vocabulario_fk FOREIGN KEY ( id_vocabulario ) REFERENCES _vocabularios ( id ),
    CONSTRAINT exemplo_vocab_volume_fk FOREIGN KEY ( id_volume ) REFERENCES exemplo_volumes ( id )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
