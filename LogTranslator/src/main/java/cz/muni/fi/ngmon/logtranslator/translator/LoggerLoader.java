package cz.muni.fi.ngmon.logtranslator.translator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class LoggerLoader {

    private String testingFile;
    private Properties properties;
    private String loggingApplicationHome;
    private String loggingApplicationNamespace;
    private String loggingFw;

    private String logger;
    private String logFactory;
    private String ngmonLogImport;
    private String ngmonLogFactoryImport;
    private String ngmonAnnotationNamespaceImport;
    private String ngmonLoggerAbstractNamespaceImport;
    private String ngmonEmptyLogStatement;

    public LoggerLoader() {
        String propertyFile = "src/main/resources/logtranslator.properties";
        try {
            InputStream is = new FileInputStream(propertyFile);
            properties = new Properties();
            properties.load(is);
            loggingFw = properties.getProperty("application_log_system");
            loggingApplicationHome = properties.getProperty("application_home");
            testingFile = properties.getProperty("example_file");
            ngmonLogImport = properties.getProperty("ngmon_log_import");
            ngmonLogFactoryImport = properties.getProperty("ngmon_log_factory_import");
            ngmonAnnotationNamespaceImport = properties.getProperty("ngmon_annotation_ns_import");
            ngmonLoggerAbstractNamespaceImport = properties.getProperty("ngmon_logger_abstract_ns_import");
            ngmonEmptyLogStatement = properties.getProperty("ngmon_empty_log_statement");
            loggingApplicationNamespace = properties.getProperty("application_namespace");

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLoggingFw() {
        return loggingFw;
    }

    public String getLoggingApplicationHome() {
        return loggingApplicationHome;
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

    public String getLogFactory() {
        return logFactory;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public void setLogFactory(String logFactory) {
        this.logFactory = logFactory;
    }

    public String getNgmonEmptyLogStatement() {
        return ngmonEmptyLogStatement;
    }

    public void setNgmonEmptyLogStatement(String ngmonEmptyLogStatement) {
        this.ngmonEmptyLogStatement = ngmonEmptyLogStatement;
    }

    public String getNgmonLoggerAbstractNamespaceImport() {
        return ngmonLoggerAbstractNamespaceImport;
    }

    public void setNgmonLoggerAbstractNamespaceImport(String ngmonLoggerAbstractNamespaceImport) {
        this.ngmonLoggerAbstractNamespaceImport = ngmonLoggerAbstractNamespaceImport;
    }

    public String getNgmonAnnotationNamespaceImport() {
        return ngmonAnnotationNamespaceImport;
    }

    public void setNgmonAnnotationNamespaceImport(String ngmonAnnotationNamespaceImport) {
        this.ngmonAnnotationNamespaceImport = ngmonAnnotationNamespaceImport;
    }

    public String getNgmonLogFactoryImport() {
        return ngmonLogFactoryImport;
    }

    public void setNgmonLogFactoryImport(String ngmonLogFactoryImport) {
        this.ngmonLogFactoryImport = ngmonLogFactoryImport;
    }

    public String getNgmonLogImport() {
        return ngmonLogImport;
    }

    public void setNgmonLogImport(String ngmonLogImport) {
        this.ngmonLogImport = ngmonLogImport;
    }

    public void setLoggingFw(String loggingFw) {
        this.loggingFw = loggingFw;
    }

    public String getLoggingApplicationNamespace() {
        return loggingApplicationNamespace;
    }

    public String getLoggingApplicationNamespaceShort() {
        return loggingApplicationNamespace.substring(loggingApplicationNamespace.lastIndexOf(".")+1);
    }

    public void setLoggingApplicationNamespace(String loggingApplicationNamespace) {
        this.loggingApplicationNamespace = loggingApplicationNamespace;
    }

    public void setLoggingApplicationHome(String loggingApplicationHome) {
        this.loggingApplicationHome = loggingApplicationHome;
    }
}
