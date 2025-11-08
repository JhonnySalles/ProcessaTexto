DELIMITER $$

CREATE PROCEDURE `sp_create_table`(IN _tablename VARCHAR(100))
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

END$$


CREATE PROCEDURE `sp_list_tables`()
BEGIN
    SELECT Table_Name AS Tabela
    FROM information_schema.tables
    WHERE Table_Schema = DATABASE();
END$$


CREATE PROCEDURE `sp_sincronizacao`(IN _data DATETIME)
BEGIN
    SELECT TABLE_NAME AS tabela, Update_Time AS ultima_sincronizacao
    FROM information_schema.TABLES
    WHERE Table_Schema = DATABASE()
    HAVING _data IS NULL OR ultima_sincronizacao > _data OR ultima_sincronizacao IS NULL;
END$$

DELIMITER ;