DELIMITER $$

CREATE DEFINER=`admin`@`%` PROCEDURE `create_table`(IN _tablename VARCHAR(100))
BEGIN

	SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ',_tablename,'_volumes (
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

    SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ',_tablename,'_capas (
          id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
          id_volume VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
          manga VARCHAR(250) DEFAULT NULL,
          volume INT(4) DEFAULT NULL,
          linguagem VARCHAR(4) DEFAULT NULL,
          arquivo VARCHAR(250) DEFAULT NULL,
          extensao VARCHAR(10) DEFAULT NULL,
          capa LONGBLOB,
          atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (id),
          KEY ',_tablename,'_volumes_fk (id_volume),
          CONSTRAINT ',_tablename,'_volumes_capas_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
        ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ',_tablename,'_capitulos (
          id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
          id_volume VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
          manga VARCHAR(250) DEFAULT NULL,
          volume INT(4) NOT NULL,
          capitulo DOUBLE NOT NULL,
          linguagem VARCHAR(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
          scan VARCHAR(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
          is_extra TINYINT(1) DEFAULT NULL,
          is_raw TINYINT(1) DEFAULT NULL,
          vocabulario LONGTEXT COLLATE utf8mb4_unicode_ci,
          atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (id),
          KEY ',_tablename,'_volumes_fk (id_volume),
          CONSTRAINT ',_tablename,'_volumes_capitulos_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE
        ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;


    SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ',_tablename,'_paginas (
          id VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
          id_capitulo VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
          nome VARCHAR(250) DEFAULT NULL,
          numero VARCHAR(36) DEFAULT NULL,
          hash_pagina VARCHAR(250) DEFAULT NULL,
          vocabulario LONGTEXT,
          atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
          PRIMARY KEY (id),
          KEY ',_tablename,'_capitulos_fk (id_capitulo),
          CONSTRAINT ',_tablename,'_capitulos_paginas_fk FOREIGN KEY (id_capitulo) REFERENCES ',_tablename,'_capitulos (id) ON DELETE CASCADE ON UPDATE CASCADE
        ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;


    SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ',_tablename,'_textos (
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

    SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ',_tablename,'_vocabularios (
          id_vocabulario VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
          id_volume VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
          id_capitulo VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
          id_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
          atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
          KEY ',_tablename,'_vocab_volume_fk (id_volume),
          KEY ',_tablename,'_vocab_capitulo_fk (id_capitulo),
          KEY ',_tablename,'_vocab_pagina_fk (id_pagina),
          KEY ',_tablename,'_vocab_vocabularios_fk (id_vocabulario),
          CONSTRAINT ',_tablename,'_vocab_capitulo_fk FOREIGN KEY (id_capitulo) REFERENCES ',_tablename,'_capitulos (id),
          CONSTRAINT ',_tablename,'_vocab_pagina_fk FOREIGN KEY (id_pagina) REFERENCES ',_tablename,'_paginas (id),
          CONSTRAINT ',_tablename,'_vocab_vocabulario_fk FOREIGN KEY (id_vocabulario) REFERENCES _vocabularios (id),
          CONSTRAINT ',_tablename,'_vocab_volume_fk FOREIGN KEY (id_volume) REFERENCES ',_tablename,'_volumes (id)
        ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END$$


CREATE DEFINER=`admin`@`%` PROCEDURE `drop_table`(IN _tablename VARCHAR(100))
BEGIN

    SET @sql = CONCAT('DROP TABLE IF EXISTS ',_tablename,'_textos;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DROP TABLE IF EXISTS ',_tablename,'_paginas;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DROP TABLE IF EXISTS ',_tablename,'_capitulos;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DROP TABLE IF EXISTS ',_tablename,'_capas;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DROP TABLE IF EXISTS ',_tablename,'_volumes;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DROP TABLE IF EXISTS ',_tablename,'_vocabularios;');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END$$


CREATE DEFINER=`admin`@`%` PROCEDURE `delete_volume`(IN _tablename VARCHAR(100), _IdsVolume VARCHAR(900))
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
    ROLLBACK;
    RESIGNAL;
    END;

    START TRANSACTION;
    SET @sql = CONCAT('DELETE vol FROM ',_tablename,'_vocabularios AS vol INNER JOIN ',_tablename, "_paginas AS p ON p.id = vol.id_pagina INNER JOIN ",
    _tablename, "_capitulos AS c ON c.id = p.id_capitulo INNER JOIN ", _tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DELETE vol FROM ',_tablename,'_vocabularios AS vol INNER JOIN ',_tablename, "_capitulos AS c ON c.id = vol.id_capitulo INNER JOIN ", _tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;


    SET @sql = CONCAT('DELETE vol FROM ',_tablename,'_vocabularios AS vol INNER JOIN ',_tablename, "_volumes AS v ON v.id = vol.id_volume WHERE v.id IN (", _IdsVolume, ");");
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;


    SET @sql = CONCAT('DELETE t FROM ',_tablename,'_textos AS t INNER JOIN  ',_tablename, "_paginas AS p ON p.id = t.id_pagina INNER JOIN ",
    _tablename, "_capitulos AS c ON c.id = p.id_capitulo INNER JOIN ", _tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @sql = CONCAT('DELETE p FROM ',_tablename,'_paginas p INNER JOIN ',_tablename, "_capitulos AS c ON c.id = p.id_capitulo INNER JOIN ", _tablename, "_volumes AS v ON v.id = c.id_volume WHERE v.id IN (", _IdsVolume, ");");
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

END$$

DELIMITER ;