package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXDrawer;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;

public class SideBar extends VBox {
    private static final Logger logger = LogManager.getLogger();

    public static final double MIN_WIDTH = 250.0d;

    private AcvContext context = AcvContext.getInstance();
    private int fileListSize = -1;
    private int selectedBox = -1;

    private JFXDrawer parentDrawer;
    public void setDrawer(JFXDrawer drawer) { parentDrawer = drawer; }

    private  StringProperty targetDocProperty = new SimpleStringProperty("");
    private StringProperty referenceDocProperty = new SimpleStringProperty("");
    public ReadOnlyStringProperty selectedTargetDocumentProperty() { return targetDocProperty; }
    public ReadOnlyStringProperty selectedReferenceDocumentProperty() { return referenceDocProperty; }

    @FXML private VBox fileList;

    public SideBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/SideBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    @FXML
    private void collapse() {
        if (parentDrawer != null && parentDrawer.isShown()) {
            parentDrawer.close();
        }
    }

    public void setFileList(List<VBox> files) {
        fileList.getChildren().clear();
        fileList.getChildren().addAll(files);
        fileListSize = files.size();
    }

    public void getDocument(int index) {
        context.annotationList.clear();
        selectedBox = index;

        loadDocument();
    }

    public void getNextDocument() {
        if (selectedBox + 1 >= fileListSize) {
            logger.debug("Doing nothing -- already at the last document.");
        } else {
            selectedBox++;
            loadDocument();
        }
    }

    public void getPreviousDocument() {
        if (selectedBox <= 0) {
            logger.debug("Doing nothing -- already on the first document.");
        } else {
            selectedBox--;
            loadDocument();
        }
    }

    private void loadDocument() {
        String id = fileList.getChildren().get(selectedBox).getId();
        String target = context.targetDirectoryProperty.getValueSafe() + context.getCorpusDictionary().getFileMappings().get(id);
        String reference = context.referenceDirectoryProperty.getValueSafe() + id;

        targetDocProperty.setValue(target);
        referenceDocProperty.setValue(reference);
        updateContent();
    }

    private void updateContent() {
        fileList.getChildren().forEach(item -> item.getStyleClass().remove("button-annotation-selected"));
        fileList.getChildren().get(selectedBox).getStyleClass().add("button-annotation-selected");
    }
}
