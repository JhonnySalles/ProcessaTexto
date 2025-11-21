DELIMITER $$

CREATE FUNCTION `fn_vocabulary_exists`(_database VARCHAR(255), _table VARCHAR(255))
    RETURNS BOOLEAN
    DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_exists BOOLEAN DEFAULT FALSE;
    SELECT TRUE INTO v_exists
      FROM information_schema.tables
     WHERE Table_Schema = _database
       AND Table_Name LIKE '%_vocabularios%'
       AND Table_Name LIKE CONCAT('%', _table, '%')
     LIMIT 1;
    RETURN v_exists;
END$$


CREATE FUNCTION `fn_table_exists`(_tablename VARCHAR(255))
    RETURNS BOOLEAN
    DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_exists BOOLEAN DEFAULT FALSE;
    SELECT TRUE INTO v_exists FROM information_schema.tables WHERE Table_Schema = DATABASE() AND Table_Name = CONCAT(REPLACE(_tablename, '_volumes', ''), '_volumes') LIMIT 1;
    RETURN v_exists;
END$$

DELIMITER ;