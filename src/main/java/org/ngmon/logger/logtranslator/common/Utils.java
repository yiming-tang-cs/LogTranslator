package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.core.LoggerFactory;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.ngmon.logger.logtranslator.ngmonLogging.SimpleLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Utils {

    private static final LogTranslatorNamespace NgmonLogger = LoggerFactory.getLogger(LogTranslatorNamespace.class, new SimpleLogger());

    public final static List<String> MATH_OPERATORS = Arrays.asList("+", "-", "*", "/");
    public final static List<String> BOOLEAN_OPERATORS = Arrays.asList("&&", "||");
    public final static List<String> PRIMITIVE_TYPES = Arrays.asList("boolean", "byte", "int", "long", "double", "float", "char");
    public static final String NEGATION = "!";
    public static final List<String> BANNED_LIST = Arrays.asList("a", "an", "the");
    public static boolean ignoreParsingErrors;
    static String applicationHome;
    static String applicationNamespace;
    static String ngmonLogImport;
    static String ngmonLogFactoryImport;
    static String ngmonLogGlobal;
    static String ngmonLoggerAbstractNamespaceImport;
    static String ngmonEmptyLogStatement;
    static String ngmonLogLength;
    static int applicationNamespaceLength;
    private static int ngmonEmptyLogStatementMethodNameLength;
    private static String ngmongLogEventsImportPrefix;
    private static String ngmonSimpleLoggerImport;
    private static String ngmonJsonerImport;

    public static void initialize() {
        String propertyFile = "src/main/resources/logtranslator.properties";
        Properties properties = new Properties();

        try {
            InputStream is = new FileInputStream(propertyFile);
            properties.load(is);

            applicationHome = properties.getProperty("application_home");
            applicationNamespace = properties.getProperty("application_namespace");
            int tmpLength = Integer.parseInt(properties.getProperty("application_namespace_length"));
            applicationNamespaceLength = (tmpLength == 0) ? (applicationNamespace.length() + 2) : tmpLength;
            ngmongLogEventsImportPrefix = properties.getProperty("ngmon_log_events_import_prefix", "log_events");
            ngmonLogImport = properties.getProperty("ngmon_log_import");
            ngmonLogFactoryImport = properties.getProperty("ngmon_log_factory_import");
            ngmonSimpleLoggerImport = properties.getProperty("ngmon_simple_logger_import");
            ngmonLogGlobal = properties.getProperty("ngmon_log_global");
            ngmonJsonerImport = properties.getProperty("ngmon_jsoner_import");
            ngmonLoggerAbstractNamespaceImport = properties.getProperty("ngmon_logger_abstract_ns_import");
            ngmonEmptyLogStatement = properties.getProperty("ngmon_empty_log_statement");
            ngmonEmptyLogStatementMethodNameLength = Integer.valueOf(properties.getProperty("ngmon_empty_log_method_name_length", "8"));
            ngmonLogLength = properties.getProperty("ngmon_log_length", "7");
            ignoreParsingErrors = Boolean.parseBoolean(properties.getProperty("ignoreParsingErrors"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static LogTranslatorNamespace getLogger() {
        return NgmonLogger;
    }

    public static String getApplicationHome() {
        return applicationHome;
    }

    public static String getNgmonSimpleLoggerImport() {
        return ngmonSimpleLoggerImport;
    }

    public static String getNgmongLogEventsImportPrefix() {
        return ngmongLogEventsImportPrefix;
    }

    public static int getNgmonLogLength() {
        return Integer.parseInt(ngmonLogLength);
    }

    public static String getNgmonEmptyLogStatement() {
        return ngmonEmptyLogStatement;
    }

    public static int getNgmonEmptyLogStatementMethodNameLength() {
        return ngmonEmptyLogStatementMethodNameLength;
    }

    public static String getNgmonLoggerAbstractNamespaceImport() {
        return ngmonLoggerAbstractNamespaceImport;
    }

    public static String getNgmonLogFactoryImport() {
        return ngmonLogFactoryImport;
    }

    public static String getNgmonJsonerImport() {
        return ngmonJsonerImport;
    }

    public static String getNgmonLogImport() {
        return ngmonLogImport;
    }

    public static String getNgmonLogGlobal() {
        return ngmonLogGlobal;
    }

    public static String getApplicationNamespace() {
        return applicationNamespace;
    }

    public static int getApplicationNamespaceLength() {
        return applicationNamespaceLength;
    }

    public static String getQualifiedNameEnd(String str) {
        return str.substring(str.lastIndexOf(".") + 1);
    }


    public static boolean listContainsItem(List<String> list, String text) {
        for (String item : list) {
            if (text.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean itemInList(List<String> list, String text) {
        for (String item : list) {
            if (item.equals(text)) {
                return true;
            }
        }
        return false;
    }

    public static int numberOfDotsInText(String text) {
        return text.length() - text.replace(".", "").length();
    }

}
