package org.ngmon.logger.logtranslator.translator;

import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.generator.StringLengthComparator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    http://www.slf4j.org/api/org/slf4j/Logger.html
    .getName();  --> remove such calling
    final static Logger logger = LoggerFactory.getLogger(Wombat.class);
    logger.debug("Temperature set to {}. Old temperature was {}.", t, oldT);
    logger.info("Temperature has risen above 50 degrees.");
*/

public class Slf4jLoggerLoader extends LoggerLoader {

    private Collection translateLogMethods;
    private Collection checkerLogMethods;

    public Slf4jLoggerLoader() {
        super();
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogger(imports.subList(1, imports.size()));
        setLogFactory(imports.get(0));
        // Does slf4j has any custom methods? Add them into this list.
//        List<String> slf4jCustomizedMethods = null;
//        this.translateLogMethods = generateTranslateMethods(levels, slf4jCustomizedMethods);
        List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error");
        this.translateLogMethods = generateTranslateMethods(levels, null);
        this.checkerLogMethods = generateCheckerMethods(levels);
    }

    /**
     * Formatting symbol can be either {} or {0} - meaning numbered formatting.
     *
     * @param text
     * @param formattedVariables
     * @param symbol
     * @return
     */
    public static String isolateFormatters(String text, List<LogFile.Variable> formattedVariables, String symbol) {
        if (text.startsWith("(MessageFormat.format")) {
            text = text.substring(0, text.length() - 1).replace("(MessageFormat.format", "");
        }
        if (formattedVariables.size() != 0 && text.contains("{}")) {
            int brackets = Utils.countOfSymbolInText(text, "{}");
            StringBuilder pattern = new StringBuilder();
            for (int i = 0; i < formattedVariables.size(); i++) {
                if (i == 0) {
                    pattern.append("~");
                } else {
                    pattern.append(" ~");
                }
            }
            // find first comma, delimiting 1st and 2nd+ argument
            text = text.substring(0, text.lastIndexOf(formattedVariables.get(0).getName()));
            text = text.substring(0, text.lastIndexOf(","));
            if (brackets > 1) {
                pattern.replace(0, pattern.length(), "~");
            }
            text = text.replaceAll("\\{\\}", pattern.toString());

        } else if (formattedVariables.size() != 0 && text.contains("{0}")) {
            List<String> formattedVars = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\{\\d+\\}");
            Matcher matcher = pattern.matcher(text);


            for (int i = 0; matcher.find(); i++) {
                String varName = formattedVariables.get(i).getName();
                formattedVars.add(varName);
                text = text.replace(matcher.group(), "~");
            }

            Collections.sort(formattedVars, new StringLengthComparator());
            for (String var : formattedVars) {
                text = text.replace(var, "");
            }


        }
        return text;
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
        return new String[]{"LoggerFactory.getLogger"};
    }
}
