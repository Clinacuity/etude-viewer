package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.Annotations;
import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.richtext.CodeArea;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotatedDocumentPane extends ScrollPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private AnchorPane anchor;
    @FXML private CodeArea codeArea;
    private Text placeholderText = new Text();

    private Annotations annotations;
    private List<Button> buttonList = new ArrayList<>();
    private Map<String, List<Annotations>> annotationList = new HashMap<>();

    public AnnotatedDocumentPane(@NamedArg("documentType") String type) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotatedDocumentPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        initialize(type);
    }

    private void initialize(String type) {
        registerSizeBindings();

        getDocumentType(type);

        codeArea.clear();
        codeArea.appendText(annotations.getRawText());

        AcvContext.getInstance().selectedAnnotationProperty.addListener((observable, oldValue, newValue) ->
                selectedAnnotationListener(newValue)
        );
    }

    private void selectedAnnotationListener(String value) {
        logger.error(value);

        if (value.equals(AcvContext.getInstance().getDefaultSelectedAnnotation())) {
            logger.error("Default Annotation is selected; clearing buttons...");
            clearButtons();
        } else {
            logger.error("Annotation count: {}", annotations.getSelectedAnnotationCount());
        }

    }

    // TODO clear buttons
    private void clearButtons() {

    }

    private void getDocumentType(String type) {
        switch(type) {
            case "REFERENCE": {
                annotations = new Annotations(AcvContext.getInstance().referenceDocumentPathProperty.getValueSafe());
            } break;

            case "TARGET": {
                annotations = new Annotations(AcvContext.getInstance().targetDocumentPathProperty.getValueSafe());
            } break ;

            default: {

            }
        }
    }

    private void registerSizeBindings() {
        /* the AnchorPane has to be as small as the current size of the CodeArea (though it can be larger through
            other elements) */
        anchor.minWidthProperty().bind(codeArea.widthProperty());
        anchor.minHeightProperty().bind(codeArea.heightProperty());

        anchor.prefWidthProperty().bind(codeArea.widthProperty());
        anchor.prefHeightProperty().bind(codeArea.heightProperty());

        // the CodeArea has to be as small as this ScrollPane container
        codeArea.minWidthProperty().bind(widthProperty());
        codeArea.minHeightProperty().bind(heightProperty());

        // the placeholder is needed to correctly respond to events
        placeholderText.textProperty().bind(codeArea.textProperty());
        placeholderText.textProperty().addListener(event -> resetCodeAreaWidth());
    }

    private void resetCodeAreaWidth() {
        int maxWidth = 0;
        for (int i = 0; i < codeArea.getParagraphs().size(); i++) {
            if (maxWidth < codeArea.getParagraphLenth(i)) {
                maxWidth = codeArea.getParagraphLenth(i);
            }
        }

        //TODO: reconsider how to calculate line height
//        codeArea.setPrefWidth(maxWidth * AcvContext.getInstance().getFontPixelWidth());
    }

    public enum DocumentType {
        REFERENCE,
        TARGET
    }
}
