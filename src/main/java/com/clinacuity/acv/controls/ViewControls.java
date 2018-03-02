package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;

public class ViewControls extends VBox {
    private static final Logger logger = LogManager.getLogger();
    private static final ToggleGroup TOGGLE_GROUP = new ToggleGroup();

    @FXML private VBox matchingTypeToggles;
    @FXML private Button previousButton;
    @FXML private Button clearButton;
    @FXML private Button nextButton;
    @FXML private TableView<AnnotationType> annotationTable;
    @FXML private TableColumn<AnnotationType, String> annotationNameColumn;
    @FXML private TableColumn<AnnotationType, String> truePosColumn;
    @FXML private TableColumn<AnnotationType, String> falsePosColumn;
    @FXML private TableColumn<AnnotationType, String> falseNegColumn;

    public TableView<AnnotationType> getAnnotationTable() { return annotationTable; }

    public ViewControls() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ViewControls.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        initializeTableColumns();
    }

    private void initializeTableColumns() {
        annotationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        annotationNameColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                setGraphic(null);
            }
        });

        truePosColumn.setGraphic(getHeaderGraphic(ColumnType.TP));
        truePosColumn.setText(null);

        falsePosColumn.setGraphic(getHeaderGraphic(ColumnType.FP));
        falsePosColumn.setText(null);

        falseNegColumn.setGraphic(getHeaderGraphic(ColumnType.FN));
        falseNegColumn.setText(null);
    }

    private HBox getHeaderGraphic(ColumnType type) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label(type.toString());
        JFXCheckBox checkBox = new JFXCheckBox();
        checkBox.setScaleX(0.8d);
        checkBox.setScaleY(0.8d);

        switch(type) {
            case TP:
                checkBox.selectedProperty().bindBidirectional(AcvContext.getInstance().truePositivesProperty);
                checkBox.setCheckedColor(Paint.valueOf("DodgerBlue"));
                checkBox.setUnCheckedColor(Paint.valueOf("DodgerBlue"));
                break;

            case FP:
                checkBox.selectedProperty().bindBidirectional(AcvContext.getInstance().falsePositivesProperty);
                checkBox.setCheckedColor(Paint.valueOf("DarkOrchid"));
                checkBox.setUnCheckedColor(Paint.valueOf("DarkOrchid"));
                break;

            case FN:
                checkBox.selectedProperty().bindBidirectional(AcvContext.getInstance().falseNegativesProperty);
                checkBox.setCheckedColor(Paint.valueOf("OrangeRed"));
                checkBox.setUnCheckedColor(Paint.valueOf("OrangeRed"));
                break;
        }

        header.getChildren().addAll(headerLabel, checkBox);
        return header;
    }

    private JFXRadioButton addToggleButton(String item) {
        JFXRadioButton button = new JFXRadioButton(item);
        button.setToggleGroup(TOGGLE_GROUP);
        button.getStyleClass().add("text-medium-normal");
        button.setOnAction(event -> AcvContext.getInstance().selectedMatchTypeProperty.setValue(button.getText()));
        return button;
    }

    @FXML
    private void clearTableSelection() {
        annotationTable.getSelectionModel().clearSelection();
    }

    public Button getPreviousButton() { return previousButton; }
    public Button getClearButton() { return clearButton; }
    public Button getNextButton() { return nextButton; }

    public void setMatchTypeToggleButtons(List<String> items) {
        matchingTypeToggles.getChildren().clear();
        items.forEach(item -> matchingTypeToggles.getChildren().add(addToggleButton(item)));
        ((JFXRadioButton)matchingTypeToggles.getChildren().get(0)).setSelected(true);
    }

    public void setTableRows(ObservableList<AnnotationType> types) {
        annotationTable.setItems(types);

        if (types != null) {
            if (types.size() > 0) {
                annotationTable.getSelectionModel().select(0);
            }
        } else {
            logger.error("types are null...");
        }
    }

    private enum ColumnType {
        TP,
        FP,
        FN
    }
}
