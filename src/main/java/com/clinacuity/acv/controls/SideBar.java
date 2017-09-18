package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXDrawer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
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

    public void setFileList(List<HBox> files) {
        fileList.getChildren().clear();
        fileList.getChildren().addAll(files);
        fileListSize = files.size();
    }

    public void getDocument(int index, String keyId) {
        context.annotationList.clear();
        selectedBox = index;

        logger.error(context.corpusFilePathProperty.getValueSafe());

        // TODO: Load files from appropriate directory
        context.targetDocumentPathProperty.setValue("/Users/jkaccetta/Desktop/t_out/" + keyId);
        context.referenceDocumentPathProperty.setValue("/Users/jkaccetta/Desktop/g_2/" + context.getCorpusDictionary().getFileMappings().get(keyId));
    }

    public void getNextDocument() {
        if (selectedBox + 1 >= fileListSize) {
            logger.warn("Doing nothing -- already at the last document.");
        } else {
            selectedBox++;
        }
    }

    public void getPreviousDocument() {
        if (selectedBox - 1 <= 0) {
            logger.warn("Doing nothing -- already on the first document.");
        }
    }
}
