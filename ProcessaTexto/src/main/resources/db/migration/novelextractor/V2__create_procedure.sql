DELIMITER $$

/*!50003 CREATE DEFINER=`admin`@`%` PROCEDURE `create_table`(IN _tablename VARCHAR(100))
BEGIN

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_volumes (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  novel VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
	  serie VARCHAR(250) DEFAULT NULL,
	  titulo VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
	  titulo_alternativo VARCHAR(250) DEFAULT NULL,
	  descricao LONGTEXT,
	  arquivo VARCHAR(250) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
	  editora VARCHAR(250) DEFAULT NULL,
	  autor VARCHAR(900) DEFAULT NULL,
	  volume FLOAT DEFAULT NULL,
	  linguagem VARCHAR(4) DEFAULT NULL,
	  is_favorito TINYINT(1) DEFAULT "0",
	  is_processado TINYINT(1) DEFAULT "0",
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id)
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_capas (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  novel LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  volume FLOAT NOT NULL,
	  linguagem VARCHAR(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  capa LONGBLOB,
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_volumes_fk (id_volume),
	  CONSTRAINT ',_tablename,'_volumes_capas_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_capitulos (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  novel LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  volume FLOAT NOT NULL,
	  capitulo VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  descricao VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  sequencia INT DEFAULT NULL,
	  linguagem VARCHAR(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  is_processado TINYINT(1) DEFAULT "0",
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_volumes_fk (id_volume),
	  CONSTRAINT ',_tablename,'_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_textos (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  sequencia INT DEFAULT NULL,
	  texto LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_capitulos_fk (id_capitulo),
	  CONSTRAINT ',_tablename,'_capitulos_textos_fk FOREIGN KEY (id_capitulo) REFERENCES ',_tablename,'_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'_vocabularios (
	  id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
	  id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  palavra VARCHAR(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  portugues LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
	  ingles LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
	  leitura LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
	  revisado TINYINT(1) DEFAULT "1",
	  atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id),
	  KEY ',_tablename,'_vocab_volume_fk (id_volume),
	  KEY ',_tablename,'_vocab_capitulo_fk (id_capitulo),
	  CONSTRAINT ',_tablename,'_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES ',_tablename,'_capitulos (id),
	  CONSTRAINT ',_tablename,'_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id)
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;
	END */$$
DELIMITER ;

/* Procedure structure for procedure `delete_volume` */

/*!50003 DROP PROCEDURE IF EXISTS  `delete_volume` */;

DELIMITER $$

/*!50003 CREATE DEFINER=`admin`@`%` PROCEDURE `delete_volume`(IN _tablename VARCHAR(100), _IdsVolume VARCHAR(900))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

        START TRANSACTION;
	SET @sql = CONCAT('DELETE vol FROM ',_tablename,'_vocabularios AS vol INNER JOIN ',_tablename, "_capitulos AS c ON c.id = vol.id_capitulo INNER JOIN ", _tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;


	SET @sql = CONCAT('DELETE vol FROM ',_tablename,'_vocabularios AS vol INNER JOIN ',_tablename, "_volumes AS v ON v.id = vol.id_volume WHERE v.id IN (", _IdsVolume, ");");
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;


	SET @sql = CONCAT('DELETE t FROM ',_tablename,'_textos AS t INNER JOIN  ',_tablename, "_capitulos AS c ON c.id = t.id_capitulo INNER JOIN ",
	  _tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;


	SET @sql = CONCAT('DELETE c FROM ',_tablename,'_capitulos AS c INNER JOIN ',_tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	SET @sql = CONCAT('DELETE v FROM ',_tablename,'_volumes AS v WHERE v.id IN (', _IdsVolume, ");");
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	COMMIT;

	END */$$

DELIMITER ;