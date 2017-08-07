package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.AnnotationButton;
import com.clinacuity.acv.controls.AnnotationType;
import com.clinacuity.acv.tasks.CreateButtonsTask;
import com.clinacuity.acv.tasks.CreateLabelsFromDocumentTask;
import com.clinacuity.acv.controls.AnnotationButton.MatchType;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;
import java.net.URL;
import java.time.Duration;
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
    private CreateLabelsFromDocumentTask getRefLabelsTask;
    private CreateLabelsFromDocumentTask getTargetLabelsTask;
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
        targetPane.maxHeightProperty().bind(referencePane.heightProperty());
        targetPane.maxHeightProperty().bind(referencePane.heightProperty());

        referencePane.getScrollPane().vvalueProperty().bindBidirectional(targetPane.getScrollPane().vvalueProperty());
        referencePane.getScrollPane().hvalueProperty().bindBidirectional(targetPane.getScrollPane().hvalueProperty());

        if (getRefLabelsTask != null && getRefLabelsTask.isRunning()) {
            getRefLabelsTask.cancel();
        }

        if (getTargetLabelsTask != null && getTargetLabelsTask.isRunning()) {
            getTargetLabelsTask.cancel();
        }

        getRefLabelsTask = new CreateLabelsFromDocumentTask(referenceAnnotationsProperty.getValue().getRawText());
        getRefLabelsTask.setOnSucceeded(event -> {
            referencePane.addLineNumberedLabels(getRefLabelsTask.getValue());
            referencePane.getAnchor().getChildren().addAll(getRefLabelsTask.getLineNumbers());
        });
        new Thread(getRefLabelsTask).start();

        getTargetLabelsTask = new CreateLabelsFromDocumentTask(targetAnnotationsProperty.getValue().getRawText());
        getTargetLabelsTask.setOnSucceeded(event -> {
            targetPane.addLineNumberedLabels(getTargetLabelsTask.getValue());
            targetPane.getAnchor().getChildren().addAll(getTargetLabelsTask.getLineNumbers());
        });
        new Thread(getTargetLabelsTask).start();
    }

    /**
     * Sets up the View Controls.  This includes adding the necessary bindings and listeners to the appropriate objects.
     * Note that some listeners are initialized when the ViewControls are created.
     */
    private void setupViewControls() {
        ObservableList<AnnotationType> types = FXCollections.observableArrayList();
        context.annotationList.forEach(item -> types.add(new AnnotationType(item, 10, 12, 14, 0.5d, 0.5d)));
        viewControls.setTableRows(types);

        context.exactMatchesProperty.addListener((obs, old, newValue) -> updateButton(newValue, MatchType.EXACT_MATCH));
        context.overlappingMatchesProperty.addListener((obs, old, newValue) -> updateButton(newValue, MatchType.PARTIAL_MATCH));
        context.noMatchesProperty.addListener((obs, old, newValue) -> updateButton(newValue, MatchType.NO_MATCH));

        context.selectedAnnotationTypeProperty.addListener(selectedAnnotationTypeListener);
    }

    private void resetAnnotationButtons(String key) {
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
        targetButtonsTask = new CreateButtonsTask(targetJson, targetPane.getLabelList());
        targetButtonsTask.setOnSucceeded(event -> {
            targetPane.addButtons(targetButtonsTask.getValue());
            setupAnnotationButtons();
        });
        new Thread(targetButtonsTask).start();

        referenceButtonsTask = new CreateButtonsTask(referenceJson, referencePane.getLabelList());
        referenceButtonsTask.setOnSucceeded(event -> {
            referencePane.addButtons(referenceButtonsTask.getValue());
            setupAnnotationButtons();
        });
        new Thread(referenceButtonsTask).start();
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

            // Link the buttons
            for (AnnotationButton targetButton: targetButtons) {
                for(AnnotationButton refButton: refButtons) {
                    int beginTarget = targetButton.getBegin();
                    int endTarget = targetButton.getEnd();
                    int beginRef = refButton.getBegin();
                    int endRef = refButton.getEnd();

                    if ((beginTarget < endRef && beginRef < endTarget) || (beginTarget == beginRef && endTarget == endRef)) {
                        targetButton.matchingButtons.add(refButton);
                        refButton.matchingButtons.add(targetButton);
                        targetButton.targetTextArea = targetPane.getFeatureTreeText();
                    }
                }

                targetButton.parent = targetPane.getAnchor();
                targetButton.setOnMouseClicked(event -> selectedAnnotationButton.setValue(targetButton));
            }

            for (AnnotationButton refButton: refButtons) {
                refButton.parent = referencePane.getAnchor();
                refButton.targetTextArea = referencePane.getFeatureTreeText();
                refButton.setOnMouseClicked(event -> selectedAnnotationButton.setValue(refButton));
            }

            /*
            * This could be faster by using either of the loops above; but for the sake of separating the logic,
            * we will use a separate for-loop.  The cost to performance is negligible.  This loop determines
            * which color to assign the buttons based on the type of match.
            */
            targetButtons.forEach(AnnotationButton::checkMatchTypes);
            refButtons.forEach(AnnotationButton::checkMatchTypes);

            removeUncheckedAnnotations();
        } else {
            logger.debug("One of the tasks is not done yet, waiting...");
        }
    }

    private void removeUncheckedAnnotations() {
        targetPane.getAnnotationButtonList().forEach(button -> {
            if (!isMatchTypeChecked(button.getMatchType())) {
                button.removeFromParent();
            }
        });

        referencePane.getAnnotationButtonList().forEach(button -> {
            if (!isMatchTypeChecked(button.getMatchType())) {
                button.removeFromParent();
            }
        });
    }

    private void updateButton(boolean isChecked, MatchType matchType) {
        referencePane.getAnnotationButtonList().forEach(button -> {
            if (button.getMatchType() == matchType) {
                if (isChecked) {
                    button.addToParent();
                } else {
                    button.removeFromParent();
                }
            }
        });

        targetPane.getAnnotationButtonList().forEach(button -> {
            if (button.getMatchType() == matchType) {
                if (isChecked) {
                    button.addToParent();
                } else {
                    button.removeFromParent();
                }
            }
        });
    }

    private boolean isMatchTypeChecked(AnnotationButton.MatchType matchType) {
        switch (matchType) {
            case EXACT_MATCH:
                return context.exactMatchesProperty.getValue();
            case PARTIAL_MATCH:
                return context.overlappingMatchesProperty.getValue();
            case NO_MATCH:
                return context.noMatchesProperty.getValue();
        }
        return false;
    }

    private void clearFeatureTrees() {
        referencePane.getFeatureTreeText().clear();
        targetPane.getFeatureTreeText().clear();
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
        clearFeatureTrees();
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
        clearFeatureTrees();

        if (oldValue != null) {
            oldValue.clearSelected();
            oldValue.matchingButtons.forEach(AnnotationButton::clearSelected);
            oldValue.sameAnnotationButtons.forEach(AnnotationButton::clearSelected);
        }

        if (newValue != null) {
            newValue.setSelected();
            newValue.matchingButtons.forEach(AnnotationButton::setSelected);
            newValue.sameAnnotationButtons.forEach(AnnotationButton::setSelected);

            newValue.fire();
            if (newValue.matchingButtons.size() == 1) {
                newValue.matchingButtons.get(0).fire();
            }
        }
    });
}
