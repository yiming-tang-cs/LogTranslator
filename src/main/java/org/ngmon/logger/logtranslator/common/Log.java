package org.ngmon.logger.logtranslator.common;

import java.util.*;

/**
 * Log class is a representation of one log call method with all
 * related variables used in this one method call.
 * Log is used for generating new methods for NGMON.
 */
public class Log {
    private List<String> comments;
    private List<LogFile.Variable> variables;
    private String level;
    private Set<String> tags;
    private String methodName;
    private String generatedNgmonLog;
    private boolean usedGeneratedNgmonLog = false;
    private String generatedReplacementLog;
    private String originalLog;
    private String goMatchLog;
    private List<LogFile.Variable> formattedVariables; // variables in slf4j formatter's standard, declared after first comma
    private String formattingSymbol;
    private List<String> ternaryValues;
    private LogFile logFile;

    public Log() {
        comments = new ArrayList<>();
        variables = new ArrayList<>();
        methodName = null;
        tags = null;
        level = null;
    }

    /**
     * Return all comments for this log.
     * Comments are all strings found in given "original" log.
     *
     * @return list of comments
     */
    public List<String> getComments() {
        return comments;
    }

    public void cleanUpCommentList() {
        List<String> temp = new ArrayList<>(comments);
        Collections.copy(temp, comments);
        for (String s : temp) {
            if (s.equals("")) {
                comments.remove(s);
            }
        }
    }

    public void addComment(String comment) {
        if (comment.length() > 1) {
            this.comments.add(0, comment);
        }
    }

    public boolean isUsedGeneratedNgmonLog() {
        return usedGeneratedNgmonLog;
    }

    public void setUsedGeneratedNgmonLog(boolean usedGeneratedNgmonLog) {
        this.usedGeneratedNgmonLog = usedGeneratedNgmonLog;
    }

    public String getFormattingSymbol() {
        return formattingSymbol;
    }

    public void setFormattingSymbol(String formattingSymbol) {
        this.formattingSymbol = formattingSymbol;
    }

    public List<LogFile.Variable> getFormattedVariables() {
        return formattedVariables;
    }

    public void addFormattedVariables(LogFile.Variable variable) {
        if (this.formattedVariables == null) {
            this.formattedVariables = new ArrayList<>();
        }
        this.formattedVariables.add(0, variable);
    }

    public String getOriginalLog() {
        return originalLog;
    }

    public void setOriginalLog(String originalLog) {
        this.originalLog = originalLog;
    }

    public List<LogFile.Variable> getVariables() {
        return variables;
    }

    /**
     * Add variable to this log.
     *
     * @param variable variable to be added to list of variables
     */
    public void addVariable(LogFile.Variable variable) {
        this.variables.add(0, variable);
    }

    public void setTernaryValues(String expBool, String expTrue, String expFalse, String expBoolRight) {
        this.ternaryValues = new ArrayList<>();
        this.ternaryValues.add(expBool);
        this.ternaryValues.add(expTrue);
        this.ternaryValues.add(expFalse);
        if (expBoolRight != null) {
            this.ternaryValues.add(expBoolRight);
        }
    }

    public List<String> getTernaryValues() {
        return ternaryValues;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Set<String> getTag() {
        if (this.tags == null) {
            this.tags = new TreeSet<>();
            for (LogFile.Variable v : variables) {
                if (v.getTag() != null) {
                    this.tags.add(v.getTag());
                }
            }
        }
        return tags;
    }

    public void setTag(String tag) {
        if (this.tags == null) {
            this.tags = new TreeSet<>();
        }
        tags.add(tag);
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        Character firstLetter = Character.toLowerCase(methodName.charAt(0));
        this.methodName = firstLetter + methodName.substring(1);
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
            ", tags='" + tags + '\'' +
            ", methodName='" + methodName + '\'' +
            ", \nfilePath='" + logFile.getFilepath() + '\'' +
            '}';
    }

    public String getGoMatchLog() {
        return goMatchLog;
    }

    public void setGoMatchLog(String goMatchLog) {
        this.goMatchLog = goMatchLog;
    }

    public void setLogFile(LogFile logFile) {
        this.logFile = logFile;
    }

    public LogFile getLogFile() {
        return logFile;
    }
}
