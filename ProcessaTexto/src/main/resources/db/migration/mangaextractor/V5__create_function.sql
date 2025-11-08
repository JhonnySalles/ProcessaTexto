DELIMITER $$

CREATE FUNCTION `fn_table_exists`(_tablename VARCHAR(255))
    RETURNS BOOLEAN
    DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE v_exists BOOLEAN DEFAULT FALSE;
    SELECT TRUE INTO v_exists FROM information_schema.tables WHERE Table_Schema = DATABASE() AND LOWER(Table_Name) = LOWER(_tablename) LIMIT 1;
    RETURN v_exists;
END$$

DELIMITER ;