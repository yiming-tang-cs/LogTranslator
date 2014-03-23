package cz.muni.fi.ngmon.logtranslator.generator;

import cz.muni.fi.ngmon.logtranslator.common.Log;
import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/*
 package org.apache.hadoop.hdfs.nfs;

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
    private ST ngmonFileContent;
    private String filePath;


    public NamespaceFileCreator(LogFile logFile) {
        namespaceClassName = logFile.getNamespaceClass();
        namespace = logFile.getNamespace();
        ngmonFileContent = prepareNewNamespace(logFile);

        System.out.println("namespace=" + logFile.getNamespace() + " nsClass=" + logFile.getNamespaceClass());
        System.out.println(ngmonFileContent.render());
    }

    public ST getNgmonFileContent() {
        return ngmonFileContent;
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

    private ST prepareNewNamespace(LogFile logFile) {
        String NAMESPACE_JAVA_CLASS_STRING_TEMPLATE =
                "package <namespace>;\n\n"
                        + "import <namespaceImport>;\n"
                        + "import <abstractNamespaceImport>;\n\n"
                        + "<namespaceAnnotation>\n"
                        + "public class <namespaceClassName> extends AbstractNamespace {\n\n"
                        + "    <methods>"
                        + "}\n";
        ST template = new ST(NAMESPACE_JAVA_CLASS_STRING_TEMPLATE);
        template.add("namespace", namespace);
        template.add("namespaceImport", Utils.getNgmonAnnotationNamespaceImport());
        template.add("abstractNamespaceImport", Utils.getNgmonLoggerAbstractNamespaceImport());
        template.add("namespaceAnnotation", Utils.ngmonAnnotationNamespace());
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

    private String prettyPrintMethod(Log log) {
        ST methodTemplate = new ST(
                "public AbstractNamespace <methodName>(<formalParameters>) {\n" +
                        "    return log(<parameterNames>);\n" +
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
                parameterNames.append(var.getNgmonName());
            } else {
                formalParameters.append(var.getName());
                parameterNames.append(var.getName());
            }
            i++;
        }
        //formalParameters.delete(formalParameters.length() - 2, formalParameters.length());
        methodTemplate.add("formalParameters", formalParameters);
        methodTemplate.add("parameterNames", parameterNames);

//        System.out.println(methodTemplate.render());
        return methodTemplate.render();
    }

    /**
     * Append to current
     * @param logFile
     */
    protected void addMethodsToNamespace(LogFile logFile) {
        // TODO append to methodList and regenerate whole file content, instead of looking up particular
        // method and inserting it in to appropriate position (?)  TreeSet
//        methods.add()
    }


}
