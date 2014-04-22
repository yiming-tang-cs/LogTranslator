package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Statistics {

    private static int changedLogMethodsCount;
    private static Map<String, Integer> loggerTypeCountMap = new HashMap<>();
    private static Set<LogFile> nonLogLogFiles = new HashSet<>();
    private static LogTranslatorNamespace LOG = Utils.getLogger();
    private static int counter = 0;
    private static long start;
    private static long stop;

    public static void addChangedLogMethodsCount() {
        changedLogMethodsCount++;
    }

    public static int getChangedLogMethodsCount() {
        return changedLogMethodsCount;
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

    /**
     * Add logFile to list of non-logFiles, just to see how much extra files we have
     * parsed via ANTLR for gathering of extra variables not found in 'current logFile'.
     * This is purely for statistics.
     *
     *  @param logFile containing no logs, but variable declarations or methods
     */
    public static void addNonLogLogFile(LogFile logFile) {
        nonLogLogFiles.add(logFile);
    }

    public static Set<LogFile> getNonLogLogFiles() {
        return nonLogLogFiles;
    }

    public static String publishRunInfo() {
        stop = System.currentTimeMillis();

        LOG.translationProcessFinishTime(((double) (stop - start) / 1000)).info();
        //        System.out.println("Finished in " + ((double) (stop - start) / 1000) + " seconds.");

        StringBuilder toPublish = new StringBuilder();
        toPublish.append(String.format("Changed %d log methods.%n", getChangedLogMethodsCount()));
        toPublish.append(String.format("\nProcessed %d of %d files. Extra files parsed by extending %d.%n%n",
            counter - nonLogLogFiles.size(), TranslatorStarter.logFiles.size(), nonLogLogFiles.size()));

        LOG.changedMethodsCount(getChangedLogMethodsCount());
        LOG.processed_log_and_extra_files(counter - nonLogLogFiles.size(), nonLogLogFiles.size()).debug();


        return toPublish.toString();
    }

    /**
     * Before exiting of processed java file by ANTLR,
     * raise counter by one.
     */
    public static void addProcessedFilesCounter() {
        counter++;
    }

    /** Start timing the run of application */
    public static long startTiming() {
        start = System.currentTimeMillis();
        return start;
    }
}
