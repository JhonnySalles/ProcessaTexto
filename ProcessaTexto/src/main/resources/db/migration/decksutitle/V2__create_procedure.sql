DELIMITER $$

/*!50003 CREATE DEFINER=admin@% PROCEDURE create_table(in _tablename varchar(100))
BEGIN

	SET @sql = CONCAT('CREATE TABLE ',_tablename,'(
	  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,
	  Episodio INT(2) DEFAULT NULL,
	  Linguagem VARCHAR(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  TempoInicial VARCHAR(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  TempoFinal VARCHAR(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
	  Texto LONGTEXT COLLATE utf8mb4_unicode_ci,
	  Traducao LONGTEXT COLLATE utf8mb4_unicode_ci,
	  Vocabulario LONGTEXT COLLATE utf8mb4_unicode_ci,
	  Atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (id)
	) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;');
	PREPARE stmt FROM @sql;
	EXECUTE stmt;
	DEALLOCATE PREPARE stmt;

	END */$$

DELIMITER ;