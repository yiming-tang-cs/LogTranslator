package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Log;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * # comment
 * #"originalLog":"LOG.info(\"Processing \"+count+\" messages from DataNodes \"+\"that were previously queued during standby state\")"}}}
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
        goMatchPatternList.add(createNewPattern(log));
    }

    private static String createNewPattern(Log log) {
        StringBuilder pattern = new StringBuilder(Utils.getApplicationNamespace() + "." + log.getMethodName() + "##");
        String originalLog = log.getOriginalLog();
        originalLog = originalLog.substring(originalLog.indexOf("("), originalLog.lastIndexOf(")"));
        // remove all between " "
        originalLog = extractVars(originalLog);

        originalLog = originalLog.replaceAll("_+", "_");
        for (LogFile.Variable var : log.getVariables()) {
            originalLog = originalLog.replaceFirst("_", "<" + var.getType().toUpperCase() + ":" + var.getName() + ">");
        }
//        System.out.println(log.getOriginalLog() + "\n" + originalLog + "\n");
        pattern.append(originalLog).append("\n\n");
        // prepend with comment and original log file
        pattern.insert(0, "//" + log.getOriginalLog() + "\n");
        return pattern.toString();
    }

    /**
     * Extract comments text and variables
     * @param text
     * @return
     */
    private static String extractVars(String text) {
        List<String> varList = new ArrayList<>();
        StringBuilder cleanText = new StringBuilder();
        StringBuilder var = new StringBuilder();
        boolean start = false;
        boolean newVar = true;
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
                    cleanText.append(",");
                    newVar = true;
                } else if (c == '\"') {
                    ;
                } else if (c != '+') {
                    if (newVar) {
                        varList.add(var.toString());
                        newVar = false;
                    }
                    var.append(c);
                    cleanText.append("_");
                }
            }
        }
//        System.out.println("vars=");
        return cleanText.toString();

    }

}
