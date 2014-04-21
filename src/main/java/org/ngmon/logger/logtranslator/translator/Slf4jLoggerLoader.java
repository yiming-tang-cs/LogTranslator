package org.ngmon.logger.logtranslator.translator;

import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    public static String isolateFormatters(String text, List<LogFile.Variable> formattedVariables) {
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
