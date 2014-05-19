package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.util.*;

/*
 package log_events.org.apache.hadoop.hdfs.nfs;

 import cz.muni.fi.annotation.Namespace;
 import cz.muni.fi.logger.AbstractNamespace;

 public class NfsNamespace extends AbstractNamespace {

     public AbstractNamespace MOUNT_MNT_PATH_CLIENT(String path, String client, String client2) {
        return this;
     }

     public AbstractNamespace FAIL_PENDING_WRITE_NEXTOFFSET(String key_getMin, String key_getMax, String getNextOffsetUnprotected) {
        return this;
     }
 }
*/

/**
 * Class creates NGMON's namespace class for each namespace with multiple LogFiles.
 * LogFiles contain Log calls on rewritten log method calls.
 * You can see example of such class in comment above.
 * <p/>
 * These files are generated into <application_home>/<ngmon_log_events_import_prefix>/<application-namespace>
 * location.
 * <p/>
 * For example: hadoop/log_events/src/main/java/org/apache/hadoop/HdfsNamespace.java file.
 */
public class NamespaceFileCreator {

    private Set<NGMONMethod> methods = new TreeSet<>();
    private String namespaceClassName;
    private String namespace;
    private ST namespaceFileContent;
    private List<String> importList = new ArrayList<>();
    private Set<String> tempImportSet = new HashSet<>();

    /**
     * Create all log methods connected for this namespace from logically associated logFiles.
     * These logs are in new NGMON namespace file.
     *
     * @param namespace current namespace to create file in
     * @param logFiles  set of LogFiles associated with this namespace
     */
    public NamespaceFileCreator(String namespace, TreeSet<LogFile> logFiles) {
        this.namespace = namespace = namespace.substring(0, namespace.lastIndexOf("."));
        // pick any element from set and set ClassName
        this.namespaceClassName = logFiles.first().getNamespaceClass();
        this.namespaceFileContent = prepareNewNamespace(namespace);
        addMethodsToNamespaceFileContent(logFiles);
        addImportsToFileContent();
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
                + "<imports>\n\n"
                + "public class <namespaceClassName> extends AbstractNamespace {\n\n"
                + "    <methods>"
                + "}\n";
        ST template = new ST(NAMESPACE_JAVA_CLASS_STRING_TEMPLATE);
        template.add("applicationNamespace", namespace);
        template.add("namespaceClassName", namespaceClassName);

        return template;
    }

    /**
     * Create new NGMON's method and adds it to Method's lists if it does not exist.
     * Append methods from LogFile objects to methods in namespaceFileContent.
     *
     * @param logFiles set containing all LogFiles Logs with possible new methods.
     */
    protected void addMethodsToNamespaceFileContent(Set<LogFile> logFiles) {
        StringBuilder methodsString = new StringBuilder();
        for (LogFile logFile : logFiles) {
            for (Log log : logFile.getLogs()) {
                NGMONMethod ngmonMethod = new NGMONMethod(log.getMethodName(), prepareFormalArguments(log));
                ngmonMethod.setLog(log);
                log.setGeneratedNgmonLog(prettyPrintMethod(ngmonMethod));

                if (!methods.contains(ngmonMethod)) {
                    methods.add(ngmonMethod);
                }
            }
        }

        for (NGMONMethod method : methods) {
            method.getLog().setUsedGeneratedNgmonLog(true);
            methodsString.append(method.getLog().getGeneratedNgmonLog());
        }
        namespaceFileContent.add("methods", methodsString.toString());
    }

    /**
     *  Add imports to AbstractNamespace generation class.
     */
    private void addImportsToFileContent() {
        importList.add("import " + Utils.getNgmonLoggerAbstractNamespaceImport() + ";\n");
        for (String col : tempImportSet) {
            importList.add("import java.util." + col + ";\n");
        }
        this.namespaceFileContent.add("imports", importList);
    }

    /**
     * Create suitable method for this namespace from Log object using StringTemplate.
     *
     * @param ngmonMethod to create method from
     * @return method suitable for
     */
    private String prettyPrintMethod(NGMONMethod ngmonMethod) {
        ST methodTemplate = new ST(
            "public AbstractNamespace <methodName>(<formalParameters>) {\n" +
//                        "    return log(<parameterNames>);\n" +
                "    return this;\n" +
                "}\n\n");

        methodTemplate.add("methodName", ngmonMethod.getMethodName());
        StringBuilder formalParameters = new StringBuilder();

        Map<String, String> formalParametersMap = ngmonMethod.getFormalParameters();

        boolean firstNewLine = true;
        int methodLength = ngmonMethod.getMethodName().length();
        String indentation = "\n\t\t\t\t\t";
        for (String name : formalParametersMap.keySet()) {
            formalParameters.append(formalParametersMap.get(name)).append(" ").append(name).append(", ");
            if (firstNewLine) {
                if (methodLength + formalParameters.length() > 80) {
                    formalParameters.append(indentation);
                    firstNewLine = false;
                }
                // take distance from last "\n\t\t\t"
            } else if (formalParameters.length() - formalParameters.toString().lastIndexOf(indentation) > 100) {
                formalParameters.append(indentation);
            }
        }
        // remove last comma and space
        if (formalParametersMap.size() > 0) {
            int pos = formalParameters.toString().lastIndexOf(", ");
            formalParameters.delete(pos, formalParameters.length());
        }

        methodTemplate.add("formalParameters", formalParameters);
        return methodTemplate.render();
    }

    /**
     * Prepare new NGMON's method. Alter duplicated variables - same name and data type,
     * by adding suffix number to latest variable name.
     *
     * @param log to create method from
     * @return returns mapping of variableName - variableType
     */
    public LinkedHashMap<String, String> prepareFormalArguments(Log log) {
        LinkedHashMap<String, String> parametersMap = new LinkedHashMap<>();
        for (LogFile.Variable variable : log.getVariables()) {

            /** Use String data type if variable is of any other data type then NGMON allowed data types */
            String varType = variable.getType();
            if (Utils.isNgmonPrimitiveTypesOnly()) {
                if (!Utils.itemInList(Utils.NGMON_ALLOWED_TYPES, varType.toLowerCase())) {
                    varType = "String";
                }
            }

            /** Get name of NGMON's variable */
            String varName;
            if (variable.getNgmonName() != null) {
                varName = variable.getNgmonName();
            } else {
                varName = variable.getName();
            }

            /** Handle duplicated names if varName & varType are same */
            if (parametersMap.containsKey(varName) && (parametersMap.get(varName).equals(varType))) {
                // get all varNames, which has same name + numbered suffix, getLast and add last+1 into map
                int occurrence = 0;
                for (String keyName : parametersMap.keySet()) {
                    if (keyName.equals(varName)) {
                            occurrence++;
                    } else if (keyName.startsWith(varName) && (keyName.length() > varName.length())) {
                        try {
                            int tmp = Integer.parseInt(keyName.substring(varName.length()));
                            if (tmp > occurrence) {
                                occurrence = tmp;
                            }
                        } catch (NumberFormatException nfe) {
                            occurrence++;
                        }
                    }
                }
                varName = varName + (occurrence + 1);
            }

            /** If variable is any Collection, add import to namespace */
            if (varType.contains("<")) {
                String generics = varType.substring(varType.indexOf("<"), varType.indexOf(">"));
                if (generics.contains(",")) {
                    String[] genericTypes = generics.split(",");
                    for (String genericType : genericTypes) {
                        if (Utils.itemInList(Utils.COLLECTION_LIST, genericType)) {
                            tempImportSet.add(genericType);
                        }
                    }
                }

                String collectionVarType = varType.substring(0, varType.indexOf("<"));
                if (collectionVarType.contains(".")) {
                    collectionVarType = collectionVarType.substring(0, collectionVarType.indexOf("."));
                }
                if (Utils.itemInList(Utils.COLLECTION_LIST, collectionVarType)) {
                    tempImportSet.add(collectionVarType);
                }
            }
            parametersMap.put(varName, varType);
        }
        return parametersMap;
    }
}
