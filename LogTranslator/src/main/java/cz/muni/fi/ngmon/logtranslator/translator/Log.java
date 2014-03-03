package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.ArrayList;
import java.util.List;

public class Log {
    private List<String> comments;
    private List<LogFile.Variable> variables;
    private String level;
    private String tag;
    private String methodName;

    public Log() {
        comments = new ArrayList<>();
        variables = new ArrayList<LogFile.Variable>();
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void generateMethodName() {
        StringBuilder logName = new StringBuilder();
        int counter = 0;
        int logNameLength = LogTranslator.getLoggerLoader().getNgmonLogLength();

        for (String comment : comments) {
            for (String str : comment.split(" ")) {
                if (counter != 0) {
                    logName.append("_");
                }
                if (!Utils.BANNED_LIST.contains(str)) logName.append(str);
                counter++;
                if (counter >= logNameLength) break;
            }
        }
        methodName = logName.toString();
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
