CREATE TABLE _sql
(
    id          varchar(36) NOT NULL,
    tipo        enum ('INSERT','SELECT','CREATE','UPDATE','DELETE') DEFAULT NULL,
    descricao   varchar(250)                                        DEFAULT NULL,
    texto       longtext,
    Atualizacao datetime                                            DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;


CREATE TABLE Exemplo_Volumes
(
    id                 varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    novel              varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    serie              varchar(250)                                                  DEFAULT NULL,
    titulo             varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    titulo_alternativo varchar(250)                                                  DEFAULT NULL,
    descricao          longtext,
    arquivo            varchar(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    editora            varchar(250)                                                  DEFAULT NULL,
    autor              varchar(900)                                                  DEFAULT NULL,
    volume             float                                                         DEFAULT NULL,
    linguagem          varchar(4)                                                    DEFAULT NULL,
    is_favorito        tinyint(1)                                                    DEFAULT '0',
    is_processado      tinyint(1)                                                    DEFAULT '0',
    atualizacao        datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Exemplo_Capas
(
    id          varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume   varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    novel       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci    NOT NULL,
    volume      float                                                        NOT NULL,
    linguagem   varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    capa        longblob,
    atualizacao datetime                                                    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_volumes_fk (id_volume),
    CONSTRAINT exemplo_volumes_capas_fk FOREIGN KEY (id_volume) REFERENCES exemplo_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exemplo_Capitulos
(
    id            varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume     varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    novel         longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci    NOT NULL,
    volume        float                                                        NOT NULL,
    capitulo      varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    descricao     varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    sequencia     int                                                           DEFAULT NULL,
    linguagem     varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    is_processado tinyint(1)                                                    DEFAULT '0',
    atualizacao   datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_volumes_fk (id_volume),
    CONSTRAINT exemplo_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES exemplo_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exemplo_Textos
(
    id          varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_capitulo varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    sequencia   int      DEFAULT NULL,
    texto       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    atualizacao datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_capitulos_fk (id_capitulo),
    CONSTRAINT exemplo_capitulos_textos_fk FOREIGN KEY (id_capitulo) REFERENCES exemplo_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exemplo_Vocabularios
(
    id          varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume   varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    id_capitulo varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    palavra     varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    portugues   longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    ingles      longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    leitura     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado    tinyint(1)                                                    DEFAULT '1',
    atualizacao datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_vocab_volume_fk (id_volume),
    KEY exemplo_vocab_capitulo_fk (id_capitulo),
    CONSTRAINT exemplo_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES exemplo_capitulos (id),
    CONSTRAINT exemplo_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES exemplo_volumes (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
