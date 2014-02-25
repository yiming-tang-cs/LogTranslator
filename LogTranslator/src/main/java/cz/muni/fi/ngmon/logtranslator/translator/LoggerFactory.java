package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggerFactory {

    private static Map<String, List<String>> loggingFrameworks;
    private static String actualLoggingFramework = null;
    private static String obtainedImport = null;

    static {
        // <LogName, List<imports Factory+Loggers>> mapping
        // Factory is always on first position!
        loggingFrameworks = new HashMap<>();
        loggingFrameworks.put("juli", Arrays.asList(null, "java.util.logging.Logger")); // JULi has no factory (Logger is factory as well)
        loggingFrameworks.put("commons", Arrays.asList("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger"));
        loggingFrameworks.put("slf4j", Arrays.asList("org.slf4j.LoggerFactory", "org.slf4j.Logger"));
        loggingFrameworks.put("log4j", Arrays.asList(null, "org.apache.log4j.Logger")); // same situation as in  JULi (Logger.getLogger or .getRootLogger)
        loggingFrameworks.put("log4j2", Arrays.asList("org.apache.logging.log4j.LogManager", "org.apache.logging.log4j.Logger"));
        loggingFrameworks.put("custom", Arrays.asList("cz.muni.fi.ngmon.logtranslator.customlogger.LogFactory", "cz.muni.fi.ngmon.logtranslator.customlogger.Logger"));

    }

    public static String getObtainedImport() {
        return obtainedImport;
    }

    public static List<String> getActualLoggingImports() {
        return loggingFrameworks.get(actualLoggingFramework);
    }

    public static String getActualLoggingFramework() {
        return actualLoggingFramework;
    }

    public static void setActualLoggingFramework(String newLoggingFramework) {
        actualLoggingFramework = newLoggingFramework;
    }

    public static LoggerLoader determineLoggingFramework(String obtImport) {
        LoggerLoader loader;
        for (String key : loggingFrameworks.keySet()) {
            if (loggingFrameworks.get(key).contains(obtImport)) {
                setActualLoggingFramework(key);
                obtainedImport = obtImport;
                break;
            }
        }

        if (actualLoggingFramework == null) {
            // This obtImport does not contain any known "log import"
            // in our loggingFrameworks map, we can safely skip it.
            return null;
        } else {
            switch (actualLoggingFramework) {
                case "juli":
                    loader = new JULLogger(); break;
                case "commons":
                    loader = new CommonsLoggingLoader(); break;
                case "slf4j":
                    loader = new Slf4jLoggerLoader(); break;
                case "log4j":
                    loader = new Log4jLoggingLoader(); break;
                case "log4j2":
                    loader = new Log4jLoggingLoader(); break;
                case "custom":
                    loader = new CustomLoggerLoader(); break;
                default:
                    loader = null;
            }
        }
        return loader;
    }


}




