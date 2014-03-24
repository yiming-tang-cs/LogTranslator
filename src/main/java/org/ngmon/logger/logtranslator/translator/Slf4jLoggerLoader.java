package org.ngmon.logger.logtranslator.translator;

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
    private List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error");

    public Slf4jLoggerLoader() {
        super();
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogger(imports.subList(1, imports.size()));
        setLogFactory(imports.get(0));
        // Does slf4j has any custom methods? Add them into this list.
//        List<String> slf4jCustomizedMethods = null;
//        this.translateLogMethods = generateTranslateMethods(levels, slf4jCustomizedMethods);
        this.translateLogMethods = generateTranslateMethods(levels, null);
        this.checkerLogMethods = generateCheckerMethods(levels);
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
        return new String[] {"LoggerFactory.getLogger"};
    }


}
