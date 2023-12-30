package org.jisho.textosJapones.controller;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import org.jisho.textosJapones.controller.novels.NovelsController;

public interface BaseController {
    AnchorPane getRoot();
    NovelsController getControllerPai();
    ProgressBar getBarraProgresso();
    void habilitar();
}
