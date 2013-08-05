package cz.muni.fi.ngmon.logchanger;

import java.util.List;
import java.util.Map;

/**
 * LogChanger object passed from python code. Main objective is to get types of all
 * variables.
 *
 * @author mtoth
 */
public class LogChanger {

    private int lineStart;
    private int lineEnd;
    private String path;
    private String oldLog;
    private String newLog;
    private List<String> variables;

    private int charStart;
    private int charEnd;
    private Map<String, Object> variableMapping;

           
    public int getLineStart() {
        return lineStart;
    }

    public void setLineStart(int lineStart) {
        this.lineStart = lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
    }

    public String getFilepath() {
        return path;
    }

    public void setFilepath(String filepath) {
        this.path = filepath;
    }

    public String getOldLog() {
        return oldLog;
    }

    public void setOldLog(String oldLog) {
        this.oldLog = oldLog;
    }

    public String getNewLog() {
        return newLog;
    }

    public void setNewLog(String newLog) {
        this.newLog = newLog;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public int getCharStart() {
        return charStart;
    }

    public void setCharStart(int charStart) {
        this.charStart = charStart;
    }

    public int getCharEnd() {
        return charEnd;
    }

    public void setCharEnd(int charEnd) {
        this.charEnd = charEnd;
    }

    public Map<String, Object> getVariableMapping() {
        return variableMapping;
    }

    public void setVariableMapping(Map<String, Object> variableMapping) {
        this.variableMapping = variableMapping;
    }

    @Override
    public String toString() {
        return "LogChanger{" + "lineStart=" + lineStart + ", lineEnd=" + lineEnd + ", path=" + path + ", oldLog=" + oldLog + ", newLog=" + newLog + ", variables=" + variables + ", charStart=" + charStart + ", charEnd=" + charEnd + ", variableMapping=" + variableMapping + '}';
    }

    
    
}
