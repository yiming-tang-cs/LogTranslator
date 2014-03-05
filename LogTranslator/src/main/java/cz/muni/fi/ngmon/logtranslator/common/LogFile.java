package cz.muni.fi.ngmon.logtranslator.common;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This object represents .java file with all variables found.
 * Variable contains more detailed information about specific
 * variable in list.
 * <p/>
 * LogFile is used for incorporating Log instances belonging to same
 * the namespace.
 */
public class LogFile {

    // Mapping of variableName : <variableProperties>
    private Map<String, List<Variable>> variableList = new LinkedHashMap<>();
    private String filepath;
    private String namespace;
    private String namespaceClass;
    private String packageName;
    private List<Log> logs;

    public LogFile(String filename) {
        filepath = filename;
        logs = new ArrayList<>();

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

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceEnd() {
        if (namespace == null) {
            System.err.println("Error! Namespace is null!");
        }
        StringBuilder stringBuilder = new StringBuilder(namespace.substring(namespace.lastIndexOf(".") + 1));
        stringBuilder.replace(0, 1, stringBuilder.substring(0, 1).toUpperCase());
        return stringBuilder.toString();
    }

    public String getNamespaceClass() {
        return namespaceClass;
    }

    public void setNamespaceClass(String namespaceClass) {
        this.namespaceClass = namespaceClass;
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
     * @param ctx               superclass of given context, used for getting given context positions and tokens
     * @param variableName      name of variable to be stored into variable list
     * @param variableTypeName  type of variable to be stored into variable list
     * @param isField           true if variable is declared in class, not in method body or as formal parameter in method
     */

    public void storeVariable(ParserRuleContext ctx, String variableName, String variableTypeName, boolean isField) {
        checkAndStoreVariable(variableName, variableTypeName, ctx.start.getLine(),
                ctx.getStart().getCharPositionInLine(), ctx.getStop().getCharPositionInLine(),
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), isField);
    }

    /**
     * Stores variable and all related information into variable list.
     *
     * @param variableName variable name
     * @param variableType variable type as string
     * @param lineNumber variable line number occurrence
     * @param lineStartPosition variable occurrence start line position
     * @param lineStopPosition variable occurrence stop line position
     * @param startPosition variable occurrence start position
     * @param stopPosition variable occurrence stop position
     * @param isField false if variable is declared in method, true otherwise
     */
    private void checkAndStoreVariable(String variableName, String variableType, int lineNumber,
                                       int lineStartPosition, int lineStopPosition, int startPosition, int stopPosition, boolean isField) {
        LogFile.Variable p = this.new Variable();

        if (variableName == null || variableType == null) {
            throw new NullPointerException("Variable name or type are null!");
        } else {
            p.setName(variableName);
            p.setType(variableType);
        }

        p.setLineNumber(lineNumber);
        p.setStartPosition(lineStartPosition);
        p.setStopPosition(lineStopPosition);
        p.setFileStartPosition(startPosition);
        p.setFileStopPosition(stopPosition);
        p.setField(isField);
        this.putVariableList(variableName, p);
    }

    public class Variable {
        private String name; // ?
        private String type;
        private int lineNumber;
        private int startPosition;
        private int stopPosition;
        private int fileStartPosition;
        private int fileStopPosition;
        private boolean isField; // true if variable is declared in class, not in method body

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

        public void setStartPosition(int startPosition) {
            this.startPosition = startPosition;
        }

        public void setStopPosition(int stopPosition) {
            this.stopPosition = stopPosition;
        }

        public void setFileStartPosition(int fileStartPosition) {
            this.fileStartPosition = fileStartPosition;
        }

        public void setFileStopPosition(int fileStopPosition) {
            this.fileStopPosition = fileStopPosition;
        }

        public boolean isField() {
            return isField;
        }

        public void setField(boolean isField) {
            this.isField = isField;
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", lineNumber=" + lineNumber +
                    ", field=" + isField +

                    /* ", startPosition=" + startPosition +
                    ", stopPosition=" + stopPosition +
                    ", fileStartPosition=" + fileStartPosition +
                    ", fileStopPosition=" + fileStopPosition + */
                    '}';
        }
    }
}
