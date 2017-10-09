package com.clinacuity.acv.tasks;

import com.clinacuity.acv.context.CorpusDictionary;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.util.ArrayList;
import java.util.List;

public class CreateSidebarItemsTask extends Task<List<HBox>> {

    private CorpusDictionary corpus;
    public void setCorpusDictionary(CorpusDictionary corpusDictionary) { corpus = corpusDictionary; }

    @Override
    public List<HBox> call() {
        if (corpus == null) {
            setException(new NullPointerException("Corpus dictionary was not initialized."));
            failed();
            return null;
        }

        return createListOfFiles();
    }

    private List<HBox> createListOfFiles() {
        List<HBox> list = new ArrayList<>();

        corpus.getFileMappings().forEach((key, value) -> {
            HBox fileEntry = new HBox();
            fileEntry.setSpacing(5.0d);
            fileEntry.setId(key);

            Label file = new Label(key);
            file.getStyleClass().add("text-medium-normal");

            Label fileTruePositive = new Label("x.xx%");
            fileTruePositive.getStyleClass().addAll("text-medium-italic", "file-list-metrics");
//            fileTruePositive.setText(Double.toString(corpus.getMetricAnnotationTypeValues("", "").getTruePositive()));

            fileEntry.getChildren().addAll(file, fileTruePositive);
            list.add(fileEntry);
        });

        return list;
    }
}
