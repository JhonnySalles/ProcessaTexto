DELIMITER $$

CREATE TRIGGER `tr_comicinfo_update` BEFORE UPDATE ON `comicinfo` FOR EACH ROW
BEGIN
    SET new.Atualizacao = NOW();
END;
$$

DELIMITER ;