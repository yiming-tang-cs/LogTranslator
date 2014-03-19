package cz.muni.fi.ngmon.logtranslator.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Utils {

    public static boolean ignoreParsingErrors;
    public static List<String> MATH_OPERATORS = Arrays.asList("+", "-", "*", "/");
    public static List<String> BOOLEAN_OPERATORS = Arrays.asList("&&", "||");
    public static List<String> PRIMITIVE_TYPES = Arrays.asList("boolean", "byte", "int", "long", "double", "float", "char");
    static String applicationHome;
    static String applicationNamespace;
    static String ngmonLogImport;
    static String ngmonLogFactoryImport;
    static String ngmonLogGlobal;
    static String ngmonAnnotationNamespaceImport;
    static String ngmonLoggerAbstractNamespaceImport;
    static String ngmonEmptyLogStatement;
    static String ngmonLogLength;
    static List<String> BANNED_LIST = Arrays.asList("a", "an", "the");

//    static final String COMMA = ",";
//    static final String PLUS = "+";
//    static final Collection<String> separators = Arrays.asList(COMMA, PLUS);

    public static void initialize() {
        String propertyFile = "src/main/resources/logtranslator.properties";
        Properties properties = new Properties();

        try {
            InputStream is = new FileInputStream(propertyFile);
            properties.load(is);

            applicationHome = properties.getProperty("application_home");
            applicationNamespace = properties.getProperty("application_namespace");
            ngmonLogImport = properties.getProperty("ngmon_log_import");
            ngmonLogFactoryImport = properties.getProperty("ngmon_log_factory_import");
            ngmonLogGlobal = properties.getProperty("ngmon_log_global");
            ngmonAnnotationNamespaceImport = properties.getProperty("ngmon_annotation_ns_import");
            ngmonLoggerAbstractNamespaceImport = properties.getProperty("ngmon_logger_abstract_ns_import");
            ngmonEmptyLogStatement = properties.getProperty("ngmon_empty_log_statement");
            ngmonLogLength = properties.getProperty("ngmon_log_length", "7");
            ignoreParsingErrors = Boolean.parseBoolean(properties.getProperty("ignoreParsingErrors"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // TODO which methods are necessary to be abstract?
    public static String getApplicationHome() {
        return applicationHome;
    }

//    public static void setLoggingApplicationHome(String newLoggingApplicationHome) {
//        applicationHome = newLoggingApplicationHome;
//    }

    public static int getNgmonLogLength() {
        return Integer.parseInt(ngmonLogLength);
    }

//    public String getNgmonEmptyLogStatement() {
//        return ngmonEmptyLogStatement;
//    }
//
//    public String getNgmonLoggerAbstractNamespaceImport() {
//        return ngmonLoggerAbstractNamespaceImport;
//    }
//
//    public String getNgmonAnnotationNamespaceImport() {
//        return ngmonAnnotationNamespaceImport;
//    }

    public static String getNgmonLogFactoryImport() {
        return ngmonLogFactoryImport;
    }

    public static String getNgmonLogImport() {
        return ngmonLogImport;
    }

    public static String getNgmonLogGlobal() {
        return ngmonLogGlobal;
    }

    //
    public static String getApplicationNamespace() {
        return applicationNamespace;
    }
//
//    public void setLoggingApplicationNamespace(String newLoggingApplicationNamespace) {
//        applicationNamespace = newLoggingApplicationNamespace;
//    }


    public static String getQualifiedNameEnd(String str) {
        return str.substring(str.lastIndexOf(".") + 1);
    }


    public static List<LogFile> generateNamespaces(List<LogFile> logFileList) {
        // TODO
        Set<String> namespaceList = new LinkedHashSet<>();

        for (LogFile lf : logFileList) {
//            System.out.println("packageName=" + lf.getPackageName());
            if (lf.getPackageName() == null) {
                System.out.println("null packageName in file " + lf.getFilepath());
            }
            namespaceList.add(lf.getPackageName());
//            System.out.println(lf.getNamespace());
            //set default namespace for now
            lf.setNamespace(applicationNamespace);
        }
        System.out.println();
        return logFileList;
    }


    public static boolean listContainsItem(List<String> list, String text) {
        for (String item : list) {
            if (text.contains(item)) {
//                System.out.println("TEXT=" + text + " item=" + item);
                return true;
            }
        }
        return false;
    }

    public static boolean listEqualsItem(List<String> list, String text) {
        for (String item : list) {
            if (item.equals(text)) {
                return true;
            }
        }
        return false;
    }
}
