package com.clinacuity.acv.context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CorpusDictionary {
    private static final Logger logger = LogManager.getLogger();
    private static final String METRICS_KEY = "metrics";
    private static final String FILE_MAPPING_KEY = "file-mapping";
    private static final String ARGUMENTS_KEY = "args";
    private static final String TRUE_POSITIVE_KEY = "TP";
    private static final String FALSE_POSITIVE_KEY = "FP";
    private static final String FALSE_NEGATIVE_KEY = "FN";
    private static final String TRUE_NEGATIVE_KEY = "TN";
    private static final String BY_TYPE_KEY = "by-type";
    private static final String MICRO_AVERAGE_KEY = "micro-average";

    private JsonObject root;
    private JsonObject metrics;
    private Map<String, String> fileMappings = new HashMap<>();
    private JsonObject arguments;

    public CorpusDictionary(String path) {
        logger.debug("Loading Corpus dictionary from {}:", path);

        File f = new File(path);
        if (f.exists() && f.isFile() && !f.isHidden()) {
            try {
                JsonParser parser = new JsonParser();

                root = parser.parse(FileUtils.readFileToString(f, "UTF-8")).getAsJsonObject();
                initialize();
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    private void initialize() {
        populateFileMappings();
        populateArguments();
        populateMetricsObject();
    }

    private void populateFileMappings() {
        if (root.has(FILE_MAPPING_KEY)) {
            JsonObject maps = root.get(FILE_MAPPING_KEY).getAsJsonObject();
            maps.keySet().forEach(key -> fileMappings.put(key, maps.get(key).getAsString()));
        } else {
            logger.throwing(new JsonParseException("Key <" + FILE_MAPPING_KEY + "> does not exist!"));
        }
    }

    private void populateArguments() {
        if (root.has(ARGUMENTS_KEY)) {
            arguments = root.get(ARGUMENTS_KEY).getAsJsonObject();
        } else {
            logger.warn("Corpus has no arguments");
            arguments = null;
        }
    }

    private void populateMetricsObject() {
        if (root.has(METRICS_KEY)) {
            metrics = root.get(METRICS_KEY).getAsJsonObject();
        } else {
            logger.throwing(new JsonParseException(""));
        }
    }

    public Set<String> getMetricsMatchTypes() {
        return metrics.keySet();
    }

    public Set<String> getMetricsAnnotationTypes(String matchType) {
        return metrics.get(matchType).getAsJsonObject().get(BY_TYPE_KEY).getAsJsonObject().keySet();
    }

    public MetricValues getMetricAnnotationTypeValues(String matchType, String annotationType) {
        if (metrics.has(matchType)) {
            JsonObject values = metrics.get(matchType).getAsJsonObject().get(BY_TYPE_KEY).getAsJsonObject();
            values = values.get(annotationType).getAsJsonObject();
            double tp = values.get(TRUE_POSITIVE_KEY).getAsDouble();
            double fp = values.get(FALSE_POSITIVE_KEY).getAsDouble();
            double fn = values.get(FALSE_NEGATIVE_KEY).getAsDouble();
            double tn = values.get(TRUE_NEGATIVE_KEY).getAsDouble();

            return new MetricValues(tp, fp, fn, tn);
        }

        logger.warn("The Metrics dictionary does not contain metrics for type {} under the {} category",
                annotationType, matchType);
        return new MetricValues(0d, 0d, 0d);
    }

    public MetricValues getMetricsMicroAverage(String matchType) {
        if (metrics.has(matchType) && metrics.get(matchType).getAsJsonObject().has(MICRO_AVERAGE_KEY)) {
            JsonObject values = metrics.get(matchType).getAsJsonObject().get(MICRO_AVERAGE_KEY).getAsJsonObject();
            double tp = values.get(TRUE_POSITIVE_KEY).getAsDouble();
            double fp = values.get(FALSE_POSITIVE_KEY).getAsDouble();
            double fn = values.get(FALSE_NEGATIVE_KEY).getAsDouble();
            double tn = values.get(TRUE_NEGATIVE_KEY).getAsDouble();

            return new MetricValues(tp, fp, fn, tn);
        }


        logger.warn("The Metrics dictionary does not contain micro-average metrics under the {} category", matchType);
        return new MetricValues(0d, 0d, 0d);
    }

    public Set<String> getArgumentKeys() {
        return arguments.keySet();
    }

    public String getArgument(String key) {
        if (arguments.has(key)) {
            return arguments.get(key).getAsString();
        } else {
            logger.throwing(new JsonParseException("Argument does not contain key <" + key + ">"));
            return null;
        }
    }

    public Map<String, String> getFileMappings() { return fileMappings; }
}
