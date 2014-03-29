package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.util.Set;
import java.util.TreeSet;

/* OLD VERSION
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


    private Set<String> methods = new TreeSet<>();
    private String namespaceClassName;
    private String namespace;
    private ST namespaceFileContent;
//    private String filePath;

    /**
     * Create all log methods connected for this namespace from logically associated logFiles.
     * These logs are in new NGMON namespace file.
     *
     * @param namespace current namespace to create file in
     * @param logFiles  set of LogFiles associated with this namespace
     */
    public NamespaceFileCreator(String namespace, TreeSet<LogFile> logFiles) {
//        System.out.println("namespace=" + namespace);

        this.namespace = namespace;
        // pick any element from set and set ClassName
        this.namespaceClassName = logFiles.first().getNamespaceClass();
        this.namespaceFileContent = prepareNewNamespace(namespace);
        addMethodsToNamespaceFileContent(logFiles);
    }

    public String getNamespaceFileContent() {
        return this.namespaceFileContent.render();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNamespaceClassName() {
        return namespaceClassName;
    }

    /**
     * Create NGMON namespace with proper imports, class declaration and log methods.
     *
     * @param namespace package name for log_events declaration
     * @return StringTemplate with ready-to-create file
     */
    private ST prepareNewNamespace(String namespace) {
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

        return template;
    }

    /**
     * Append methods from LogFile objects to methods in namespaceFileContent.
     *
     * @param logFiles set containing all LogFiles Logs with possible new methods.
     */
    protected void addMethodsToNamespaceFileContent(Set<LogFile> logFiles) {
        for (LogFile logFile : logFiles) {

            for (Log log : logFile.getLogs()) {
                String newMethod = prettyPrintMethod(log);
                if (!methods.contains(newMethod)) {
                    methods.add(newMethod);
                }
            }
        }
        namespaceFileContent.add("methods", methods);
    }

    /**
     * Create suitable method for this namespace from Log object using StringTemplate.
     *
     * @param log to create method from
     * @return method suitable for
     */
    private String prettyPrintMethod(Log log) {
        // TODO 80 characters limitation (?)
        ST methodTemplate = new ST(
                "public AbstractNamespace <methodName>(<formalParameters>) {\n" +
//                        "    return log(<parameterNames>);\n" +
                        "    return this;\n" +
                        "}\n\n");

        methodTemplate.add("methodName", log.getMethodName());
        StringBuilder formalParameters = new StringBuilder();
//        StringBuilder parameterNames = new StringBuilder();

        int i = 0;
        for (LogFile.Variable var : log.getVariables()) {
            if (i != 0) {
                formalParameters.append(", ");
//                parameterNames.append(", ");
            }


            /** Use String data type if variable is of any other data type then NGMON allowed data types */
            String type = var.getType();
            if (Utils.isNgmonPrimitiveTypesOnly()) {
                if (!Utils.listContainsItem(Utils.NGMON_ALLOWED_TYPES, type)) {
                    type = "String";
                }
            }

            formalParameters.append(type).append(" ");
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

}
