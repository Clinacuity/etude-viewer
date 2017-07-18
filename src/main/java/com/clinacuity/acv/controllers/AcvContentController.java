package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.AnnotationButton;
import com.clinacuity.acv.tasks.CreateButtonsTask;
import com.clinacuity.acv.tasks.GetLabelsFromDocumentTask;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AcvContentController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    private AcvContext context;

    @FXML private AnnotatedDocumentPane referencePane;
    @FXML private AnnotatedDocumentPane targetPane;
    @FXML private ViewControls viewControls;
    private ObjectProperty<Annotations> targetAnnotationsProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Annotations> referenceAnnotationsProperty = new SimpleObjectProperty<>();
    private ObjectProperty<AnnotationButton> selectedAnnotationButton = new SimpleObjectProperty<>();
    private double characterHeight = -1.0;
    private GetLabelsFromDocumentTask getRefLabelsTask;
    private GetLabelsFromDocumentTask getTargetLabelsTask;
    private CreateButtonsTask targetButtonsTask;
    private CreateButtonsTask referenceButtonsTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        context = AcvContext.getInstance();

        FxTimer.runLater(Duration.ofMillis(300), this::init);
    }

    private void init() {
        setupAnnotationProperties();

        setupDocumentPanes();

        setupViewControls();

        logger.debug("Annotation Comparison View Controller initialized");
    }

    /**
     * Sets up the Annotation object properties with listeners and values
     */
    private void setupAnnotationProperties() {
        referenceAnnotationsProperty.addListener(annotationPropertiesListener);
        targetAnnotationsProperty.addListener(annotationPropertiesListener);

        referenceAnnotationsProperty.setValue(new Annotations(context.referenceDocumentPathProperty.getValueSafe()));
        targetAnnotationsProperty.setValue(new Annotations(context.targetDocumentPathProperty.getValueSafe()));

        context.referenceDocumentPathProperty.addListener(((observable, oldValue, newValue) ->
                referenceAnnotationsProperty.setValue(new Annotations(newValue))));

        context.targetDocumentPathProperty.addListener(((observable, oldValue, newValue) ->
                targetAnnotationsProperty.setValue(new Annotations(newValue))));

        selectedAnnotationButton.addListener(selectedAnnotationButtonListener);
    }

    /**
     * Sets up the children AnnotatedDocumentPanes by calling their initialize method. It also binds their scrollbars
     */
    private void setupDocumentPanes() {
        referencePane.vvalueProperty().bindBidirectional(targetPane.vvalueProperty());
        referencePane.hvalueProperty().bindBidirectional(targetPane.hvalueProperty());

        if (characterHeight <= 0.0d) {
            characterHeight = referencePane.getCharacterHeight();
        }

        if (getRefLabelsTask != null && getRefLabelsTask.isRunning()) {
            getRefLabelsTask.cancel();
        }

        if (getTargetLabelsTask != null && getTargetLabelsTask.isRunning()) {
            getTargetLabelsTask.cancel();
        }

        getRefLabelsTask = new GetLabelsFromDocumentTask(referenceAnnotationsProperty.getValue().getRawText(), characterHeight);
        getRefLabelsTask.setOnSucceeded(event -> referencePane.addLabels(getRefLabelsTask.getValue()));
        new Thread(getRefLabelsTask).start();

        getTargetLabelsTask = new GetLabelsFromDocumentTask(targetAnnotationsProperty.getValue().getRawText(), characterHeight);
        getTargetLabelsTask.setOnSucceeded(event -> targetPane.addLabels(getTargetLabelsTask.getValue()));
        new Thread(getTargetLabelsTask).start();
    }

    /**
     * Sets up the View Controls.  This includes adding the necessary bindings and listeners to the appropriate objects.
     * Note that some listeners are initialized when the ViewControls are created.
     */
    private void setupViewControls() {
        context.selectedAnnotationTypeProperty.addListener(selectedAnnotationTypeListener);
    }

    private void resetAnnotationButtons(String key) {
        if (key.equals(context.getDefaultSelectedAnnotation())) {
            targetPane.addButtons(new ArrayList<>());
            referencePane.addButtons(new ArrayList<>());
        } else {

            if (targetButtonsTask != null && targetButtonsTask.isRunning()) {
                targetButtonsTask.cancel();
            }

            if (referenceButtonsTask != null && referenceButtonsTask.isRunning()) {
                referenceButtonsTask.cancel();
            }

            List<JsonObject> targetJson = targetAnnotationsProperty.getValue().getAnnotationsByKey(key);
            List<JsonObject> referenceJson = referenceAnnotationsProperty.getValue().getAnnotationsByKey(key);

            // TODO: button actions must include behaviors against their matchingButtons object
            // TODO: matchingButtons objects must be populated
            targetButtonsTask = new CreateButtonsTask(targetJson, targetPane.getLabelList(), characterHeight);
            targetButtonsTask.setOnSucceeded(event -> {
                targetPane.addButtons(targetButtonsTask.getValue());
                setupAnnotationButtons();
            });
            new Thread(targetButtonsTask).start();

            referenceButtonsTask = new CreateButtonsTask(referenceJson, referencePane.getLabelList(), characterHeight);
            referenceButtonsTask.setOnSucceeded(event -> {
                referencePane.addButtons(referenceButtonsTask.getValue());
                setupAnnotationButtons();
            });
            new Thread(referenceButtonsTask).start();
        }
    }

    private int finished = 0;
    /**
     * Sets up the annotation buttons, including assigning their onClick events and setting their matched annotation
     * buttons.  Note: This could not be done using the tasks' onDone() method, since both tasks are already completed
     * by the time their onFinished() methods begin processing.
     */
    private void setupAnnotationButtons() {
        finished++;
        if (finished >= 2) {
            finished = 0;
            List<AnnotationButton> targetButtons = targetPane.getAnnotationButtonList();
            List<AnnotationButton> refButtons = referencePane.getAnnotationButtonList();

            for (AnnotationButton targetButton: targetButtons) {
                for(AnnotationButton refButton: refButtons) {
                    if (targetButton.getBegin() >= refButton.getBegin() && targetButton.getEnd() <= refButton.getEnd()) {
                        targetButton.addMatch(refButton);
                        refButton.addMatch(targetButton);
                        targetButton.targetTextArea = viewControls.getTargetFeatureTree();
                    }
                }

                targetButton.setOnMouseClicked(event -> selectedAnnotationButton.setValue(targetButton));
            }

            for (AnnotationButton refButton: refButtons) {
                refButton.targetTextArea = viewControls.getReferenceFeatureTree();
                refButton.setOnMouseClicked(event -> selectedAnnotationButton.setValue(refButton));
            }
        } else {
            logger.debug("One of the tasks is not done yet, waiting...");
        }
    }

    /* ******************************
     *                              *
     * Change Listeners             *
     *                              *
     *******************************/

    /**
     * Listens on the AcvContext's selected annotation and updates the document panes' buttons accordingly
     */
    private ChangeListener<String> selectedAnnotationTypeListener = (observable, oldValue, newValue) -> {
        logger.debug("Selected Annotation Changed: {}", newValue);
        resetAnnotationButtons(newValue);
    };

    /**
     * Updates the Context's annotation type list whenever the Annotations objects are updated.  This usually occurs
     * when new documents are loaded.
     */
    private ChangeListener<Annotations> annotationPropertiesListener = (observable, oldValue, newValue) ->
            newValue.getAnnotationKeySet().forEach(key -> {
                if (AcvContext.getInstance().annotationList.contains(key)) {
                    logger.warn("Duplicate key not added: {}", key);
                } else {
                    context.annotationList.add(key);
                }
            });

    private ChangeListener<AnnotationButton> selectedAnnotationButtonListener = ((observable, oldValue, newValue) -> {
        if (oldValue != null) {
            oldValue.clearSelected();
            for (AnnotationButton button: oldValue.matchingButtons) {
                button.clearSelected();
            }
        }

        if (newValue != null) {
            newValue.setSelected();
            for (AnnotationButton button: newValue.matchingButtons) {
                button.setSelected();
            }

            newValue.fire();

            if (newValue.matchingButtons.size() == 1) {
                newValue.matchingButtons.get(0).fire();
            }
        }
    });
}
