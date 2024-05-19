CREATE TABLE estatistica (
    id               VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    sequencial       INT NOT NULL AUTO_INCREMENT,
    kanji            VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura          VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    tipo             VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    quantidade       DOUBLE                                                       DEFAULT NULL,
    percentual       FLOAT                                                        DEFAULT NULL,
    media            DOUBLE                                                       DEFAULT NULL,
    percentual_medio FLOAT                                                        DEFAULT NULL,
    cor_sequencial   INT                                                          DEFAULT NULL,
    atualizacao      datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( sequencial ),
    key              sequencial (sequencial)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE exclusao (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    palavra     VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    atualizacao datetime                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( palavra )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE kanjax_pt (
    id                VARCHAR(36)                                                   DEFAULT NULL,
    sequencia         INT                                                          NOT NULL,
    kanji             VARCHAR(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    keyword           VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    meaning           VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    koohii1           longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    koohii2           longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    onyomi            VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    kunyomi           VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    onwords           longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    kunwords          longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    jlpt              INT                                                           DEFAULT NULL,
    grade             INT                                                           DEFAULT NULL,
    freq              INT                                                           DEFAULT NULL,
    strokes           INT                                                           DEFAULT NULL,
    variants          VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    radical           VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    parts             VARCHAR(100)                                                  DEFAULT NULL,
    utf8              VARCHAR(10)                                                   DEFAULT NULL,
    sjis              VARCHAR(5)                                                    DEFAULT NULL,
    istraduzido       tinyint(1) DEFAULT NULL,
    ischecado         tinyint(1) DEFAULT NULL,
    isrevisado        tinyint(1) DEFAULT NULL,
    sinaliza          tinyint(1) DEFAULT NULL,
    data_traducao     datetime                                                      DEFAULT NULL,
    data_correcao     datetime                                                      DEFAULT NULL,
    obs               VARCHAR(100)                                                  DEFAULT NULL,
    iskanjax_original tinyint(1) DEFAULT '0',
    palavra           VARCHAR(100)                                                  DEFAULT NULL,
    significado       VARCHAR(250)                                                  DEFAULT NULL,
    atualizacao       datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( kanji ),
    key               sequencia (sequencia)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE revisar (
    id            VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    vocabulario   VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    forma_basica  VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura       VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura_novel VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ingles        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    portugues     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado      tinyint(1) DEFAULT '0',
    aparece       INT                                                           DEFAULT '0',
    isanime       tinyint(1) DEFAULT '0',
    ismanga       tinyint(1) DEFAULT '0',
    isnovel       tinyint(1) DEFAULT '0',
    atualizacao   datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( vocabulario ),
    key           vocabulario (vocabulario, forma_basica)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE vocabulario (
    id            VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    vocabulario   VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    forma_basica  VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura       VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura_novel VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ingles        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    portugues     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    jlpt          INT                                                           DEFAULT '0',
    atualizacao   datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( vocabulario ),
    key           vocabulario (vocabulario, forma_basica)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE words_kanji_info (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    sequencia   INT NOT NULL AUTO_INCREMENT,
    word        VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    read_info   VARCHAR(350) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    frequency   INT                                                           DEFAULT NULL,
    tabela      longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    atualizacao datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( sequencia )
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;