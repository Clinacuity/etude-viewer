package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AnnotationComparisonView extends GridPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private AnnotatedDocumentPane referencePane;
    @FXML private AnnotatedDocumentPane targetPane;

    public AnnotationComparisonView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationComparisonView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
