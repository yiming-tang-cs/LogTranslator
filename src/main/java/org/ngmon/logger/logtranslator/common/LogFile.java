package org.ngmon.logger.logtranslator.common;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

/**
 * This object represents .java file with all variables found.
 * Variable contains more detailed information about specific
 * variable in list.
 * <p/>
 * LogFile is used for incorporating Log instances belonging to same
 * the namespace.
 */
public class LogFile implements Comparable {

    // Mapping of variableName : <variableProperties>
    private Map<String, List<Variable>> variableList = new LinkedHashMap<>();
    private Set<LogFile> connectedLogFilesList = new HashSet<>();
    private String filepath;
    private String namespace;
    private String namespaceClass;
    private String packageName;
    private List<Log> logs;
    private boolean containsStaticImport;
    private List<String> staticImports;
    private List<String> imports;
    private boolean finishedParsing = false;
    private String rewrittenJavaContent;

    public LogFile(String filename) {
        filepath = filename;
        logs = new ArrayList<>();
        this.imports = new ArrayList<>();

    }

    public Map<String, List<Variable>> getVariableList() {
        return variableList;
    }

    public void putVariableList(String variableName, Variable variable) {
        List<Variable> vars;
        if (getProperties(variableName) != null) {
            vars = getProperties(variableName);
        } else {
            vars = new ArrayList<>();
        }
        vars.add(variable);
        variableList.put(variableName, vars);
    }

    public List<Variable> getProperties(String variableName) {
        return variableList.get(variableName);
    }

    public String getFilepath() {
        return filepath;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * Set namespace and create namespaceClass. namespaceClass is created
     * from end of namespace and appended 'Namespace' by default.
     * Set namespaceClassName and drop last part of namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
        this.namespaceClass = getNamespaceEnd() + Utils.getNgmonDefaultNamespaceEnd();
        this.namespace = namespace.substring(0, namespace.lastIndexOf("."));
    }

    public String getWholeNamespace() {
        return namespace + "." + namespaceClass;
    }

    /**
     * Create from current namespace name ClassName.
     *
     * @return Namespace end (mostly className from import)
     */
    public String getNamespaceEnd() {
        if (namespace == null) {
            System.err.println("Error! Namespace is null! PackageName=" + packageName + " " + filepath);
            this.namespace = packageName;
        }
        StringBuilder stringBuilder = new StringBuilder(namespace.substring(namespace.lastIndexOf(".") + 1));
        stringBuilder.replace(0, 1, stringBuilder.substring(0, 1).toUpperCase());
        return stringBuilder.toString();
    }

    public String getNamespaceClass() {
        return namespaceClass;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public boolean isContainsStaticImport() {
        return containsStaticImport;
    }

    public void setContainsStaticImport(boolean containsStaticImport) {
        this.staticImports = new ArrayList<>();
        this.containsStaticImport = containsStaticImport;
    }

    public void addStaticImports(String staticImport) {
        staticImports.add(staticImport);
    }

    public void addImport(String importQualifiedName) {
        this.imports.add(importQualifiedName);
    }

    public List<String> getImports() {
        return imports;
    }

    public boolean isFinishedParsing() {
        return finishedParsing;
    }

    public void setFinishedParsing(boolean finishedParsing) {
        this.finishedParsing = finishedParsing;
    }

    public void addConnectedLogFilesList(LogFile logFile) {
        this.connectedLogFilesList.add(logFile);
    }

    public Set<LogFile> getConnectedLogFilesList() {
        return connectedLogFilesList;
    }

    public void addLog(Log log) {
        this.logs.add(log);
    }

    @Override
    public String toString() {
        return "LogFile{" +
            "variableList=" + variableList +
            ", filepath='" + filepath + '\'' +
            '}';
    }

    /**
     * Simplified helper method for storing variables into variable list. More data based on context is passed to
     * checkAndStoreVariable() which actually stores variable and all related information into variable list.
     *
     * @param ctx              superclass of given context, used for getting given context positions and tokens
     * @param variableName     name of variable to be stored into variable list
     * @param variableTypeName type of variable to be stored into variable list
     * @param isField          true if variable is declared in class, not in method body or as formal parameter in method
     */

    public void storeVariable(ParserRuleContext ctx, String variableName, String variableTypeName, boolean isField, String newNgmonName) {
//        checkAndStoreVariable(variableName, variableTypeName, newNgmonName, ctx.start.getLine(),
//                ctx.getStart().getCharPositionInLine(), ctx.getStop().getCharPositionInLine(),
//                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), isField);
        // TODO add trace() log
        checkAndStoreVariable(variableName, variableTypeName, newNgmonName, ctx.start.getLine(), isField);
    }

    /**
     * Stores variable and all related information into variable list.
     *
     * @param variableName variable name
     * @param variableType variable type as string
     * @param lineNumber   variable line number occurrence
     * @param isField      false if variable is declared in method, true otherwise
     */
    private void checkAndStoreVariable(String variableName, String variableType, String newNgmonName, int lineNumber, boolean isField) {
        LogFile.Variable p = this.new Variable();

        if (variableName == null || variableType == null) {
            throw new NullPointerException("Variable name or type are null!");
        } else {
            p.setName(variableName);
            p.setType(variableType);
        }
        if (newNgmonName != null) p.setNgmonName(newNgmonName);

        p.setLineNumber(lineNumber);
        p.setField(isField);
        this.putVariableList(variableName, p);
    }

    /**
     * 0 = equal, -1 otherwise
     *
     * @param other LogFile object to compare
     * @return 0 if LogFiles are same - have same FilePath, -1 otherwise
     */
    @Override
    public int compareTo(Object other) {
        if (other == null) {
            throw new NullPointerException("Other object is null!");
        }
        // TODO make sure it works correctly
        if (other instanceof LogFile) {
            LogFile otherLf = (LogFile) other;
            if (otherLf.getFilepath().equals(this.getFilepath())) {
                return 0;
            }
        }
        return -1;
    }

    public String getRewrittenJavaContent() {
        return rewrittenJavaContent;
    }

    public void setRewrittenJavaContent(String rewrittenJavaContent) {
        this.rewrittenJavaContent = rewrittenJavaContent;
    }

    public class Variable {
        private String name; // ?
        private String type;
        private String ngmonName;
        private int lineNumber;
        private boolean isField; // true if variable is declared in class, not in method body
        private String changeOriginalName = null; // need if we want to use changed original variable in log - typecasting

        public String getNgmonName() {
            return ngmonName;
        }

        public void setNgmonName(String ngmonName) {
            this.ngmonName = ngmonName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public boolean isField() {
            return isField;
        }

        public void setField(boolean isField) {
            this.isField = isField;
        }

        public String getChangeOriginalName() {
            return changeOriginalName;
        }

        public void setChangeOriginalName(String changeOriginalName) {
            this.changeOriginalName = changeOriginalName;
        }

        @Override
        public String toString() {
            return "Variable{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", lineNumber=" + lineNumber +
                ", field=" + isField +
                '}';
        }
    }
}
