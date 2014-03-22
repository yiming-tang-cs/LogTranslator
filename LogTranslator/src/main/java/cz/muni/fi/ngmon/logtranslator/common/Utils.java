package cz.muni.fi.ngmon.logtranslator.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class Utils {

    public final static List<String> MATH_OPERATORS = Arrays.asList("+", "-", "*", "/");
    public final static List<String> BOOLEAN_OPERATORS = Arrays.asList("&&", "||");
    public final static List<String> PRIMITIVE_TYPES = Arrays.asList("boolean", "byte", "int", "long", "double", "float", "char");
    public static final String NEGATION = "!";
    static final List<String> BANNED_LIST = Arrays.asList("a", "an", "the");
    public static boolean ignoreParsingErrors;
    static String applicationHome;
    static String applicationNamespace;
    static String ngmonLogImport;
    static String ngmonLogFactoryImport;
    static String ngmonLogGlobal;
    static String ngmonAnnotationNamespaceImport;
    static String ngmonLoggerAbstractNamespaceImport;
    static String ngmonEmptyLogStatement;
    static String ngmonLogLength;
    private static int applicationNamespaceLength;

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
            int tmpLength = Integer.parseInt(properties.getProperty("application_namespace_length"));
            applicationNamespaceLength = (tmpLength == 0) ? (applicationNamespace.length() + 2) : tmpLength;
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


    // TODO Decide which methods are necessary to be abstract?
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


    /**
     * Generate few namespaces for this logging application. Resolve number of namespaces
     * for this app based on applicationNamespaceLength property and set them to LogFiles.
     *
     * @param logFileList input list of logFiles, which contain only filepath and package qualified name.
     * @return same list of logFiles, but each of them has filled appropriate namespace.
     */
    public static List<LogFile> generateNamespaces(List<LogFile> logFileList) {
        Set<String> namespaceSet = new TreeSet<>();
        System.out.println("appnamespaceLength=" + applicationNamespaceLength);
        for (LogFile lf : logFileList) {
//            System.out.println("packageName=" + lf.getPackageName());
            if (lf.getPackageName() == null) {
                System.err.println("null packageName in file " + lf.getFilepath());
            }
            String namespace = createNamespace(lf.getPackageName());
            namespaceSet.add(namespace);
            lf.setNamespace(namespace);
        }

        StringBuilder sb = new StringBuilder();
        for (String s : namespaceSet) {
            sb.append(s).append("\n");
        }
        System.out.println("namespaceSet=" + sb.toString());
        return logFileList;
    }

    /**
     * Create ngmon log namespace which will contain all calls for this logs.
     * This method sets granularity level of ngmon log messages.
     * If original packageName length is longer then applicationNamespaceLength
     * property, make it shorter.
     *
     * @param packageName string to change
     * @return shortened packageName from ngmon length rules
     */
    private static String createNamespace(String packageName) {
        int numberOfDots = packageName.length() - packageName.replace(".", "").length();

        if (numberOfDots < applicationNamespaceLength) {
            return packageName;
        } else {
            StringBuilder newPackageName = new StringBuilder();
            String[] pckgs = packageName.split("\\.", applicationNamespaceLength + 1);
            pckgs[pckgs.length - 1] = "";
            for (String p : pckgs) {
                if (!p.equals("")) newPackageName.append(p).append(".");
            }
            // remove last extra dot
            newPackageName.deleteCharAt(newPackageName.length() - 1);
            return newPackageName.toString();
        }
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
