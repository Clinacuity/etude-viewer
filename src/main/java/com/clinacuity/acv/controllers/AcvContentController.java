package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.context.MetricValues;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.AnnotationButton;
import com.clinacuity.acv.controls.AnnotationType;
import com.clinacuity.acv.controls.SideBar;
import com.clinacuity.acv.tasks.CreateButtonsTask;
import com.clinacuity.acv.tasks.CreateLabelsFromDocumentTask;
import com.clinacuity.acv.controls.AnnotationButton.MatchType;
import com.clinacuity.acv.tasks.CreateSidebarItemsTask;
import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXDrawer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
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
    @FXML private JFXDrawer drawer;
    @FXML private SideBar sideBar;
    private ObjectProperty<Annotations> targetAnnotationsProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Annotations> referenceAnnotationsProperty = new SimpleObjectProperty<>();
    private ObjectProperty<AnnotationButton> selectedAnnotationButton = new SimpleObjectProperty<>();
    private CreateLabelsFromDocumentTask referenceLabelsTask;
    private CreateLabelsFromDocumentTask targetLabelsTask;
    private CreateButtonsTask targetButtonsTask;
    private CreateButtonsTask referenceButtonsTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        context = AcvContext.getInstance();

        FxTimer.runLater(Duration.ofMillis(500), this::init);
    }

    private void init() {
        setupSideBar();

        setupAnnotationProperties();

        setupDocumentPanes();

        setupViewControls();

        logger.debug("Annotation Comparison View Controller initialized");
    }

    private void setupSideBar() {
        sideBar.setDrawer(drawer);
        drawer.setDefaultDrawerSize(SideBar.MIN_WIDTH);

        CreateSidebarItemsTask sidebarTask = new CreateSidebarItemsTask();
        sidebarTask.setCorpusDictionary(AcvContext.getInstance().getCorpusDictionary());
        sidebarTask.setOnSucceeded(event -> {
            List<HBox> results = sidebarTask.getValue();

            if (results != null) {
                sideBar.setFileList(results);
                results.forEach(box -> box.setOnMouseClicked(click ->
                    sideBar.getDocument(results.indexOf(box))));

                getNextDocument();
            }
        });
        new Thread(sidebarTask).start();
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

        targetAnnotationsProperty.addListener((observable, old, newValue) -> resetLabels(false, newValue));
        referenceAnnotationsProperty.addListener((observable, old, newValue) -> resetLabels(true, newValue));
    }

    /**
     * Sets up the View Controls.  This includes adding the necessary bindings and listeners to the appropriate objects.
     * Note that some listeners are initialized when the ViewControls are created.
     */
    private void setupViewControls() {
        createTableRows();
        context.annotationList.addListener((observable, old, newValue) -> createTableRows());

        context.truePositivesProperty.addListener((obs, old, newValue) -> {
            updateButtons(newValue, MatchType.TRUE_POS, targetPane);
            updateButtons(newValue, MatchType.TRUE_POS, referencePane);
        });
        context.falsePositivesProperty.addListener((obs, old, newValue) -> updateButtons(newValue, MatchType.FALSE_POS, targetPane));
        context.falseNegativesProperty.addListener((obs, old, newValue) -> updateButtons(newValue, MatchType.FALSE_NEG, referencePane));

        viewControls.getPreviousButton().setOnAction(event -> changeAnnotationButton(false));
        viewControls.getClearButton().setOnAction(event -> selectedAnnotationButton.setValue(null));
        viewControls.getNextButton().setOnAction(event -> changeAnnotationButton(true));

        targetAnnotationsProperty.addListener((obs, old, newValue) -> {
            ObservableList<String> list = FXCollections.observableArrayList(newValue.getMatchTypes());
            context.matchingTypes.setValue(list);
            viewControls.setMatchTypeToggleButtons(newValue.getMatchTypes());

            if (!list.isEmpty()) {
                context.selectedMatchTypeProperty.setValue(list.get(0));
            }
        });

        context.selectedAnnotationTypeProperty.addListener(selectedAnnotationTypeListener);
        context.selectedMatchTypeProperty.addListener((observable, old, newValue) -> createTableRows());
    }
    
    private void resetLabels(boolean isReference, Annotations newValue) {
        // both methods have to be the same, referencing different objects. Lambdas require (effectively) final vars
        if (isReference) {
            if (referenceLabelsTask != null && referenceLabelsTask.isRunning()) {
                referenceLabelsTask.cancel();
            }
            referenceLabelsTask = new CreateLabelsFromDocumentTask(newValue.getRawText());
            referenceLabelsTask.setOnSucceeded(event -> {
                referencePane.addLineNumberedLabels(referenceLabelsTask.getValue());
                referencePane.getAnchor().getChildren().addAll(referenceLabelsTask.getLineNumbers());
            });
            new Thread(referenceLabelsTask).start();
        } else {
            if (targetLabelsTask != null && targetLabelsTask.isRunning()) {
                targetLabelsTask.cancel();
            }
            targetLabelsTask = new CreateLabelsFromDocumentTask(newValue.getRawText());
            targetLabelsTask.setOnSucceeded(event -> {
                targetPane.addLineNumberedLabels(targetLabelsTask.getValue());
                targetPane.getAnchor().getChildren().addAll(targetLabelsTask.getLineNumbers());
            });
            new Thread(targetLabelsTask).start();
        }
    }
    
    private void resetAnnotationButtons(String key) {
        if (targetButtonsTask != null && targetButtonsTask.isRunning()) {
            targetButtonsTask.cancel();
        }

        if (referenceButtonsTask != null && referenceButtonsTask.isRunning()) {
            referenceButtonsTask.cancel();
        }

        if (key != null) {
            List<JsonObject> targetJson = targetAnnotationsProperty.getValue().getAnnotationsByKey(key);
            List<JsonObject> referenceJson = referenceAnnotationsProperty.getValue().getAnnotationsByKey(key);

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
        } else {
            targetPane.addButtons(new ArrayList<>());
            referencePane.addButtons(new ArrayList<>());
        }
    }

    private int finished = 0;
    /**
     * Sets up the annotation buttons, including assigning their onClick events and setting their matched annotation
     * buttons.  Note: This could not be done using the tasks' onDone() method, since both tasks are already completed
     * by the time their onFinished() methods begin processing.
     */
    synchronized private void setupAnnotationButtons() {
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

                    if (beginTarget == beginRef && endTarget == endRef) {
                        targetButton.matchingButtons.add(refButton);
                        refButton.matchingButtons.add(targetButton);
                    }

                    targetButton.targetTextArea = targetPane.getFeatureTreeText();
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
            * we will use a separate for-loop.  The cost to performance is O(n).  This loop determines
            * which color to assign the buttons based on the type of match.
            */
            targetButtons.forEach(button -> button.checkMatchTypes(MatchType.FALSE_POS));
            refButtons.forEach(button -> button.checkMatchTypes(MatchType.FALSE_NEG));

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

    private void updateButtons(boolean isChecked, MatchType matchType, AnnotatedDocumentPane documentPane) {
        documentPane.getAnnotationButtonList().forEach(button -> {
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
            case TRUE_POS:
                return context.truePositivesProperty.getValue();
            case FALSE_POS:
                return context.falsePositivesProperty.getValue();
            case FALSE_NEG:
                return context.falseNegativesProperty.getValue();
        }
        return false;
    }

    private void clearFeatureTrees() {
        referencePane.getFeatureTreeText().clear();
        targetPane.getFeatureTreeText().clear();
    }

    private void createTableRows() {
        ObservableList<AnnotationType> types = FXCollections.observableArrayList();

        context.annotationList.forEach(annotationKey -> {
            Annotations annotation = targetAnnotationsProperty.get();
            MetricValues values = annotation.getMetricValues(annotationKey);

            double tp = values.getTruePositive();
            double fp = values.getFalsePositive();
            double fn = values.getFalseNegative();
            double recall = tp / (tp + fn) * 100.0d;
            double precision = tp / (tp + fp) * 100.0d;

            types.add(new AnnotationType(annotationKey, tp, fp, fn, recall, precision));
        });
        viewControls.setTableRows(types);
    }

    private void changeAnnotationButton(boolean isNext) {
        AnnotationButton currentButton = selectedAnnotationButton.getValue();
        if (currentButton != null) {
            if (isNext) {
                if (currentButton.nextButton != null) {
                    selectedAnnotationButton.setValue(currentButton.nextButton);
                } else {
                    logger.warn("Current button does not have a Next value.");
                }
            } else {
                if (currentButton.previousButton != null) {
                    selectedAnnotationButton.setValue(currentButton.previousButton);
                } else {
                    logger.warn("Current button does not have a Previous value.");
                }
            }
        } else {
            logger.warn("No annotation selected!");
        }
    }

    @FXML
    private void getPreviousDocument() {
        sideBar.getPreviousDocument();
    }

    @FXML private void getNextDocument() {
        sideBar.getNextDocument();
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
        resetAnnotationButtons(newValue);
    };

    /**
     * Updates the Context's annotation type list whenever the Annotations objects are updated.  This usually occurs
     * when new documents are loaded.
     */
    private ChangeListener<Annotations> annotationPropertiesListener = (observable, oldValue, newValue) -> {
        List<String> list = FXCollections.observableList(context.annotationList);
        list.clear();
        newValue.getAnnotationKeySet().forEach(key -> {
            if (!list.contains(key)) {
                list.add(key);
            }
        });
        context.annotationList.setValue(FXCollections.observableArrayList(list));
    };

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

    public void collapsePanel() {
        if (drawer.isShown()) {
            drawer.close();
        } else {
            drawer.open();
        }
    }
}
