CREATE TABLE Estatistica (
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
    atualizacao      DATETIME                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sequencial),
    KEY sequencial (sequencial)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Exclusao (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    palavra     VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    atualizacao DATETIME                                                     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (palavra)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Kanjax_pt (
    id                VARCHAR(36)                                                   DEFAULT NULL,
    sequencia         INT                                                          NOT NULL,
    kanji             VARCHAR(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    keyword           VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    meaning           VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    koohii1           LONGTEXT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    koohii2           LONGTEXT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    onyomi            VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    kunyomi           VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    onwords           LONGTEXT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    kunwords          LONGTEXT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
    jlpt              INT                                                           DEFAULT NULL,
    grade             INT                                                           DEFAULT NULL,
    freq              INT                                                           DEFAULT NULL,
    strokes           INT                                                           DEFAULT NULL,
    variants          VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    radical           VARCHAR(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    parts             VARCHAR(100)                                                  DEFAULT NULL,
    utf8              VARCHAR(10)                                                   DEFAULT NULL,
    sjis              VARCHAR(5)                                                    DEFAULT NULL,
    isTraduzido       TINYINT(1)                                                    DEFAULT NULL,
    isChecado         TINYINT(1)                                                    DEFAULT NULL,
    isRevisado        TINYINT(1)                                                    DEFAULT NULL,
    sinaliza          TINYINT(1)                                                    DEFAULT NULL,
    data_traducao     DATETIME                                                      DEFAULT NULL,
    data_correcao     DATETIME                                                      DEFAULT NULL,
    obs               VARCHAR(100)                                                  DEFAULT NULL,
    isKanjax_original TINYINT(1)                                                    DEFAULT '0',
    palavra           VARCHAR(100)                                                  DEFAULT NULL,
    significado       VARCHAR(250)                                                  DEFAULT NULL,
    atualizacao       DATETIME                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (kanji),
    KEY Sequencia (sequencia)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE Revisar (
    id            VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    vocabulario   VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    forma_basica  VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura       VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura_novel VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ingles        LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    portugues     LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    revisado      TINYINT(1)                                                    DEFAULT '0',
    aparece       INT                                                           DEFAULT '0',
    isAnime       TINYINT(1)                                                    DEFAULT '0',
    isManga       TINYINT(1)                                                    DEFAULT '0',
    isNovel       TINYINT(1)                                                    DEFAULT '0',
    atualizacao   DATETIME                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vocabulario),
    KEY Vocabulario (vocabulario, forma_basica)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Sincronizacao (
    id          INT                                                                                                                                  NOT NULL AUTO_INCREMENT,
    conexao     ENUM ('MANGA_EXTRACTOR','NOVEL_EXTRACTOR','TEXTO_INGLES','DECKSUBTITLE','FIREBASE') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    envio       DATETIME DEFAULT NULL,
    recebimento DATETIME DEFAULT NULL,
    PRIMARY KEY (id, conexao)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb3;

CREATE TABLE Vocabulario (
    id            VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    vocabulario   VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    forma_basica  VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura       VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    leitura_novel VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ingles        LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    portugues     LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    jlpt          INT                                                           DEFAULT '0',
    atualizacao   DATETIME                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vocabulario),
    KEY Vocabulario (vocabulario, forma_basica)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE Words_kanji_info (
    id          VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    sequencia   INT NOT NULL AUTO_INCREMENT,
    word        VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    read_info   VARCHAR(350) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    frequency   INT                                                           DEFAULT NULL,
    tabela      LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    atualizacao DATETIME                                                      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sequencia)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;