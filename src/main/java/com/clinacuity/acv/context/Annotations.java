package com.clinacuity.acv.context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Annotations {
    private static final Logger logger = LogManager.getLogger();

    private Gson gson = new Gson();
    private JsonObject root;
    private JsonObject metadata;
    private Map<String, List<JsonObject>> annotations = new HashMap<>();

    public Annotations(String filePath) {
        File f = new File(filePath);

        if (f.exists()) {
            try {
                JsonParser parser = new JsonParser();
                root = parser.parse(FileUtils.readFileToString(new File(filePath), "UTF-8")).getAsJsonObject();
                metadata = root.get("metadata").getAsJsonObject();
                loadAnnotations(root.get("annotations").getAsJsonObject());
                notifyAnnotations();
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    private void loadAnnotations(JsonObject annotationRoot) {
        annotationRoot.entrySet().forEach(entry -> {
            try {
                JsonObject childAnnotation = entry.getValue().getAsJsonArray().get(0).getAsJsonObject();
                String type = childAnnotation.get("type").getAsString();

                if (!annotations.keySet().contains(type)) {
                    annotations.put(type, new ArrayList<>());
                }
                annotations.get(type).add(childAnnotation);
            } catch (IllegalStateException e) {
                logger.warn("Entry is not a JsonObject", entry.getKey());
            }
        });
    }

    public void notifyAnnotations() {
        annotations.keySet().forEach(key -> AcvContext.getInstance().annotationList.add(key));
    }

    public int getSelectedAnnotationCount() {
        String key = AcvContext.getInstance().selectedAnnotationProperty.getValueSafe();
        return annotations.keySet().contains(key) ? annotations.get(key).size() : -1;
    }

    public String getRawText() {
        if (metadata != null && metadata.keySet().contains("raw_text")) {
            return  metadata.get("raw_text").getAsString();
        } else {
            return "";
        }
    }
}
