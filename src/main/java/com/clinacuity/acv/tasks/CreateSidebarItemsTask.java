package com.clinacuity.acv.tasks;

import com.clinacuity.acv.context.CorpusDictionary;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CreateSidebarItemsTask extends Task<List<VBox>> {

    private CorpusDictionary corpus;
    public void setCorpusDictionary(CorpusDictionary corpusDictionary) { corpus = corpusDictionary; }

    @Override
    public List<VBox> call() {
        if (corpus == null) {
            setException(new NullPointerException("Corpus dictionary was not initialized."));
            failed();
            return null;
        }

        return createListOfFiles();
    }

    private List<VBox> createListOfFiles() {
        List<VBox> list = new ArrayList<>();

        corpus.getFileMappings().forEach((key, value) -> {
            VBox fileEntry = new VBox();
            fileEntry.setSpacing(2.0d);
            fileEntry.setId(key);
            fileEntry.setPadding(new Insets(0, 0, 0, 5.0d));

            Label file = new Label(key);
            file.getStyleClass().add("text-medium-normal");

            HBox fileMetrics = new HBox();
            fileMetrics.setSpacing(10.0d);
            fileMetrics.setPadding(new Insets(0, 0, 0, 10.0));

            Label falsePos = new Label("FP: [pending]");
            falsePos.getStyleClass().add("false-positives-count");

            Label falseNeg = new Label("FN: [pending]");
            falseNeg.getStyleClass().add("false-negatives-count");

            fileMetrics.getChildren().addAll(falsePos, falseNeg);

            Label fileTruePositive = new Label("x.xx%");
            fileTruePositive.getStyleClass().addAll("text-medium-italic", "file-list-metrics");

            fileEntry.getChildren().addAll(file, fileMetrics, new Separator());
            list.add(fileEntry);
        });

        return list;
    }
}
