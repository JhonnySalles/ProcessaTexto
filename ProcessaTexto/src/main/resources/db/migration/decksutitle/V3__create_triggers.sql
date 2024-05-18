DELIMITER $$

CREATE
    TRIGGER `exemplo_insert` BEFORE INSERT ON `Exemplo`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER `exemplo_update` BEFORE UPDATE ON `Exemplo`
    FOR EACH ROW BEGIN
    IF (OLD.id != NEW.id) OR (OLD.Episodio != NEW.Episodio) OR  (OLD.Linguagem != NEW.Linguagem) OR  (OLD.TempoInicial != NEW.TempoInicial) OR  (OLD.TempoFinal != NEW.TempoFinal) OR
		(OLD.Texto != NEW.Texto) OR (OLD.Traducao != NEW.Traducao) OR (OLD.Vocabulario != NEW.Vocabulario)   THEN
	SET new.atualizacao = NOW();
END IF;
END;
$$


CREATE
    TRIGGER tr_fila_sql_insert
    BEFORE INSERT
    ON _fila_sql
    FOR EACH ROW
BEGIN
    IF (NEW.id IS NULL OR NEW.id = '') THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER tr_fila_sql_update
    BEFORE UPDATE
    ON _fila_sql
    FOR EACH ROW
BEGIN
    SET new.atualizacao = NOW();
END;
$$

DELIMITER ;