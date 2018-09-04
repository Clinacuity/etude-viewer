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
import com.clinacuity.acv.controls.AnnotationButton.MatchType;
import com.clinacuity.acv.tasks.CreateLabelsTask;
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
import static com.clinacuity.acv.controllers.ConfigurationBuilderController.CorpusType;

public class AcvContentController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    private AcvContext context;

    @FXML private AnnotatedDocumentPane referencePane;
    @FXML private AnnotatedDocumentPane systemOutPane;
    @FXML private ViewControls viewControls;
    @FXML private JFXDrawer drawer;
    @FXML private SideBar sideBar;
    @FXML private Label systemFile;
    @FXML private Label referenceFile;
    private Annotations systemOutAnnotations;
    private Annotations referenceAnnotations;
    private ObjectProperty<AnnotationButton> selectedAnnotationButton = new SimpleObjectProperty<>();
    private CreateLabelsTask createLabelsTask;
    private CreateButtonsTask createButtonsTask;

    // TODO: no ticket but we should get rid of this var
    private int documentsLoaded = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        context = AcvContext.getInstance();

        FxTimer.runLater(Duration.ofMillis(500), this::init);
    }

    private void init() {
        initDocumentPanes();

        initSideBar();

        initViewControls();

        logger.debug("Annotation Comparison View Controller finished initialization");
    }

    /**
     * Sets up the sidebar's drawer, size, items, and listeners to the changes on the sidebar
     */
    private void initSideBar() {
        sideBar.setDrawer(drawer);
        drawer.setDefaultDrawerSize(SideBar.MIN_WIDTH);

        sideBar.selectedSystemOutDocumentProperty().addListener((obs, old, newValue) -> {
            String[] paths = newValue.split("/");
            systemFile.setText(paths[paths.length - 1]);
            systemOutAnnotations = new Annotations(newValue);
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
        referencePane.getScrollPane().vvalueProperty().bindBidirectional(systemOutPane.getScrollPane().vvalueProperty());
        referencePane.getScrollPane().hvalueProperty().bindBidirectional(systemOutPane.getScrollPane().hvalueProperty());
    }

    private void initViewControls() {
        context.truePositivesProperty.addListener((obs, old, newValue) -> {
            updateButtons(newValue, MatchType.TRUE_POS, systemOutPane);
            updateButtons(newValue, MatchType.TRUE_POS, referencePane);
        });
        context.falsePositivesProperty.addListener((obs, old, newValue) -> updateButtons(newValue, MatchType.FALSE_POS, systemOutPane));
        context.falseNegativesProperty.addListener((obs, old, newValue) -> updateButtons(newValue, MatchType.FALSE_NEG, referencePane));

        viewControls.getNextButton().setOnAction(event -> {
            AnnotationButton currentButton = selectedAnnotationButton.getValue();
            if (currentButton != null) {
                if (currentButton.nextButton != null) {
                    selectedAnnotationButton.setValue(currentButton.nextButton);
                } else {
                    logger.debug("Current Annotation Button does not have a Next value.");
                }
            } else {
                if (!systemOutPane.getAnnotationButtonList().isEmpty()) {
                    if (!referencePane.getAnnotationButtonList().isEmpty()) {
                        if (systemOutPane.getAnnotationButtonList().get(0).getBegin() > referencePane.getAnnotationButtonList().get(0).getBegin()) {
                            selectedAnnotationButton.setValue(referencePane.getAnnotationButtonList().get(0));
                        } else {
                            selectedAnnotationButton.setValue(systemOutPane.getAnnotationButtonList().get(0));
                        }
                    } else {
                        selectedAnnotationButton.setValue(systemOutPane.getAnnotationButtonList().get(0));
                    }
                } else {
                    if (!referencePane.getAnnotationButtonList().isEmpty()) {
                        selectedAnnotationButton.setValue(referencePane.getAnnotationButtonList().get(0));
                    } else {
                        logger.warn("There are no buttons here! If there's a table row selected, this shouldn't occur...");
                    }
                }
            }
        });

        viewControls.getPreviousButton().setOnAction(event -> {
            AnnotationButton currentButton = selectedAnnotationButton.getValue();
            if (currentButton != null) {
                if (currentButton.previousButton != null) {
                    selectedAnnotationButton.setValue(currentButton.previousButton);
                } else {
                    logger.debug("Current Annotation Button does not have a Previous value.");
                }
            } else {
                if (!systemOutPane.getAnnotationButtonList().isEmpty()) {
                    if (!referencePane.getAnnotationButtonList().isEmpty()) {
                        if (systemOutPane.getAnnotationButtonList().get(systemOutPane.getAnnotationButtonList().size() - 1).getBegin()
                                < referencePane.getAnnotationButtonList().get(referencePane.getAnnotationButtonList().size() - 1).getBegin()) {
                            selectedAnnotationButton.setValue(referencePane
                                    .getAnnotationButtonList().get(referencePane.getAnnotationButtonList().size() - 1));
                        } else {
                            selectedAnnotationButton.setValue(systemOutPane
                                    .getAnnotationButtonList().get(systemOutPane.getAnnotationButtonList().size() - 1));
                        }
                    } else {
                        selectedAnnotationButton.setValue(systemOutPane
                                .getAnnotationButtonList().get(systemOutPane.getAnnotationButtonList().size() - 1));
                    }
                } else {
                    if (!referencePane.getAnnotationButtonList().isEmpty()) {
                        selectedAnnotationButton.setValue(referencePane
                                .getAnnotationButtonList().get(referencePane.getAnnotationButtonList().size() - 1));
                    } else {
                        logger.warn("There are no buttons here! If there's a table row selected, this shouldn't occur...");
                    }
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

            systemOutAnnotations.getAnnotationKeySet().forEach(key -> {
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

            createLabelsTask = new CreateLabelsTask(systemOutAnnotations.getRawText(), referenceAnnotations.getRawText());
            createLabelsTask.setOnSucceeded(event -> {
                systemOutPane.addLineNumberedLabels(createLabelsTask.getValue().get(CorpusType.SYSTEM));
                referencePane.addLineNumberedLabels(createLabelsTask.getValue().get(CorpusType.REFERENCE));
                resetViewControls();
            });
            createLabelsTask.setOnFailed(event -> {
                systemOutPane.addLineNumberedLabels(null);
                referencePane.addLineNumberedLabels(null);
                logger.throwing(createLabelsTask.getException());
            });
            new Thread(createLabelsTask).start();
        }
    }

    private void populateMatchTypes() {
        List<String> matchTypes = systemOutAnnotations.getMatchTypes();
        viewControls.setMatchTypeToggleButtons(matchTypes);

        if (matchTypes.size() > 0) {
            context.selectedMatchTypeProperty.setValue(matchTypes.get(0));
        }
    }

    synchronized private void resetViewControls() {
        createTableRows();
        setViewControlsListeners();
        viewControls.getAnnotationTable().getSelectionModel().clearSelection();
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
            Annotations annotation = systemOutAnnotations;
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
        List<JsonObject> systemOutJson = systemOutAnnotations.getAnnotationsByKey(key);
        List<JsonObject> referenceJson = referenceAnnotations.getAnnotationsByKey(key);

        createButtonsTask = new CreateButtonsTask(systemOutJson, systemOutPane.getLabelList(), referenceJson, referencePane.getLabelList());
        createButtonsTask.setOnSucceeded(event -> {
            systemOutPane.addButtons(createButtonsTask.getValue().get(CorpusType.SYSTEM));
            referencePane.addButtons(createButtonsTask.getValue().get(CorpusType.REFERENCE));
            removeUncheckedAnnotations();
        });
        createButtonsTask.setOnFailed(event -> {
            logger.throwing(createButtonsTask.getException());
            systemOutPane.addButtons(new ArrayList<>());
            removeUncheckedAnnotations();
        });

        createButtonsTask.setSelectedAnnotationButton(selectedAnnotationButton);
        createButtonsTask.setSystemOutPane(systemOutPane);
        createButtonsTask.setReferencePane(referencePane);
        new Thread(createButtonsTask).start();
    }

    private void removeUncheckedAnnotations() {
        systemOutPane.getAnnotationButtonList().forEach(button -> {
            if (isMatchTypeChecked(button.getMatchType())) {
                button.removeFromParent();
            }
        });

        referencePane.getAnnotationButtonList().forEach(button -> {
            if (isMatchTypeChecked(button.getMatchType())) {
                button.removeFromParent();
            }
        });
    }

    private boolean isMatchTypeChecked(AnnotationButton.MatchType matchType) {
        switch (matchType) {
            case TRUE_POS:
                return !context.truePositivesProperty.getValue();
            case FALSE_POS:
                return !context.falsePositivesProperty.getValue();
            case FALSE_NEG:
                return !context.falseNegativesProperty.getValue();
        }
        return true;
    }

    private void cancelEvents() {
        if (createLabelsTask != null && createLabelsTask.isRunning()) {
            createLabelsTask.cancel();
        }

        if (createButtonsTask != null && createButtonsTask.isRunning()) {
            createButtonsTask.cancel();
        }
    }

    private void clearPanes() {
        systemOutPane.reset();
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
            systemOutPane.clearFeatureTree();
            referencePane.clearFeatureTree();
        }
        selectedAnnotationButton.setValue(null);
    });

    private ChangeListener<String> onMatchTypeSelectionChanged = ((observable, oldValue, newValue) -> createTableRows());

    private ChangeListener<AnnotationButton> onAnnotationButtonClicked = ((observable, oldValue, newValue) -> {
        systemOutPane.clearFeatureTree();
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

            // update scroll bar
            double scrollPaneHeight = systemOutPane.getScrollPane().getContent().getBoundsInLocal().getHeight();
            double nodeHeight = newValue.getBoundsInParent().getMaxY();
            systemOutPane.getScrollPane().setVvalue(nodeHeight / scrollPaneHeight);
        }
    });
}
