package org.ngmon.logger.logtranslator.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Log class is a representation of one log call method with all
 * related variables used in this one method call.
 * Log is used for generating new methods for NGMON.
 */
public class Log {
    private List<String> comments;
    private List<LogFile.Variable> variables;
    private String level;
    private List<String> tag;
    private String methodName;
    private String generatedNgmonLog;
    private String generatedReplacementLog;

    public Log() {
        comments = new ArrayList<>();
        variables = new ArrayList<>();
        methodName = null;
        tag = null;
        level = null;
    }


    public List<String> getComments() {
        return comments;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public List<LogFile.Variable> getVariables() {
        return variables;
    }

    public void addVariable(LogFile.Variable variable) {
        this.variables.add(variable);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(String tag) {
        if (this.tag == null) {
            this.tag = new ArrayList<>();
        }
        this.tag.add(tag);
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getGeneratedNgmonLog() {
        return generatedNgmonLog;
    }

    public void setGeneratedNgmonLog(String generatedNgmonLog) {
        this.generatedNgmonLog = generatedNgmonLog;
    }

    public String getGeneratedReplacementLog() {
        return generatedReplacementLog;
    }

    public void setGeneratedReplacementLog(String generatedReplacementLog) {
        this.generatedReplacementLog = generatedReplacementLog;
    }

    @Override
    public String toString() {
        return "Log{" +
                "comments=" + comments +
                ", variables=" + variables +
                ", level='" + level + '\'' +
                ", tag='" + tag + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
