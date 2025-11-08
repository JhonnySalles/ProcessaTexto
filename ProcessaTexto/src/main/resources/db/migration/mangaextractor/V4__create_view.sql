DELIMITER $$

CREATE OR REPLACE VIEW vw_sincronizacao AS
    SELECT REPLACE(information_schema.tb.Table_Name, '_volumes', '') AS tabela, MAX(information_schema.aux.Update_Time) AS ultima_sincronizacao
      FROM information_schema.TABLES tb
      JOIN information_schema.TABLES aux ON aux.Table_Schema = tb.Table_Schema AND aux.Table_Name LIKE CONCAT(REPLACE(tb.Table_Name, '_volumes', ''), '_%')
     WHERE tb.Table_Schema = DATABASE() AND tb.Table_Name LIKE '%\\_volumes'
     GROUP BY tabela $$

DELIMITER ;

