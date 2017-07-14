package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class AcvContentController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private AnnotatedDocumentPane referencePane;
    @FXML private AnnotatedDocumentPane targetPane;
    @FXML private ViewControls viewControls;
    private ObjectProperty<Annotations> targetAnnotationsProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Annotations> referenceAnnotationsProperty = new SimpleObjectProperty<>();
    private AcvContext context;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        context = AcvContext.getInstance();

        setupAnnotationProperties();

        setupDocumentPanes();

        AcvContext.getInstance().selectedAnnotationTypeProperty.addListener(selectedAnnotationTypeListener);
        logger.debug("Annotation Comparison View Controller initialized");
    }

    private void setupAnnotationProperties() {
        referenceAnnotationsProperty.addListener(notifyAnnotationSet);
        targetAnnotationsProperty.addListener(notifyAnnotationSet);

        referenceAnnotationsProperty.setValue(new Annotations(context.referenceDocumentPathProperty.getValueSafe()));
        targetAnnotationsProperty.setValue(new Annotations(context.targetDocumentPathProperty.getValueSafe()));

        context.referenceDocumentPathProperty.addListener(((observable, oldValue, newValue) ->
                referenceAnnotationsProperty.setValue(new Annotations(newValue))));

        context.targetDocumentPathProperty.addListener(((observable, oldValue, newValue) ->
                targetAnnotationsProperty.setValue(new Annotations(newValue))));
    }

    private void setupDocumentPanes() {
        referencePane.initialize(referenceAnnotationsProperty.getValue());
        targetPane.initialize(targetAnnotationsProperty.getValue());
    }

    private Annotations updateAnnotations(String path) {
        return new Annotations(path);
    }

    /********************************
     *                              *
     * Change Listeners             *
     *                              *
     *******************************/

    /**
     * Listens on the AcvContext's selected annotation and updates the document panes' buttons accordingly
     */
    private ChangeListener<String> selectedAnnotationTypeListener = new ChangeListener<String>() {
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

    /**
     * Updates the Context's annotation type list whenever the Annotations objects are updated.  This usually occurs
     * when new documents are loaded.
     */
    private ChangeListener<Annotations> notifyAnnotationSet = new ChangeListener<Annotations>() {
        @Override
        public void changed(ObservableValue<? extends Annotations> observable, Annotations oldValue, Annotations newValue) {
            newValue.getAnnotationKeySet().forEach(key -> {
                if (AcvContext.getInstance().annotationList.contains(key)) {
                    logger.warn("Duplicate key not added: {}", key);
                } else {
                    AcvContext.getInstance().annotationList.add(key);
                }
            });
        }
    };
}
