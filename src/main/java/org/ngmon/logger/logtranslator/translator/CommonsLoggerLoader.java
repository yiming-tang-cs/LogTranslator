package org.ngmon.logger.logtranslator.translator;

import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.generator.StringLengthComparator;

import java.util.*;


/*
    http://commons.apache.org/proper/commons-logging/apidocs/index.html
    Commons Logging
    http://commons.apache.org/proper/commons-logging/apidocs/org/apache/commons/logging/Log.html
    http://commons.apache.org/proper/commons-logging/apidocs/org/apache/commons/logging/impl/Log4JLogger.html
 */

/**
 * Class represents Apache Commons logging logger,
 * it's definitions and usage for LogTranslator itself.
 */
public class CommonsLoggerLoader extends LoggerLoader {

    //    private static List<String> formattingSymbols = Arrays.asList("%b", "%h", "%s",  "%c", "%d", "%o", "%x", "%e", "%f", "%g", "%a", "%t");
    private static List<String> formattingSymbols = Arrays.asList("b", "h", "s", "c", "d", "o", "x", "e", "f", "g", "a", "t");
    private Collection translateLogMethods;
    private Collection checkerLogMethods;


    public CommonsLoggerLoader() {
        super();
//        org.apache.commons.logging.Log or org.apache.commons.logging.impl.Log4JLogger
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogFactory(imports.get(0));
        setLogger(imports.subList(1, imports.size()));


//        List<String> customCustomizedMethods = null; //Arrays.asList("asd");
        List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal");
        this.checkerLogMethods = generateCheckerMethods(levels);
        this.translateLogMethods = generateTranslateMethods(levels, null);
    }

    /**
     * Method extracts %ds formatters and substitutes them with underscore symbol.
     * Works for string formatter.
     *
     * @param methodText text to be formatted
     * @return text with substituted formatters by underscores
     */
    public static String isolateFormatters(String methodText, List<LogFile.Variable> formattedVariables) {
        List<String> formatters = new ArrayList<>();

        if (methodText.startsWith("String.format")) {
            methodText = methodText.substring("String.format".length(), methodText.length() - 1);
        } else if (methodText.contains("String.format")) {
            int startPos = methodText.indexOf("String.format");
            int endPos = methodText.lastIndexOf(")");
            methodText = methodText.substring(0, startPos) + methodText.substring(startPos + "String.format(".length(), endPos);
//            if (methodText.startsWith("\"")) {
//                methodText = methodText.substring(1);
//            }
        }
        String tempMethod = methodText;

        while (tempMethod.contains("%")) {
            StringBuilder formatter = new StringBuilder();
            int percPos = tempMethod.indexOf("%");
            Character c_prev = null;//tempMethod.charAt(percPos);
            for (Character c : tempMethod.substring(percPos).toLowerCase().toCharArray()) {
                // search for first character in Formatting symbols, followed by any non alphanum character
                formatter.append(c);
                if (Utils.listContainsItem(formattingSymbols, c.toString()) != null) {
                    formatters.add(formatter.toString());
                    tempMethod = tempMethod.substring(percPos + 2);
                    break;
                }

                if (c == ' ' || c == ',' || c == '"') {
                    tempMethod = tempMethod.substring(percPos + 1);
                    break;
                }

                if (c_prev != null) {
                    if (c == '%' && c_prev == '%') {
                        // escaping of '%' per cent
                        tempMethod = tempMethod.substring(percPos + 1);
                        break;
                    }
                }
                c_prev = c;
            }
        }
        int i = 0;
//        System.out.println("error=" + methodText);
        for (String frmt : formatters) {
            checkFormattedVarType(frmt, formattedVariables.get(i));
            methodText = methodText.replaceFirst(frmt, "~");
            i++;
        }

        List<String> formatted = new ArrayList<>();
        for (LogFile.Variable fvar : formattedVariables) {
            formatted.add(fvar.getName());
        }
        Collections.sort(formatted, new StringLengthComparator());
        for (String fvar : formatted) {
//            methodText = methodText.replace(fvar, "xx");
            methodText = methodText.replace(fvar, "");
        }
//        methodText = methodText.replaceAll(",*(xx),*", "~");

        return methodText;
    }

    private static void checkFormattedVarType(String frmt, LogFile.Variable variable) {
        if ((frmt.endsWith("d") || frmt.endsWith("o") || frmt.endsWith("x")) && !variable.getType().equals("int")) {
            variable.setType("int");
        } else if ((frmt.endsWith("e") || frmt.endsWith("f") || frmt.endsWith("g") || frmt.endsWith("a")) && !variable.getType().equals("float")) {
            variable.setType("float");
        }
    }

    public static boolean hasFormatters(String methodText) {
        List<String> formatters = new ArrayList<>();
        String tempMethod = methodText;

        while (tempMethod.contains("%")) {
            StringBuilder formatter = new StringBuilder();
            int percPos = tempMethod.indexOf("%");
            for (Character c : tempMethod.substring(percPos).toCharArray()) {
                // search for first character in Formatting symbols, followed by any non alphanum character
                formatter.append(c);
                if (Utils.listContainsItem(formattingSymbols, c.toString()) != null) {
                    formatters.add(formatter.toString());
                    tempMethod = tempMethod.substring(percPos + 2);
                    break;
                }
                if (c == ' ' || c == ',' || c == '"') {
                    tempMethod = tempMethod.substring(percPos + 1);
                    break;
                }
            }
        }
//        System.out.println("new=" + methodText);
        return !formatters.isEmpty();
    }

    public static List<String> getFormattingSymbols() {
        return formattingSymbols;
    }

    @Override
    public Collection getTranslateLogMethods() {
        return translateLogMethods;
    }

    @Override
    public Collection getCheckerLogMethods() {
        return checkerLogMethods;
    }

    @Override
    public String[] getFactoryInitializations() {
        return new String[]{"LogFactory.getInstance", "LogFactory.getLog"};
    }
}
