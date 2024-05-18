CREATE TABLE _sql (
    id          VARCHAR(36) NOT NULL,
    tipo        enum ('INSERT','SELECT','CREATE','UPDATE','DELETE') DEFAULT NULL,
    descricao   VARCHAR(250) DEFAULT NULL,
    texto       longtext,
    atualizacao datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

INSERT INTO `_sql` (id, tipo, descricao, texto, Atualizacao)
VALUES ('cb43444b-33c4-11ee-ad88-309c231b7fe8', 'DELETE', 'Delete a partir de volume',
    'DELETE vol FROM %s_vocabulario AS vol INNER JOIN %s_paginas AS p ON p.id = vol.id_pagina \r\nINNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE vol FROM %s_vocabularios AS vol INNER JOIN %s_capitulos AS c ON c.id = vol.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE vol FROM %s_vocabularios AS vol INNER JOIN %s_volumes AS v ON v.id = vol.id_volume WHERE v.id = 1;\r\n\r\n\r\n\r\nDELETE t FROM %s_textos AS t INNER JOIN %s_paginas AS p ON p.id = t.id_pagina \r\nINNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE p FROM %s_paginas p INNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE c FROM %s_capitulos AS c INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE v FROM %s_volumes AS v WHERE v.id = 1;',
    '2023-08-05 16:17:57');

CREATE TABLE _vocabularios (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    base        VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    manga       VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    volume      INT                                                           DEFAULT '0',
    capitulo    DOUBLE                                                        DEFAULT '0',
    palavra     VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    portugues   longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    ingles      longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    leitura     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado    tinyint(1) DEFAULT '1',
    atualizacao datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE exemplo_volumes (
    id            VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    manga         VARCHAR(250) DEFAULT NULL,
    volume        INT          DEFAULT NULL,
    linguagem     VARCHAR(4)   DEFAULT NULL,
    arquivo       VARCHAR(250) DEFAULT NULL,
    vocabulario   longtext,
    is_processado tinyint(1) DEFAULT '0',
    atualizacao   datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE exemplo_capas (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume   VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    manga       VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    volume      INT                                     DEFAULT NULL,
    linguagem   VARCHAR(4) COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    arquivo     VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    extensao    VARCHAR(10) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    capa        longblob,
    atualizacao datetime                                DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    key         exemplo_volumes_fk (id_volume),
    CONSTRAINT exemplo_volumes_capas_fk FOREIGN KEY ( id_volume ) REFERENCES exemplo_volumes ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE exemplo_capitulos (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    id_volume   VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    manga       VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    volume      INT                                                           NOT NULL,
    capitulo    DOUBLE                                                        NOT NULL,
    linguagem   VARCHAR(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    scan        VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    is_extra    tinyint(1) DEFAULT NULL,
    is_raw      tinyint(1) DEFAULT NULL,
    vocabulario longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    atualizacao datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    key         exemplo_volumes_fk (id_volume),
    CONSTRAINT exemplo_volumes_capitulos_fk FOREIGN KEY ( id_volume ) REFERENCES exemplo_volumes ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE exemplo_paginas (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    nome        VARCHAR(250) DEFAULT NULL,
    numero      VARCHAR(36)  DEFAULT NULL,
    hash_pagina VARCHAR(250) DEFAULT NULL,
    vocabulario longtext,
    atualizacao datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    key         exemplo_capitulos_fk (id_capitulo),
    CONSTRAINT exemplo_capitulos_paginas_fk FOREIGN KEY ( id_capitulo ) REFERENCES exemplo_capitulos ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE exemplo_textos (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_pagina   VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    sequencia   INT                                                          DEFAULT NULL,
    texto       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    posicao_x1  DOUBLE                                                       DEFAULT NULL,
    posicao_y1  DOUBLE                                                       DEFAULT NULL,
    posicao_x2  DOUBLE                                                       DEFAULT NULL,
    posicao_y2  DOUBLE                                                       DEFAULT NULL,
    versao_app  VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0',
    atualizacao datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( id ),
    key         exemplo_paginas_fk (id_pagina),
    CONSTRAINT exemplo_paginas_textos_fk FOREIGN KEY ( id_pagina ) REFERENCES exemplo_paginas ( id ) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE exemplo_vocabularios (
    id_vocabulario VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    id_volume      VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    id_capitulo    VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    id_pagina      VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    atualizacao    datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    key            exemplo_vocab_volume_fk (id_volume),
    key            exemplo_vocab_capitulo_fk (id_capitulo),
    key            exemplo_vocab_pagina_fk (id_pagina),
    key            exemplo_vocab_vocabularios_fk (id_vocabulario),
    CONSTRAINT exemplo_vocab_capitulo_fk FOREIGN KEY ( id_capitulo ) REFERENCES exemplo_capitulos ( id ),
    CONSTRAINT exemplo_vocab_pagina_fk FOREIGN KEY ( id_pagina ) REFERENCES exemplo_paginas ( id ),
    CONSTRAINT exemplo_vocab_vocabulario_fk FOREIGN KEY ( id_vocabulario ) REFERENCES _vocabularios (id),
    CONSTRAINT exemplo_vocab_volume_fk FOREIGN KEY ( id_volume ) REFERENCES exemplo_volumes ( id )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
