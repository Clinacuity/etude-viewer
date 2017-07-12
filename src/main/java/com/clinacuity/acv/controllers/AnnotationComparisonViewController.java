package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class AnnotationComparisonViewController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML public AnnotatedDocumentPane referencePane;
    @FXML public AnnotatedDocumentPane targetPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (targetPane == null) {
            logger.error("Whoa, this is null...");
        }

        referencePane.initialize(
                new Annotations(AcvContext.getInstance().referenceDocumentPathProperty.getValueSafe()), this);
        targetPane.initialize(
                new Annotations(AcvContext.getInstance().targetDocumentPathProperty.getValueSafe()), this);

        AcvContext.getInstance().selectedAnnotationProperty.addListener(selectedAnnotationListener);
        logger.debug("Annotation Comparison View Controller initialized");
    }

    public void notifyAnnotationSet(Set<String> keySet) {
        keySet.forEach(key -> {
            if (!AcvContext.getInstance().annotationList.contains(key)) {
                AcvContext.getInstance().annotationList.add(key);
                logger.debug("Annotation {} added", key);
            } else {
                logger.debug("Annotation {} NOT ADDED", key);
            }
        });
    }

    /********************************
     *                              *
     * Change Listeners             *
     *                              *
     *******************************/

    /**
     * Listens on the AcvContext's selected annotation and updates the document panes' buttons accordingly
     */
    private ChangeListener<String> selectedAnnotationListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            logger.debug("Selected Annotation Changed: {}", newValue);

            if (newValue.equals(AcvContext.getInstance().getDefaultSelectedAnnotation())) {
                logger.error("Default Annotation is selected; clearing buttons.");
                targetPane.clearButtons();
                referencePane.clearButtons();
            } else {
                targetPane.resetButtons(newValue);
                referencePane.resetButtons(newValue);
            }
        }
    };
}
