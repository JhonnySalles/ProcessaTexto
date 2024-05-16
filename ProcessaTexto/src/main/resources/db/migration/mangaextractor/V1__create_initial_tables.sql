CREATE TABLE _sql (
    id          varchar(36) NOT NULL,
    tipo        enum ('INSERT','SELECT','CREATE','UPDATE','DELETE') DEFAULT NULL,
    descricao   varchar(250)                                        DEFAULT NULL,
    texto       longtext,
    Atualizacao datetime                                            DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

insert into _sql (id, tipo, descricao, texto, Atualizacao)
values ('cb43444b-33c4-11ee-ad88-309c231b7fe8', 'DELETE', 'Delete a partir de volume',
        'DELETE vol FROM %s_vocabulario AS vol INNER JOIN %s_paginas AS p ON p.id = vol.id_pagina \r\nINNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE vol FROM %s_vocabularios AS vol INNER JOIN %s_capitulos AS c ON c.id = vol.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE vol FROM %s_vocabularios AS vol INNER JOIN %s_volumes AS v ON v.id = vol.id_volume WHERE v.id = 1;\r\n\r\n\r\n\r\nDELETE t FROM %s_textos AS t INNER JOIN %s_paginas AS p ON p.id = t.id_pagina \r\nINNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE p FROM %s_paginas p INNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE c FROM %s_capitulos AS c INNER JOIN %s_volumes AS v ON v.id = c.id_volume WHERE v.id = 1;\r\n\r\nDELETE v FROM %s_volumes AS v WHERE v.id = 1;',
        '2023-08-05 16:17:57');

CREATE TABLE _vocabularios (
    id          varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    base        varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    manga       varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    volume      int                                                           DEFAULT '0',
    capitulo    double                                                        DEFAULT '0',
    palavra     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    portugues   longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    ingles      longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    leitura     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado    tinyint(1)                                                    DEFAULT '1',
    atualizacao datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exemplo_Volumes (
    id            varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    manga         varchar(250) DEFAULT NULL,
    volume        int          DEFAULT NULL,
    linguagem     varchar(4)   DEFAULT NULL,
    arquivo       varchar(250) DEFAULT NULL,
    vocabulario   longtext,
    is_processado tinyint(1)   DEFAULT '0',
    atualizacao   datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Exemplo_Capitulos (
    id            varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume     varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    manga         longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci    NOT NULL,
    volume        int                                                          NOT NULL,
    capitulo      double                                                       NOT NULL,
    linguagem     varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    scan          varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    is_extra      tinyint(1)                                                    DEFAULT NULL,
    is_raw        tinyint(1)                                                    DEFAULT NULL,
    is_processado tinyint(1)                                                    DEFAULT '0',
    vocabulario   longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    atualizacao   datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_volumes_fk (id_volume),
    CONSTRAINT exemplo_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES exemplo_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exemplo_Paginas (
    id            varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_capitulo   varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    nome          varchar(250) DEFAULT NULL,
    numero        varchar(36)  DEFAULT NULL,
    hash_pagina   varchar(250) DEFAULT NULL,
    is_processado tinyint(1)   DEFAULT '0',
    vocabulario   longtext,
    atualizacao   datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_capitulos_fk (id_capitulo),
    CONSTRAINT exemplo_capitulos_paginas_fk FOREIGN KEY (id_capitulo) REFERENCES exemplo_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Exemplo_Textos (
    id          varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_pagina   varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    sequencia   int                                                          DEFAULT NULL,
    texto       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    posicao_x1  double                                                       DEFAULT NULL,
    posicao_y1  double                                                       DEFAULT NULL,
    posicao_x2  double                                                       DEFAULT NULL,
    posicao_y2  double                                                       DEFAULT NULL,
    versao_app  varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0',
    atualizacao datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_paginas_fk (id_pagina),
    CONSTRAINT exemplo_paginas_textos_fk FOREIGN KEY (id_pagina) REFERENCES exemplo_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exemplo_Vocabularios (
    id          varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    id_volume   varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    id_capitulo varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    id_pagina   varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    palavra     varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    portugues   longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    ingles      longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    leitura     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado    tinyint(1)                                                    DEFAULT '1',
    atualizacao datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY exemplo_vocab_volume_fk (id_volume),
    KEY exemplo_vocab_capitulo_fk (id_capitulo),
    KEY exemplo_vocab_pagina_fk (id_pagina),
    CONSTRAINT exemplo_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES exemplo_capitulos (id),
    CONSTRAINT exemplo_vocab_pagina_fk FOREIGN KEY (id_pagina) REFERENCES exemplo_paginas (id),
    CONSTRAINT exemplo_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES exemplo_volumes (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
