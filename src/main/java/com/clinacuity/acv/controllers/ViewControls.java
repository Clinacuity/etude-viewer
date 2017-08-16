package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.controls.AnnotationType;
import com.jfoenix.controls.JFXCheckBox;
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

public class ViewControls extends VBox {
    private static final Logger logger = LogManager.getLogger();

    @FXML private JFXCheckBox toggleExactMatches;
    @FXML private JFXCheckBox togglePartialMatches;
    @FXML private TableView<AnnotationType> annotationTable;
    @FXML private Button previousButton;
    @FXML private Button clearButton;
    @FXML private Button nextButton;
    @FXML private TableColumn<AnnotationType, String> annotationNameColumn;
    @FXML private TableColumn<AnnotationType, String> truePosColumn;
    @FXML private TableColumn<AnnotationType, String> falsePosColumn;
    @FXML private TableColumn<AnnotationType, String> falseNegColumn;

    public ViewControls() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ViewControls.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        AcvContext context = AcvContext.getInstance();

        // assign listener events to toggles
        context.exactMatchesProperty.bindBidirectional(toggleExactMatches.selectedProperty());
        context.overlappingMatchesProperty.bindBidirectional(togglePartialMatches.selectedProperty());

        annotationTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            logger.debug(newValue.getAnnotationName());
            context.selectedAnnotationTypeProperty.setValue(newValue.getAnnotationName());
        });

        setTableColumns();
    }

    private void setTableColumns() {
        annotationNameColumn.setCellFactory(param -> new TableCell<AnnotationType, String>() {
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
        checkBox.setCheckedColor(Paint.valueOf("OrangeRed"));
        checkBox.setUnCheckedColor(Paint.valueOf("OrangeRed"));
        checkBox.setScaleX(0.8d);
        checkBox.setScaleY(0.8d);

        switch(type) {
            case TP:
                checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                    // TODO: bind with Exact and Partial matches
                    logger.error("TRUE POS new value: {}", newValue);
                }));
                break;

            case FP:
                checkBox.selectedProperty().bindBidirectional(AcvContext.getInstance().falsePositivesProperty);
                break;

            case FN:
                checkBox.selectedProperty().bindBidirectional(AcvContext.getInstance().falseNegativesProperty);
                break;
        }

        header.getChildren().addAll(headerLabel, checkBox);
        return header;
    }

    void setTableRows(ObservableList<AnnotationType> types) {
        Button button = new Button("Hello");
        button.setOnAction(event -> logger.error("SAY WHAT?!"));

        annotationTable.setItems(types);
    }

    Button getPreviousButton() { return previousButton; }
    Button getClearButton() { return clearButton; }
    Button getNextButton() { return nextButton; }

    private enum ColumnType {
        NAME,
        TP,
        FP,
        FN
    }
}
