package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * // comment
 * //"originalLog":"LOG.info(\"Processing \"+count+\" messages from DataNodes \"+\"that were previously queued during standby state\")"}}}
 * org.hadooop.XYZ.replacementLog##Processing <INT:count> messages from DataNodes that were previously queued during standby state.")
 */
public class GoMatchGenerator {

    private static Set<String> goMatchPatternList = new HashSet<>();

    public static String getGoMatchPatternListToString() {
        StringBuilder output = new StringBuilder();
        for (String pattern : goMatchPatternList) {
            output.append(pattern);
        }
        return output.toString();
    }

    public static void createGoMatch(Log log) {
        log.getComments();
        String goMatchPattern = createNewPattern(log);
        // prepend with comment and original log file
        goMatchPatternList.add("// " + log.getOriginalLog() + "\n" + goMatchPattern);
        log.setGoMatchLog(goMatchPattern);
    }

    private static String createNewPattern(Log log) {
        StringBuilder pattern = new StringBuilder(Utils.getApplicationNamespace() + "." + log.getMethodName() + "##");

        String originalLog = log.getOriginalLog();
        originalLog = originalLog.substring(originalLog.indexOf("("), originalLog.lastIndexOf(")"));
        originalLog = extractVars(originalLog);

        List<String> variableNames = new ArrayList<>();
        for (LogFile.Variable var : log.getVariables()) {

            /** Rename duplicated variable names */
            String varName;
            if (var.getNgmonName() != null) {
                varName = var.getNgmonName();
            } else {
                varName = var.getName();
            }

            if (variableNames.contains(varName)) {
                // get all varNames, which have same name + numbered suffix, getLast and add last+1 into map
                int occurrence = 0;
                for (String vName : variableNames) {
                    if (vName.startsWith(varName) && (vName.length() > varName.length())) {
                        int tmp = Integer.parseInt(vName.substring(varName.length()));
                        if (tmp > occurrence) {
                            occurrence = tmp;
                        }
                    }
                }
                varName = varName + (occurrence + 1);
            }
            variableNames.add(varName);

            String type = var.getType();
            if (!Utils.itemInList(Utils.NGMON_ALLOWED_TYPES, type.toLowerCase())) {
                type = "String";
            }
            originalLog = originalLog.replaceFirst("_", "<" + type.toUpperCase() + ":" + varName + ">");
            if (log.getGeneratedReplacementLog().contains("tag(\"methodCall\")") && originalLog.contains("_")) {
                originalLog = originalLog.replaceAll("_", "");
            }
        }

        pattern.append(originalLog).append("\n\n");
        return pattern.toString();
    }

    /**
     * Extract commentary text from log statement.
     *
     * @param text to extract variables from
     * @return altered text without variables
     */
    private static String extractVars(String text) {
        StringBuilder cleanText = new StringBuilder();
        boolean start = false;
        char c_prev = text.charAt(0);
        for (char c : text.substring(1).toCharArray()) {
            if (c == '\"' && c_prev != '\\') {
                start = !start;
                c_prev = c;
            }
            if (start) {
                if (c != '"') {
                    cleanText.append(c);
                }
            } else {
                if (c == ',') {
                    cleanText.append(", ");
                } else if (c == '\"') {
                    ;
                } else if (c != '+') {
                    cleanText.append("_");
                } else if (c == '+') {
                    cleanText.append(" ");
                }
            }
        }
        /** remove multiple empty spaces or underscore chars, remove all non alphanum */
        String clean =  cleanText.toString().replaceAll("[^A-Za-z0-9 _]", "").replaceAll("  +", " ").replaceAll("_+", "_");;
        return clean;

    }

}
