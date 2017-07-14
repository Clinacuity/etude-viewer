package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;

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

        setupViewControls();

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
        referencePane.initialize(referenceAnnotationsProperty.getValue(), context.selectedReferenceJsonObject);
        targetPane.initialize(targetAnnotationsProperty.getValue(), context.selectedTargetJsonObject);
    }

    private void setupViewControls() {
        context.selectedTargetJsonObject.addListener((observable, oldValue, newValue) ->
                viewControls.setTargetFeatureTreeText(updateTextArea(newValue)));

        context.selectedReferenceJsonObject.addListener((observable, oldValue, newValue) ->
                viewControls.setReferenceFeatureTreeText(updateTextArea(newValue)));
    }

    /**
     * Returns the String value of a JsonObject's tree view.
     * @param value The JsonObject
     * @return      The String representation of its tree view
     */
    private String updateTextArea(JsonObject value) {
        StringBuilder buffer = new StringBuilder();
        for (String key: value.keySet()) {
            if (value.get(key).equals(JsonNull.INSTANCE)) {
                buffer.append("null");
            } else {
                buffer.append(value.get(key).getAsString());
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }

    private Annotations updateAnnotations(String path) {
        return new Annotations(path);
    }

    /* *******************************
     *                              *
     * Change Listeners             *
     *                              *
     *******************************/

    /**
     * Listens on the AcvContext's selected annotation and updates the document panes' buttons accordingly
     */
    private ChangeListener<String> selectedAnnotationTypeListener = (observable, oldValue, newValue) -> {
        logger.debug("Selected Annotation Changed: {}", newValue);

        if (newValue.equals(AcvContext.getInstance().getDefaultSelectedAnnotation())) {
            logger.error("Default Annotation is selected; clearing buttons.");
            targetPane.clearButtons();
            referencePane.clearButtons();
        } else {
            targetPane.resetButtons(newValue);
            referencePane.resetButtons(newValue);
        }
    };

    /**
     * Updates the Context's annotation type list whenever the Annotations objects are updated.  This usually occurs
     * when new documents are loaded.
     */
    private ChangeListener<Annotations> notifyAnnotationSet = (observable, oldValue, newValue) ->
            newValue.getAnnotationKeySet().forEach(key -> {
                if (AcvContext.getInstance().annotationList.contains(key)) {
                    logger.warn("Duplicate key not added: {}", key);
                } else {
                    AcvContext.getInstance().annotationList.add(key);
                }
            });
}
