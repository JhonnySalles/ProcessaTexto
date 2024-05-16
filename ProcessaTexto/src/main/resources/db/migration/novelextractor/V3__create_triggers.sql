DELIMITER $$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_capas_insert` BEFORE UPDATE ON `exemplo_capas` FOR EACH ROW BEGIN    IF (NEW.id IS NULL OR NEW.id = '') THEN      SET new.id = UUID();    END IF;  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_capas_update` BEFORE UPDATE ON `exemplo_capas` FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_capitulos_insert` BEFORE UPDATE ON `exemplo_capitulos` FOR EACH ROW BEGIN    IF (NEW.id IS NULL OR NEW.id = '') THEN      SET new.id = UUID();    END IF;  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_capitulos_update` BEFORE UPDATE ON `exemplo_capitulos` FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_textos_insert` BEFORE UPDATE ON `exemplo_textos` FOR EACH ROW BEGIN    IF (NEW.id IS NULL OR NEW.id = '') THEN      SET new.id = UUID();    END IF;  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_textos_update` BEFORE UPDATE ON `exemplo_textos` FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_vocabularios_insert` BEFORE UPDATE ON `exemplo_vocabularios` FOR EACH ROW BEGIN    IF (NEW.id IS NULL OR NEW.id = '') THEN      SET new.id = UUID();    END IF;  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_vocabularios_update` BEFORE UPDATE ON `exemplo_vocabularios` FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_volumes_insert` BEFORE UPDATE ON `exemplo_volumes` FOR EACH ROW BEGIN    IF (NEW.id IS NULL OR NEW.id = '') THEN      SET new.id = UUID();    END IF;  END */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'admin'@'%' */ /*!50003 TRIGGER `tr_exemplo_volumes_update` BEFORE UPDATE ON `exemplo_volumes` FOR EACH ROW BEGIN    SET new.Atualizacao = NOW();  END */$$

DELIMITER ;