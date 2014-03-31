package org.ngmon.logger.logtranslator.common;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

    private static int changedLogMethodsCount;
    private static Map<String, Integer> loggerTypeCountMap = new HashMap<>();

    public static void addChangedLogMethodsCount() {
        changedLogMethodsCount++;
    }

    public static void addLoggerTypeCount(String loggerType) {
        int actCount;
        if (loggerTypeCountMap.get(loggerType) == null) {
            actCount = 0;
        } else {
            actCount = loggerTypeCountMap.get(loggerType);
        }
        loggerTypeCountMap.put(loggerType, actCount);
    }

    public static void reportLoaderType() {
        // add +1 to map of Stats<key, count>
    }
}
