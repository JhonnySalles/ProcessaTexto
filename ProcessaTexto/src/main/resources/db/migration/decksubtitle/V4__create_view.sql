DELIMITER $$

CREATE OR REPLACE VIEW vw_sincronizacao AS
    SELECT Table_Name AS tabela, Update_Time AS ultima_sincronizacao
      FROM information_schema.TABLES
     WHERE Table_Schema = DATABASE()$$

DELIMITER ;

