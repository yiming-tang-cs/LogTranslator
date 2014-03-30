package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.translator.LoggerLoader;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

    private static int changedLogMethodsCount;
    private static Map<String, Integer> loggerTypeCountMap = new HashMap<>();

    public static void addChangedLogMethodsCount() {
        changedLogMethodsCount++;
    }

    public static void addLoggerTypeCount(String loggerType) {
        int actCount = loggerTypeCountMap.get(loggerType);
        loggerTypeCountMap.put(loggerType, actCount);
    }

    public static void reportLoaderType(LoggerLoader loggerLoader) {
        // add +1 to map of Stats<key, count>
    }
}
