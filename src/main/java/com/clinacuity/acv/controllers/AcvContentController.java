package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.context.MetricValues;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.AnnotationButton;
import com.clinacuity.acv.controls.AnnotationType;
import com.clinacuity.acv.controls.SideBar;
import com.clinacuity.acv.controls.ViewControls;
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
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
    @FXML private Label systemFile;
    @FXML private Label referenceFile;
    private Annotations targetAnnotations;
    private Annotations referenceAnnotations;
    private ObjectProperty<AnnotationButton> selectedAnnotationButton = new SimpleObjectProperty<>();
    private CreateLabelsFromDocumentTask referenceLabelsTask;
    private CreateLabelsFromDocumentTask targetLabelsTask;
    private CreateButtonsTask targetButtonsTask;
    private CreateButtonsTask referenceButtonsTask;
    private int documentsLoaded = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        context = AcvContext.getInstance();

        FxTimer.runLater(Duration.ofMillis(500), this::init);
    }

    private void init() {
        initSideBar();

        initDocumentPanes();

        initViewControls();

        logger.debug("Annotation Comparison View Controller finished initialization");
    }

    /**
     * Sets up the sidebar's drawer, size, items, and listeners to the changes on the sidebar
     */
    private void initSideBar() {
        sideBar.setDrawer(drawer);
        drawer.setDefaultDrawerSize(SideBar.MIN_WIDTH);

        sideBar.selectedTargetDocumentProperty().addListener((obs, old, newValue) -> {
            String[] paths = newValue.split("/");
            systemFile.setText(paths[paths.length - 1]);
            targetAnnotations = new Annotations(newValue);
            resetPanes();
        });

        sideBar.selectedReferenceDocumentProperty().addListener((obs, old, newValue) -> {
            String[] paths = newValue.split("/");
            referenceFile.setText(paths[paths.length - 1]);
            referenceAnnotations = new Annotations(newValue);
            resetPanes();
        });

        CreateSidebarItemsTask sidebarTask = new CreateSidebarItemsTask();
        sidebarTask.setCorpusDictionary(context.getCorpusDictionary());
        sidebarTask.setOnSucceeded(event -> {
            List<VBox> results = sidebarTask.getValue();

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
     * Sets up the children AnnotatedDocumentPanes by calling their initialize method. It also binds their scrollbars
     */
    private void initDocumentPanes() {
        referencePane.getScrollPane().vvalueProperty().bindBidirectional(targetPane.getScrollPane().vvalueProperty());
        referencePane.getScrollPane().hvalueProperty().bindBidirectional(targetPane.getScrollPane().hvalueProperty());
    }

    private void initViewControls() {
        context.truePositivesProperty.addListener((obs, old, newValue) -> {
            updateButtons(newValue, MatchType.TRUE_POS, targetPane);
            updateButtons(newValue, MatchType.TRUE_POS, referencePane);
        });
        context.falsePositivesProperty.addListener((obs, old, newValue) -> updateButtons(newValue, MatchType.FALSE_POS, targetPane));
        context.falseNegativesProperty.addListener((obs, old, newValue) -> updateButtons(newValue, MatchType.FALSE_NEG, referencePane));

        viewControls.getPreviousButton().setOnAction(event -> {
            AnnotationButton currentButton = selectedAnnotationButton.getValue();
            if (currentButton != null) {
                if (currentButton.previousButton != null) {
                    selectedAnnotationButton.setValue(currentButton.previousButton);
                } else {
                    logger.debug("Current Annotation Button does not have a Previous value.");
                }
            }
        });
        viewControls.getNextButton().setOnAction(event -> {
            AnnotationButton currentButton = selectedAnnotationButton.getValue();
            if (currentButton != null) {
                if (currentButton.nextButton != null) {
                    selectedAnnotationButton.setValue(currentButton.nextButton);
                } else {
                    logger.debug("Current Annotation Button does not have a Next value.");
                }
            }
        });
        viewControls.getClearButton().setOnAction(event -> selectedAnnotationButton.setValue(null));
    }

    synchronized private void resetPanes() {
        documentsLoaded++;
        if (documentsLoaded >= 2) {
            documentsLoaded = 0;
            removeViewControlsListeners();
            cancelEvents();
            clearPanes();

            populateMatchTypes();

            List<String> list = FXCollections.observableList(context.annotationList);
            list.clear();

            targetAnnotations.getAnnotationKeySet().forEach(key -> {
                if (!list.contains(key)) {
                    list.add(key);
                }
            });
            referenceAnnotations.getAnnotationKeySet().forEach(key -> {
                if (!list.contains(key)) {
                    list.add(key);
                }
            });

            context.annotationList.setValue(FXCollections.observableArrayList(list));

            referenceLabelsTask = new CreateLabelsFromDocumentTask(referenceAnnotations.getRawText());
            referenceLabelsTask.setOnSucceeded(event -> {
                referencePane.addLineNumberedLabels(referenceLabelsTask.getValue());
                resetViewControls();
            });
            new Thread(referenceLabelsTask).start();

            targetLabelsTask = new CreateLabelsFromDocumentTask(targetAnnotations.getRawText());
            targetLabelsTask.setOnSucceeded(event -> {
                targetPane.addLineNumberedLabels(targetLabelsTask.getValue());
                resetViewControls();
            });
            new Thread(targetLabelsTask).start();
        }
    }

    private void populateMatchTypes() {
        List<String> matchTypes = targetAnnotations.getMatchTypes();
        viewControls.setMatchTypeToggleButtons(matchTypes);

        if (matchTypes.size() > 0) {
            context.selectedMatchTypeProperty.setValue(matchTypes.get(0));
        }
    }

    synchronized private void resetViewControls() {
        documentsLoaded++;
        if (documentsLoaded >= 2) {
            documentsLoaded = 0;
            createTableRows();
            setViewControlsListeners();
            viewControls.getAnnotationTable().getSelectionModel().clearSelection();
        }
    }

    private void removeViewControlsListeners() {
        viewControls.getAnnotationTable().getSelectionModel().selectedItemProperty().removeListener(onTableSelectionChanged);
        context.selectedMatchTypeProperty.removeListener(onMatchTypeSelectionChanged);
        selectedAnnotationButton.removeListener(onAnnotationButtonClicked);
    }

    private void setViewControlsListeners() {
        viewControls.getAnnotationTable().getSelectionModel().selectedItemProperty().addListener(onTableSelectionChanged);
        context.selectedMatchTypeProperty.addListener(onMatchTypeSelectionChanged);
        selectedAnnotationButton.addListener(onAnnotationButtonClicked);
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

    private void createTableRows() {
        ObservableList<AnnotationType> types = FXCollections.observableArrayList();

        context.annotationList.forEach(annotationKey -> {
            Annotations annotation = targetAnnotations;
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

    private void resetAnnotationButtons(String key) {
        List<JsonObject> targetJson = targetAnnotations.getAnnotationsByKey(key);
        List<JsonObject> referenceJson = referenceAnnotations.getAnnotationsByKey(key);

        targetButtonsTask = new CreateButtonsTask(targetJson, targetPane.getLabelList());
        targetButtonsTask.setOnSucceeded(event -> {
            targetPane.addButtons(targetButtonsTask.getValue());
            setupAnnotationButtons();
        });
        targetButtonsTask.setOnFailed(event -> {
            logger.throwing(targetButtonsTask.getException());
            targetPane.addButtons(new ArrayList<>());
            setupAnnotationButtons();
        });
        new Thread(targetButtonsTask).start();

        referenceButtonsTask = new CreateButtonsTask(referenceJson, referencePane.getLabelList());
        referenceButtonsTask.setOnSucceeded(event -> {
            referencePane.addButtons(referenceButtonsTask.getValue());
            setupAnnotationButtons();
        });
        referenceButtonsTask.setOnFailed(event -> {
            logger.throwing(referenceButtonsTask.getException());
            referencePane.addButtons(new ArrayList<>());
            setupAnnotationButtons();
        });
        new Thread(referenceButtonsTask).start();
    }

    synchronized private void setupAnnotationButtons() {
        documentsLoaded++;
        if (documentsLoaded >= 2) {
            documentsLoaded = 0;
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
                }

                targetButton.targetTextArea = targetPane.getFeatureTreeText();
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

    private void cancelEvents() {
        if (targetLabelsTask != null && targetLabelsTask.isRunning()) {
            targetLabelsTask.cancel();
        }

        if (referenceLabelsTask != null && referenceLabelsTask.isRunning()) {
            referenceLabelsTask.cancel();
        }

        if (targetButtonsTask != null && targetButtonsTask.isRunning()) {
            targetButtonsTask.cancel();
        }

        if (referenceButtonsTask != null && referenceButtonsTask.isRunning()) {
            referenceButtonsTask.cancel();
        }
    }

    private void clearPanes() {
        targetPane.reset();
        referencePane.reset();
    }

    @FXML private void getPreviousDocument() {
        sideBar.getPreviousDocument();
    }

    @FXML private void getNextDocument() {
        sideBar.getNextDocument();
    }

    @FXML private void collapsePanel() {
        if (drawer.isShown()) {
            drawer.close();
        } else {
            drawer.open();
        }
    }

    /** ******************
     *                   *
     * Change Listeners  *
     *                   *
     ********************/

    private ChangeListener<AnnotationType> onTableSelectionChanged = ((observable, oldValue, newValue) -> {
        cancelEvents();
        if (newValue != null) {
            context.selectedAnnotationTypeProperty.setValue(newValue.getAnnotationName());
            resetAnnotationButtons(newValue.getAnnotationName());
        } else {
            targetPane.clearFeatureTree();
            referencePane.clearFeatureTree();
        }
        selectedAnnotationButton.setValue(null);
    });

    private ChangeListener<String> onMatchTypeSelectionChanged = ((observable, oldValue, newValue) -> createTableRows());

    private ChangeListener<AnnotationButton> onAnnotationButtonClicked = ((observable, oldValue, newValue) -> {
        targetPane.clearFeatureTree();
        referencePane.clearFeatureTree();

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
                newValue.matchingButtons.forEach(AnnotationButton::fire);
            }

            // update scroll bars
            double scrollPaneHeight = targetPane.getHeight() * 0.5d / targetPane.getAnchor().getHeight();
            double currentScroll = targetPane.getScrollPane().getVvalue();
            double targetScroll = newValue.getLayoutY() / targetPane.getAnchor().getHeight();

            if (targetScroll - currentScroll <= -scrollPaneHeight ||
                    targetScroll - currentScroll > scrollPaneHeight) {
                targetPane.getScrollPane().setVvalue(targetScroll);
                referencePane.getScrollPane().setVvalue(targetScroll);
            }
        }
    });
}
