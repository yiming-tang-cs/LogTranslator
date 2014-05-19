package org.ngmon.logger.logtranslator.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

    /*
        log4j   1.2
        http://logging.apache.org/log4j/1.2/apidocs/index.html

        log4j2  2.0
        http://logging.apache.org/log4j/2.x/manual/api.html
        http://logging.apache.org/log4j/2.x/log4j-api/apidocs/index.html
     */
/**
 * Class represents Apache Log4j logger,
 * it's definitions and usage for LogTranslator itself.
 */
public class Log4jLoggerLoader extends LoggerLoader {

    private Collection<String> translateLogMethods;
    private Collection checkerLogMethods;

    public Log4jLoggerLoader() {
        super();
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogger(imports.subList(1, imports.size()));
        setLogFactory(imports.get(0));  // take care, log4j has no logFactory

        // Does log4j has any custom methods? Add them into this list.
        List<String> log4jCustomizedMethods = Arrays.asList("log", "entry", "printf");
        List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal", "");
        this.checkerLogMethods = generateCheckerMethods(levels);
        this.translateLogMethods = generateTranslateMethods(levels, log4jCustomizedMethods);
    }

    @Override
    public Collection getTranslateLogMethods() {
        return translateLogMethods;
    }

    @Override
    public Collection getCheckerLogMethods() {
        return checkerLogMethods;
    }

    public String[] getFactoryInitializations() {
        String[] factories;
        if (LoggerFactory.getActualLoggingFramework().equals("log4j")) {
            factories = new String[]{"Logger.getRootLogger", "Logger.getLogger", "LogFactory.getLog"};
        } else {
            // log4j2
            factories = new String[]{"LogManager.getLogger"};
        }
        return factories;
    }

}
