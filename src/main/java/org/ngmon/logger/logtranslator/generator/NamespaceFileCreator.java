package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/*
 package log_events.org.apache.hadoop.hdfs.nfs;

 import cz.muni.fi.annotation.Namespace;
 import cz.muni.fi.logger.AbstractNamespace;

 @Namespace
 public class NfsNamespace extends AbstractNamespace {

     public AbstractNamespace MOUNT_MNT_PATH_CLIENT(String path, String client) {
        return log(path, client);
     }

     public AbstractNamespace FAIL_PENDING_WRITE_NEXTOFFSET(String key_getMin, String key_getMax, String getNextOffsetUnprotected) {
        return log(key_getMin, key_getMax, getNextOffsetUnprotected);
     }
 }
*/

// https://theantlrguy.atlassian.net/wiki/display/ST4/StringTemplate+4+Documentation
public class NamespaceFileCreator {

    private Set<LogFile> logFileList = new HashSet<>();
    private Set<String> methods = new TreeSet<>();
    private String namespaceClassName;
    private String namespace;
    private ST namespaceFileContent;
    private String filePath;

    public NamespaceFileCreator(LogFile logFile) {
        namespaceClassName = logFile.getNamespaceClass();
        namespace = Utils.getApplicationNamespace();
        namespaceFileContent = prepareNewNamespace(logFile);

        System.out.println("namespace=" + logFile.getNamespace() + " nsClass=" + logFile.getNamespaceClass());
        System.out.println(namespaceFileContent.render());
    }

    public ST getNamespaceFileContent() {
        return namespaceFileContent;
    }

    public Set<LogFile> getLogFileList() {
        return logFileList;
    }

    public void addLogFileList(LogFile logFile) {
        this.logFileList.add(logFile);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Create NGMON namespace with proper imports, class declaration and log methods.
     *
     * @param logFile namespace is create from this logFile.
     * @return StringTemplate with ready-to-create file
     */
    private ST prepareNewNamespace(LogFile logFile) {
        String NAMESPACE_JAVA_CLASS_STRING_TEMPLATE =
                "package log_events.<applicationNamespace>;\n\n"
//                        + "import <namespaceImport>;\n"
                        + "import <abstractNamespaceImport>;\n\n"
//                        + "<namespaceAnnotation>\n"
                        + "public class <namespaceClassName> extends AbstractNamespace {\n\n"
                        + "    <methods>"
                        + "}\n";
        ST template = new ST(NAMESPACE_JAVA_CLASS_STRING_TEMPLATE);
        template.add("applicationNamespace", namespace);
//        template.add("namespaceImport", Utils.getNgmonAnnotationNamespaceImport());
        template.add("abstractNamespaceImport", Utils.getNgmonLoggerAbstractNamespaceImport());
//        template.add("namespaceAnnotation", Utils.ngmonAnnotationNamespace());
        template.add("namespaceClassName", namespaceClassName);

        // generate methods from this logFile
        for (Log log : logFile.getLogs()) {
            String newNgmonLog = prettyPrintMethod(log);
            methods.add(newNgmonLog);
            log.setGeneratedNgmonLog(newNgmonLog);
        }
        template.add("methods", methods);
        return template;
    }

    /**
     * Create suitable method for this namespace from Log object using StringTemplate.
     *
     * @param log to create method from
     * @return method suitable for
     */
    private String prettyPrintMethod(Log log) {
        ST methodTemplate = new ST(
                "public AbstractNamespace <methodName>(<formalParameters>) {\n" +
//                        "    return log(<parameterNames>);\n" +
                        "    return this;\n" +
                        "}\n\n");

        methodTemplate.add("methodName", log.getMethodName());
        StringBuilder formalParameters = new StringBuilder();
        StringBuilder parameterNames = new StringBuilder();

        int i = 0;
        for (LogFile.Variable var : log.getVariables()) {
            if (i != 0) {
                formalParameters.append(", ");
                parameterNames.append(", ");
            }

            formalParameters.append(var.getType()).append(" ");
            if (var.getNgmonName() != null) {
                formalParameters.append(var.getNgmonName());
//                parameterNames.append(var.getNgmonName());
            } else {
                formalParameters.append(var.getName());
//                parameterNames.append(var.getName());
            }
            i++;
        }
        //formalParameters.delete(formalParameters.length() - 2, formalParameters.length());
        methodTemplate.add("formalParameters", formalParameters);
//        methodTemplate.add("parameterNames", parameterNames);
        return methodTemplate.render();
    }

    /**
     * Append new methods from LogFile object to current methods in namespaceFileContent.
     *
     * @param logFile contains Logs with possible new methods.
     */
    protected void addMethodsToNamespace(LogFile logFile) {
        // TODO append to methodList and regenerate whole file content, instead of looking up particular
        // method and inserting it in to appropriate position (?)  TreeSet
//        methods.add()
        if (this.getLogFileList().contains(logFile)) {
            System.err.println("Nothing new should be added to this namespace Log");
            System.exit(100);
        }
        for (Log log : logFile.getLogs()) {
            String newMethod = prettyPrintMethod(log);
            if (!methods.contains(newMethod)) {
                methods.add(newMethod);
            }
        }
        namespaceFileContent.remove("methods");
        namespaceFileContent.add("methods", methods);
    }

}
