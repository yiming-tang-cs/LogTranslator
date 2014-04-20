package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.translator.CommonsLoggerLoader;
import org.ngmon.logger.logtranslator.translator.Slf4jLoggerLoader;

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
            output.append(pattern).append("\n\n");
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

    /**
     * Create new Go-Match pattern using log object, which
     * holds all information about log.
     *
     * @param log create new gomatch pattern from this log
     * @return created new goMatch pattern
     */
    private static String createNewPattern(Log log) {
        StringBuilder pattern = new StringBuilder(Utils.getApplicationNamespace() + "." + log.getMethodName() + "##");

        String originalLog = log.getOriginalLog();
        originalLog = originalLog.substring(originalLog.indexOf("("), originalLog.lastIndexOf(")"));
        originalLog = extractVars(originalLog, log);

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
        }
        // TODO check slf4j and common on behaviour!
        if (log.getGeneratedReplacementLog().contains("tag(\"methodCall\")") && originalLog.contains("_")) {
            originalLog = originalLog.replaceAll("_", "");
        }

        pattern.append(originalLog);
        return pattern.toString();
    }

    /**
     * Extract commentary text from log statement.
     * Handle special case of formatted variables
     *
     * @param text to extract variables from
     * @param log containing all information about this log
     * @return altered text without variables
     */
    private static String extractVars(String text, Log log) {
        List<LogFile.Variable> formattedVariables = log.getFormattedVariables();
        String symbol = log.getFormattingSymbol();
        String textNoFormatters = null;

        StringBuilder cleanText = new StringBuilder();
        /** If text contains slf4j's formatter brackets {}, don't extract variables
         manually */
        if (formattedVariables != null) {
            if (symbol.equals("%")) {
                textNoFormatters = CommonsLoggerLoader.isolateFormatters(text, formattedVariables);

            } else if (symbol.equals("{}")) {
                textNoFormatters = Slf4jLoggerLoader.isolateFormatters(text, formattedVariables);
            }
            return textNoFormatters;
        }

        /** If not, continue manually */
        boolean parseComment = false;
        char c_prev = text.charAt(0);
        for (char c : text.substring(1).toCharArray()) {
            if (c == '\"' && c_prev != '\\') {
                parseComment = !parseComment;
            }
            if (parseComment) {
                if (c != '"') {
                    cleanText.append(c);
                }
                // if we don't skip variables, change them to "_"
            } else {
                if (c == ',') {
                    cleanText.append(", ");
                } else if (c == '\"') {
                    ;
                } else if (c != '+') {
                    cleanText.append("_");
                }
            }
            c_prev = c;
        }
        /** remove multiple empty spaces or underscore chars, remove all non alphanum */
        return cleanText.toString().replaceAll("[^A-Za-z0-9,': \\[\\]_]->#\\(\\)", "").replaceAll("  +", " ").replaceAll("_+", "_");
    }
}
