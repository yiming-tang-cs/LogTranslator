package cz.muni.fi.ngmon.logtranslator.translator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public abstract class LoggerLoader {

    private List<String> availableLogMethods;

    private String testingFile;
    private Properties properties;
    private String loggingApplicationHome;
    private String loggingApplicationNamespace;
    private String loggingFw;

    private String logger;
    private String logFactory;
    private String ngmonLogImport;
    private String ngmonLogFactoryImport;
    private String ngmonLogGlobal;
    private String ngmonAnnotationNamespaceImport;
    private String ngmonLoggerAbstractNamespaceImport;
    private String ngmonEmptyLogStatement;

    public LoggerLoader() {
        String propertyFile = "src/main/resources/logtranslator.properties";
        try {
            InputStream is = new FileInputStream(propertyFile);
            properties = new Properties();
            properties.load(is);

            testingFile = properties.getProperty("example_file");

            loggingFw = properties.getProperty("application_log_system");
            loggingApplicationHome = properties.getProperty("application_home");
            ngmonLogImport = properties.getProperty("ngmon_log_import");
            ngmonLogFactoryImport = properties.getProperty("ngmon_log_factory_import");
            ngmonLogGlobal = properties.getProperty("ngmon_log_global");
            ngmonAnnotationNamespaceImport = properties.getProperty("ngmon_annotation_ns_import");
            ngmonLoggerAbstractNamespaceImport = properties.getProperty("ngmon_logger_abstract_ns_import");
            ngmonEmptyLogStatement = properties.getProperty("ngmon_empty_log_statement");
            loggingApplicationNamespace = properties.getProperty("application_namespace");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO which methods are necessary to be abstract?
    public String getLoggingFw() {
        return loggingFw;
    }

    public void setLoggingFw(String loggingFw) {
        this.loggingFw = loggingFw;
    }

    public String getLoggingApplicationHome() {
        return loggingApplicationHome;
    }

    public void setLoggingApplicationHome(String loggingApplicationHome) {
        this.loggingApplicationHome = loggingApplicationHome;
    }

    public String getTestingFile() {
        return testingFile;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLogFactory() {
        return logFactory;
    }

    public void setLogFactory(String logFactory) {
        this.logFactory = logFactory;
    }

    // --- ^^^ ---


    public String getNgmonEmptyLogStatement() {
        return ngmonEmptyLogStatement;
    }

    public String getNgmonLoggerAbstractNamespaceImport() {
        return ngmonLoggerAbstractNamespaceImport;
    }

    public String getNgmonAnnotationNamespaceImport() {
        return ngmonAnnotationNamespaceImport;
    }

    public String getNgmonLogFactoryImport() {
        return ngmonLogFactoryImport;
    }

    public String getNgmonLogImport() {
        return ngmonLogImport;
    }

    public String getNgmonLogGlobal() {
        return ngmonLogGlobal;
    }

    public String getLoggingApplicationNamespace() {
        return loggingApplicationNamespace;
    }

    public void setLoggingApplicationNamespace(String loggingApplicationNamespace) {
        this.loggingApplicationNamespace = loggingApplicationNamespace;
    }

    public String getQualifiedNameEnd(String str) {
        return str.substring(str.lastIndexOf(".") + 1);
    }

    public abstract List<String> getAvailableLogMethods();
}
