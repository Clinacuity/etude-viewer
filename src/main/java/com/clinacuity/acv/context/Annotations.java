package com.clinacuity.acv.context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Annotations {
    private static final Logger logger = LogManager.getLogger();

    private Gson gson = new Gson();
    private JsonObject root;
    private Map<String, List<JsonObject>> annotationMap = new HashMap<>();
    public Map<String, List<JsonObject>> getAnnotationMap() { return annotationMap; }

    public Annotations(String filePath) {
        logger.debug("Loading Annotations from: {}", filePath);

        File f = new File(filePath);
        if (f.exists()) {
            try {
                JsonParser parser = new JsonParser();
                root = parser.parse(FileUtils.readFileToString(new File(filePath), "UTF-8")).getAsJsonObject();

                // TODO: do something we these
                root.get("offset_mapping").getAsJsonObject();
                loadAnnotations(root.get("annotations").getAsJsonObject());
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

                if (!annotationMap.keySet().contains(type)) {
                    annotationMap.put(type, new ArrayList<>());
                }
                annotationMap.get(type).add(childAnnotation);
            } catch (IllegalStateException e) {
                logger.warn("Entry is not a JsonObject", entry.getKey());
            }
        });
    }

    public Set<String> getAnnotationKeySet() { return annotationMap.keySet(); }

    public List<JsonObject> getAnnotationsByKey(String key) {
        return getAnnotationMap().get(key);
    }

    public String getRawText() {
        return root.get("raw_content").getAsString();
    }


}
