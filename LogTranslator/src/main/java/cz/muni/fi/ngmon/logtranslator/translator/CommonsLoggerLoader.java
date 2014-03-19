package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/*
    http://commons.apache.org/proper/commons-logging/apidocs/index.html
    Commons Logging
    http://commons.apache.org/proper/commons-logging/apidocs/org/apache/commons/logging/Log.html
    http://commons.apache.org/proper/commons-logging/apidocs/org/apache/commons/logging/impl/Log4JLogger.html
 */

public class CommonsLoggerLoader extends LoggerLoader {

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
        return new String[] {"LogFactory.getInstance", "LogFactory.getLog"};
    }
}
