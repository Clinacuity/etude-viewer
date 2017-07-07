package com.clinacuity.acv.context;

import com.clinacuity.acv.controllers.AppMainController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AcvContext {
    private static final Logger logger = LogManager.getLogger();

    private static final AcvContext instance = new AcvContext();
    public static AcvContext getInstance() { return instance; }
    public AppMainController mainController;

    // Properties
    public StringProperty referenceDocumentPathProperty = new SimpleStringProperty("");
    public StringProperty targetDocumentPathProperty = new SimpleStringProperty("");
    public StringProperty selectedAnnotationProperty = new SimpleStringProperty("");
    public BooleanProperty exactMatchesProperty = new SimpleBooleanProperty(false);
    public BooleanProperty overlappingMatchesProperty = new SimpleBooleanProperty(false);
    public BooleanProperty subsumedMatchesProperty = new SimpleBooleanProperty(false);
    public BooleanProperty noMatchesProperty = new SimpleBooleanProperty(false);

    public ListProperty<String> annotationList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final String defaultSelectedAnnotation = "Select Annotation...";
    public final String getDefaultSelectedAnnotation() { return defaultSelectedAnnotation; }

    private AcvContext() {
        annotationList.add(defaultSelectedAnnotation);

        referenceDocumentPathProperty.addListener((observable, oldValue, newValue) -> cleanupAnnotationList());
        targetDocumentPathProperty.addListener((observable, oldValue, newValue) -> cleanupAnnotationList());
    }

    private void cleanupAnnotationList() {
        if (annotationList.size() > 1) {
            annotationList.remove(1, annotationList.size() - 1);
            selectedAnnotationProperty.setValue(annotationList.get(0));
        }
    }
}
