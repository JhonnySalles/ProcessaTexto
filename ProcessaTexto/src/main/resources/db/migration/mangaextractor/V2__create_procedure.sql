DELIMITER $$

/*!50003 CREATE DEFINER=admin@% PROCEDURE create_table(in _tablename varchar(100))
BEGIN

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_volumes (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  manga VARCHAR(250) DEFAULT NULL,
	  volume INT(4) DEFAULT NULL,
	  linguagem VARCHAR(4) DEFAULT NULL,
	  arquivo VARCHAR(250) DEFAULT NULL,
	  vocabulario LONGTEXT,
	  is_processado TINYINT(1) DEFAULT "0",
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id)
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_capitulos (
	  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_volume VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
	  manga LONGTEXT COLLATE utf8mb4_unicode_ci NOT NULL,
	  volume INT(4) NOT NULL,
	  capitulo DOUBLE NOT NULL,
	  linguagem VARCHAR(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  scan VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  is_extra TINYINT(1) DEFAULT NULL,
	  is_raw TINYINT(1) DEFAULT NULL,
	  is_processado TINYINT(1) DEFAULT "0",
	  vocabulario LONGTEXT COLLATE utf8mb4_unicode_ci,
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_volumes_fk (id_volume),
	  CONSTRAINT ',_tablename,'_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;


	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_paginas (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  nome VARCHAR(250) DEFAULT NULL,
	  numero VARCHAR(36) DEFAULT NULL,
	  hash_pagina VARCHAR(250) DEFAULT NULL,
	  is_processado TINYINT(1) DEFAULT "0",
	  vocabulario LONGTEXT,
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_capitulos_fk (id_capitulo),
	  CONSTRAINT ',_tablename,'_capitulos_paginas_fk FOREIGN KEY (id_capitulo) REFERENCES ',_tablename,'_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;


	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_textos (
	  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
	  sequencia INT(4) DEFAULT NULL,
	  texto LONGTEXT COLLATE utf8mb4_unicode_ci,
	  posicao_x1 DOUBLE DEFAULT NULL,
	  posicao_y1 DOUBLE DEFAULT NULL,
	  posicao_x2 DOUBLE DEFAULT NULL,
	  posicao_y2 DOUBLE DEFAULT NULL,
	  versao_app VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT "0",
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_paginas_fk (id_pagina),
	  CONSTRAINT ',_tablename,'_paginas_textos_fk FOREIGN KEY (id_pagina) REFERENCES ',_tablename,'_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_vocabularios (
	  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_volume VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  id_capitulo VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  id_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  palavra VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  portugues LONGTEXT COLLATE utf8mb4_unicode_ci,
	  ingles LONGTEXT COLLATE utf8mb4_unicode_ci,
	  leitura LONGTEXT COLLATE utf8mb4_unicode_ci,
	  revisado TINYINT(1) DEFAULT "1",
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_vocab_volume_fk (id_volume),
	  KEY ',_tablename,'_vocab_capitulo_fk (id_capitulo),
	  KEY ',_tablename,'_vocab_pagina_fk (id_pagina),
	  CONSTRAINT ',_tablename,'_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES ',_tablename,'_capitulos (id),
	  CONSTRAINT ',_tablename,'_vocab_pagina_fk FOREIGN KEY (id_pagina) REFERENCES ',_tablename,'_paginas (id),
	  CONSTRAINT ',_tablename,'_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id)
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;


	END */$$

DELIMITER ;