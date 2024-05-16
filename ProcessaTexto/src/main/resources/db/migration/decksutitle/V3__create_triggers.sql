DELIMITER $$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER _sql_update BEFORE UPDATE ON _sql FOR EACH ROW BEGIN
	SET new.atualizacao = NOW();
    END */$$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 exemplo_insert */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER exemplo_insert BEFORE INSERT ON exemplo FOR EACH ROW BEGIN
	IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
	END IF;
    END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER exemplo_update BEFORE UPDATE ON exemplo FOR EACH ROW BEGIN
	IF (OLD.id != NEW.id) OR (OLD.Episodio != NEW.Episodio) OR  (OLD.Linguagem != NEW.Linguagem) OR  (OLD.TempoInicial != NEW.TempoInicial) OR  (OLD.TempoFinal != NEW.TempoFinal) OR
		(OLD.Texto != NEW.Texto) OR (OLD.Traducao != NEW.Traducao) OR (OLD.Vocabulario != NEW.Vocabulario)   THEN
	SET new.atualizacao = NOW();
	END IF;
    END */$$

DELIMITER ;