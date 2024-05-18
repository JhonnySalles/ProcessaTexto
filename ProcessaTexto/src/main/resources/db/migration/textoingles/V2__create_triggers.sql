DELIMITER $$

CREATE
    TRIGGER `tr_exclusao_insert` BEFORE INSERT ON `exclusao`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER `tr_exclusao_update` BEFORE UPDATE ON `exclusao`
    FOR EACH ROW BEGIN
    SET new.Atualizacao = NOW();
END;
$$


CREATE
    TRIGGER `tr_revisar_insert` BEFORE INSERT ON `revisar`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER `tr_revisar_update` BEFORE UPDATE ON `revisar`
    FOR EACH ROW BEGIN
    SET new.Atualizacao = NOW();
END;
$$


CREATE
    TRIGGER `tr_valido_insert` BEFORE INSERT ON `valido`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER `tr_valido_update` BEFORE UPDATE ON `valido`
    FOR EACH ROW BEGIN
    SET new.Atualizacao = NOW();
END;
$$


CREATE
    TRIGGER `tr_vocabulario_insert` BEFORE INSERT ON `vocabulario`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER `tr_vocabulario_update` BEFORE UPDATE ON `vocabulario`
    FOR EACH ROW BEGIN
    SET new.Atualizacao = NOW();
END;
$$

DELIMITER ;