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


CREATE
    TRIGGER `tr_estatistica_update` BEFORE UPDATE ON `estatistica`
    FOR EACH ROW BEGIN
    SET new.atualizacao = NOW();
END;
$$

CREATE
    TRIGGER `tr_estatistica_insert` BEFORE INSERT ON `estatistica`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$


CREATE
    TRIGGER `tr_kanjax_pt_update` BEFORE UPDATE ON `kanjax_pt`
    FOR EACH ROW BEGIN
    SET new.atualizacao = NOW();
END;
$$

CREATE
    TRIGGER `tr_kanjax_pt_insert` BEFORE INSERT ON `kanjax_pt`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$


CREATE
    TRIGGER `tr_words_kanji_info_insert` BEFORE INSERT ON `words_kanji_info`
    FOR EACH ROW BEGIN
    IF (NEW.id IS NULL OR NEW.id = '')  THEN
		SET new.id = UUID();
END IF;
END;
$$

CREATE
    TRIGGER `tr_words_kanji_info_update` BEFORE UPDATE ON `words_kanji_info`
    FOR EACH ROW BEGIN
    SET new.atualizacao = NOW();
END;
$$

DELIMITER ;