package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.core.LoggerFactory;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.ngmon.logger.logtranslator.ngmonLogging.SimpleLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Class reads from logtranslator.properties file and sets accordingly all settings
 * as specified in file.
 * Class contains some useful methods as well.
 */
public class Utils {

    public final static List<String> NGMON_ALLOWED_TYPES = Arrays.asList("string", "boolean", "byte", "int", "long", "double", "float", "char");
    //    public static List<String> NGMON_ALLOWED_OBJECT_TYPES = Arrays.asList("String", "Boolean", "Byte", "Integer", "Long", "Double", "Float", "Character");
    public final static List<String> MATH_OPERATORS = Arrays.asList("+", "-", "*", "/");
    public final static List<String> BOOLEAN_OPERATORS = Arrays.asList("&&", "||", "==", "!=", "<", ">");
    public final static List<String> PRIMITIVE_TYPES = Arrays.asList("boolean", "byte", "int", "long", "double", "float", "char");
    public static final String NEGATION = "!";
    public static final List<String> BANNED_LIST = Arrays.asList("a", "an", "the");
    public static final List<String> COLLECTION_LIST = Arrays.asList("Collection", "List", "Map", "Set", "SortedSet", "Queue");
    public static final List<String> JAVA_KEYWORDS = Arrays.asList(
        "abstract", "continue", "for", "new", "switch",
        "assert", "default", "goto", "package", "synchronized",
        "boolean", "do", "if", "private", "this",
        "break", "double", "implements", "protected", "throw",
        "byte", "else", "import", "public", "throws",
        "case", "enum", "instanceof", "return", "transient",
        "catch", "extends", "int", "short", "try",
        "char", "final", "interface", "static", "void",
        "class", "finally", "long", "strictfp", "volatile",
        "const", "float", "native", "super", "while");
    public static final List<String> JAVA_ESCAPE_CHARS = Arrays.asList("\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\\'", "\\\\");
    public static final List<String> DEFAULT_LOG_LEVELS = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal", "log");
    public static List<String> FORMATTERS = Arrays.asList("String.format", "MessageFormatter.format", "StringUtils", "Formatter.format", "print", "formatMessage", "{}", "%");
    public static final String sep = File.separator;
    private static final LogTranslatorNamespace NgmonLogger = LoggerFactory.getLogger(LogTranslatorNamespace.class, new SimpleLogger());
    public static boolean ignoreParsingErrors;
    public static boolean goMatchDebug;
    public static boolean goMatchWorkaround;
    public static String goMatchLocation;
    public static String debugOutputLocation;
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
    private static String ngmonDefaultNamespaceEnd;
    private static boolean ngmonPrimitiveTypesOnly;
    private static String logTranslatorGeneratedProject;

    // list of old & new generated log
    private static StringBuilder oldNewLogList = new StringBuilder();


    public static String getLogTranslatorGeneratedProject() {
        return logTranslatorGeneratedProject;
    }

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
            ngmonDefaultNamespaceEnd = properties.getProperty("ngmon_default_namespace_end", "Namespace");
            ngmonEmptyLogStatement = properties.getProperty("ngmon_empty_log_statement");
            ngmonEmptyLogStatementMethodNameLength = Integer.valueOf(properties.getProperty("ngmon_empty_log_method_name_length", "8"));
            ngmonLogLength = properties.getProperty("ngmon_log_length", "7");
            ignoreParsingErrors = Boolean.parseBoolean(properties.getProperty("ignoreParsingErrors"));
            ngmonPrimitiveTypesOnly = Boolean.parseBoolean(properties.getProperty("generate_primitive_types_only"));
            goMatchDebug = Boolean.parseBoolean(properties.getProperty("gomatch_debug_mode", "false"));
            goMatchWorkaround = Boolean.parseBoolean(properties.getProperty("gomatch_workaround", "false"));
            goMatchLocation = properties.getProperty("gomatch_generated_files", "generated/go-match.patterns");
            debugOutputLocation = properties.getProperty("debug_output_location", "generated/ngmonold-newfiles");
            logTranslatorGeneratedProject = applicationHome + sep + properties.getProperty("generated_project_name", "logtranslator") + sep;
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

    public static String getNgmonDefaultNamespaceEnd() {
        return ngmonDefaultNamespaceEnd;
    }

    public static boolean isNgmonPrimitiveTypesOnly() {
        return ngmonPrimitiveTypesOnly;
    }

    /**
     * Look for an existence of item from list in given text.
     *
     * @param list get various options from this list
     * @param text test for existence of item from list in this text
     * @return if item is found, return item, else return null
     */
    public static String listContainsItem(List<String> list, String text) {
        for (String item : list) {
            if (text.contains(item)) {
                return item;
            }
        }
        return null;
    }

    public static boolean itemInList(List<String> list, String text) {
        for (String item : list) {
            if (item.equals(text)) {
                return true;
            }
        }
        return false;
    }

    public static int countOfSymbolInText(String text, String symbol) {
        text = text.replaceAll(" ", "");
        return (text.length() - text.replace(symbol, "").length()) / symbol.length();
    }

    public static String getOldNewLogList(List<LogFile> logFiles) {
        for (LogFile logfs : logFiles) {
            for (Log log : logfs.getLogs()) {
                // TODO DEBUG()!
                String logs = log.getOriginalLog() + "\n"
                    + log.getGeneratedReplacementLog() + "\n"
                    + log.getGeneratedNgmonLog().substring(0, log.getGeneratedNgmonLog().indexOf("{") - 1) + "\n"
                    + log.getGoMatchLog() + "\n\n\n";
                oldNewLogList.append(logs);
            }
        }

        return oldNewLogList.toString();
    }
}
