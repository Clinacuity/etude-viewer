package com.clinacuity.acv.controls;

import com.google.gson.JsonObject;
import javafx.scene.control.Button;

public class AnnotationButton extends Button {
    JsonObject annotation;

    public AnnotationButton(JsonObject json) {
        setStyle("-fx-focus-traversable: false;");
        annotation = json;
    }
}
