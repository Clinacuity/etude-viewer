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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class ViewControls extends VBox {
    private static final Logger logger = LogManager.getLogger();
    private static final String BUTTON_STYLE = "button-table";
    private static final String BUTTON_SELECTED = "button-table-selected";

    @FXML private JFXCheckBox toggleExactMatches;
    @FXML private JFXCheckBox togglePartialMatches;
    @FXML private JFXCheckBox toggleFalsePosMatch;
    @FXML private JFXCheckBox toggleFalseNegMatch;
    @FXML private TableView<AnnotationType> annotationTable;
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
        context.falsePositivesProperty.bindBidirectional(toggleFalsePosMatch.selectedProperty());
        context.falseNegativesProperty.bindBidirectional(toggleFalseNegMatch.selectedProperty());

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
                setText(null);
                setGraphic(empty ? null : getTableButton(item, ColumnType.NAME));
            }
        });

        truePosColumn.setGraphic(getHeaderGraphic(ColumnType.TP));
        truePosColumn.setText(null);

        falsePosColumn.setGraphic(getHeaderGraphic(ColumnType.FP));
        falsePosColumn.setText(null);

        falseNegColumn.setGraphic(getHeaderGraphic(ColumnType.FN));
        falseNegColumn.setText(null);
    }

    private Button getTableButton(String text, ColumnType type) {
        Button button = new Button(text);
        button.getStyleClass().add(BUTTON_STYLE);

        switch(type) {
            case NAME:
                button.setOnAction(event -> AcvContext.getInstance().selectedAnnotationTypeProperty.set(text));
                logger.error("Stuff happened and got set to {}", text);
                break;

            case TP:
            case FP:
            case FN:
                break;
        }

        return button;
    }

    private HBox getHeaderGraphic(ColumnType type) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label(type.toString());
        JFXCheckBox checkBox = new JFXCheckBox();

        switch(type) {
            case TP:
                checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                    logger.error("TRUE POS new value: {}", newValue);
                }));
                break;

            case FP:
                checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                    logger.error("FALSE POS new value: {}", newValue);
                }));
                break;

            case FN:
                checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
                    logger.error("FALSE NEG new value: {}", newValue);
                }));
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

    private enum ColumnType {
        NAME,
        TP,
        FP,
        FN
    }
}
