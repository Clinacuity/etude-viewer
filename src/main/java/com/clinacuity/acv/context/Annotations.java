package com.clinacuity.acv.context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;

public class Annotations {
    private static final Logger logger = LogManager.getLogger();

    private static final String METRICS_KEY = "metrics";
    private static final String METRICS_TYPES_KEY = "by-type";
    private static final String TRUE_POS_KEY = "TP";
    private static final String FALSE_POS_KEY = "FP";
    private static final String FALSE_NEG_KEY = "FN";
    private static final String CONTENT_KEY = "raw_content";
    private static final String OFFSETS_KEY = "offset_mapping";
    private static final String ANNOTATIONS_KEY = "annotations";
    private static final String ANNOTATION_TYPE_KEY = "type";

    private JsonObject root;
    private JsonObject metrics = null;
    private Map<String, List<JsonObject>> annotationMap = new HashMap<>();
    public Map<String, List<JsonObject>> getAnnotationMap() { return annotationMap; }
    public boolean hasOffsetMapping() { return root.has(OFFSETS_KEY); }
    public boolean hasMetrics() { return root.has(METRICS_KEY); }

    /**
     * Convenience object for managing a document's Json objects.
     * @param filePath      The path to the json file
     */
    public Annotations(String filePath) {
        logger.debug("Loading Annotations from: {}", filePath);

        File f = new File(filePath);
        if (f.exists()) {
            try {
                JsonParser parser = new JsonParser();
                root = parser.parse(FileUtils.readFileToString(f, "UTF-8")).getAsJsonObject();

                if (hasOffsetMapping()) {
                    root.get(OFFSETS_KEY).getAsJsonObject();
                }

                if (hasMetrics()) {
                    metrics = root.get(METRICS_KEY).getAsJsonObject();
                }

                loadAnnotations(root.get(ANNOTATIONS_KEY).getAsJsonObject());
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    private void loadAnnotations(JsonObject annotationRoot) {
        annotationRoot.entrySet().forEach(entry -> {
            try {
                JsonObject childAnnotation = entry.getValue().getAsJsonArray().get(0).getAsJsonObject();
                String type = childAnnotation.get(ANNOTATION_TYPE_KEY).getAsString();

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
        return root.get(CONTENT_KEY).getAsString();
    }

    /**
     * Returns the True Positive percentage for any given Annotation type.
     * @param annotationType    The Annotation whose metrics we are collecting
     * @return                  The value of the metric for the given Annotation
     */
    public double getMetricsTruePositive(String annotationType) {
        return getMetricsItem(annotationType, TRUE_POS_KEY);
    }

    /**
     * Returns the False Positive percentage for any given Annotation type
     * @param annotationType    The Annotation whose metrics we are collecting
     * @return                  The value of the metric for the given Annotation
     */
    public double getMetricsFalsePositive(String annotationType) {
        return getMetricsItem(annotationType, FALSE_POS_KEY);
    }

    /**
     * Returns the False Negative percentage for any given Annotation type
     * @param annotationType    The Annotation whose metrics we are collecting
     * @return                  The value of the metric for the given Annotation
     */
    public double getMetricsFalseNegative(String annotationType) {
        return getMetricsItem(annotationType, FALSE_NEG_KEY);
    }

//    /**
//     * True negatives are impossible to determine; this value should always be 0.0d
//     * @param annotationType    The Annotation whose metrics we are collecting
//     * @return                  The value of the metric for the given Annotation
//     */
//    public double getMetricsTrueNegative(String annotationType) {
//        return getMetricsItem(annotationType, TRUE_NEG_KEY);
//    }

    private double getMetricsItem(String annotationKey, String metricKey) {
        JsonObject json;

        if (metrics != null) {
            if (metrics.has(METRICS_TYPES_KEY)) {
                json = metrics.get(METRICS_TYPES_KEY).getAsJsonObject();
                if (json.has(annotationKey)) {
                    json = json.get(annotationKey).getAsJsonObject();
                    if (json.has(metricKey)) {
                        return json.get(metricKey).getAsDouble();
                    } else {
                        logger.warn("The metrics object has no information for value <{}> on key <{}>", metricKey, annotationKey);
                    }
                } else {
                    logger.warn("The metrics object has no information for key <{}>", annotationKey);
                    return 0.0d;
                }
            } else {
                logger.throwing(new JsonParseException("Key <" + METRICS_TYPES_KEY + "> does not exist!"));
                return 0.0d;
            }
        } else {
            logger.warn("There is no metrics object; key <{}> begin set to 0.0d.");
            return 0.0d;
        }

        return 0.0d;
    }
}
