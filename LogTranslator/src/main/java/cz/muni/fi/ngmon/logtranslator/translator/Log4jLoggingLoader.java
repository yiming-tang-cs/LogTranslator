package cz.muni.fi.ngmon.logtranslator.translator;

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


public class Log4jLoggingLoader extends LoggerLoader {


    private Collection<String> translateLogMethods;
    private Collection checkerLogMethods;
    private List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal");

    public Log4jLoggingLoader() {
        super();
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogger(imports.subList(1, imports.size()));
        setLogFactory(imports.get(0));  // take care, log4j has no logFactory

        // Does log4j has any custom methods? Add them into this list.
        List<String> log4jCustomizedMethods = Arrays.asList("log", "entry", "printf");
        this.checkerLogMethods = generateCheckerMethods(levels);
        this.translateLogMethods = generateTranslateMethods(levels, log4jCustomizedMethods);
        // TODO translate or remove isEnabled method checker!
        this.translateLogMethods.add("isEnabled");

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













