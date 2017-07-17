package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.scene.control.Button;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AnnotationButton extends Button {
    private static final Logger logger = LogManager.getLogger();

    private JsonObject annotation;
    public List<AnnotationButton> matchingButtons = new ArrayList<>();

    public AnnotationButton(JsonObject json) {
        setStyle("-fx-focus-traversable: false;");
        annotation = json;
    }

    public String getAnnotation() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(AcvContext.getInstance().selectedAnnotationTypeProperty.getValueSafe());
        buffer.append(":\n");

        for (String key: annotation.keySet()) {
            buffer.append("\u2014> ");
            buffer.append(key);

            if (annotation.get(key).equals(JsonNull.INSTANCE)) {
                buffer.append(":  null");
            } else {
                buffer.append(":  ");
                buffer.append(annotation.get(key).getAsString());
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }
}
