DELIMITER $$

CREATE OR REPLACE VIEW vw_sincronizacao AS
    SELECT REPLACE(information_schema.tb.TABLE_NAME, '_volumes', '') AS tabela, MAX(information_schema.aux.UPDATE_TIME) AS ultima_sincronizacao
      FROM (information_schema.TABLES tb JOIN information_schema.TABLES aux
            ON (((aux.TABLE_SCHEMA = tb.TABLE_SCHEMA) AND (aux.TABLE_NAME LIKE CONCAT(REPLACE(tb.TABLE_NAME, '_volumes', ''), '_%')))))
     WHERE ((tb.TABLE_SCHEMA = DATABASE()) AND (tb.TABLE_NAME LIKE '%\\_volumes'))
     GROUP BY tabela $$

DELIMITER ;

