package com.clinacuity.acv.tasks;

import com.clinacuity.acv.context.CorpusDictionary;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateSidebarItemsTask extends Task<ScrollPane> {
    private static final Logger logger = LogManager.getLogger();

    private CorpusDictionary corpus;
    public void setCorpusDictionary(CorpusDictionary corpusDictionary) { corpus = corpusDictionary; }

    @Override
    public ScrollPane call() {
        if (corpus == null) {
            setException(new NullPointerException("Corpus dictionary was not initialized."));
            failed();
            return null;
        }

        return createListOfFiles();
    }

    private ScrollPane createListOfFiles() {
        VBox fileList = new VBox();
        fileList.setSpacing(1.0d);

        corpus.getFileMappings().forEach((key, value) -> {
            HBox fileEntry = new HBox();
            fileEntry.setSpacing(5.0d);

            Label file = new Label(key);
            file.getStyleClass().add("text-medium-normal");

            Label fileTruePositive = new Label("x.xx%");
            fileTruePositive.getStyleClass().addAll("text-medium-italic", "file-list-metrics");
//            fileTruePositive.setText(Double.toString(corpus.getMetricAnnotationTypeValues("", "").getTruePositive()));

            fileEntry.setOnMouseClicked(
                    event -> logger.error("Clicking on these does not do anything yet...\n{}", key));

            fileEntry.getChildren().addAll(file, fileTruePositive);
            fileList.getChildren().add(fileEntry);
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(fileList);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(new Insets(3.0d));

        return scrollPane;
    }
}
