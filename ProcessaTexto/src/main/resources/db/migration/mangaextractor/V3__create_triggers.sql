DELIMITER $$

CREATE
    TRIGGER `tr_exemplo_volumes_insert` BEFORE INSERT ON `exemplo_volumes`
    FOR EACH ROW BEGIN IF (NEW.id IS NULL OR NEW.id = '') THEN  SET new.id = UUID(); END IF;  END;
$$

CREATE
    TRIGGER `tr_exemplo_volumes_update` BEFORE UPDATE ON `exemplo_volumes`
    FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END;
$$


CREATE
    TRIGGER `tr_exemplo_capitulos_insert` BEFORE INSERT ON `exemplo_capitulos`
    FOR EACH ROW BEGIN IF (NEW.id IS NULL OR NEW.id = '') THEN  SET new.id = UUID(); END IF;  END;
$$

CREATE
    TRIGGER `tr_exemplo_capitulos_update` BEFORE UPDATE ON `exemplo_capitulos`
    FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END;
$$


CREATE
    TRIGGER `tr_exemplo_paginas_insert` BEFORE INSERT ON `exemplo_paginas`
    FOR EACH ROW BEGIN IF (NEW.id IS NULL OR NEW.id = '') THEN  SET new.id = UUID(); END IF;  END;
$$

CREATE
    TRIGGER `tr_exemplo_paginas_update` BEFORE UPDATE ON `exemplo_paginas`
    FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END;
$$


CREATE
    TRIGGER `tr_exemplo_textos_insert` BEFORE INSERT ON `exemplo_textos`
    FOR EACH ROW BEGIN IF (NEW.id IS NULL OR NEW.id = '') THEN  SET new.id = UUID(); END IF;  END;
$$

CREATE
    TRIGGER `tr_exemplo_textos_update` BEFORE UPDATE ON `exemplo_textos`
    FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END;
$$


CREATE
    TRIGGER `tr_exemplo_capas_insert` BEFORE INSERT ON `exemplo_capas`
    FOR EACH ROW BEGIN IF (NEW.id IS NULL OR NEW.id = '') THEN SET new.id = UUID(); END IF; END;
$$

CREATE
    TRIGGER `tr_exemplo_capas_update` BEFORE UPDATE ON `exemplo_capas`
    FOR EACH ROW BEGIN   SET new.Atualizacao = NOW(); END;
$$


CREATE
    TRIGGER `tr_exemplo_vocabularios_update` BEFORE UPDATE ON `exemplo_vocabularios`
    FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END;
$$


DELIMITER ;