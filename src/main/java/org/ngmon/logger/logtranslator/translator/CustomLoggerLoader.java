package org.ngmon.logger.logtranslator.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Class represents Custom logger,
 * it's definitions and usage for LogTranslator itself.
 *
 * It is mainly here to show, how easy it is to define own logging
 * methods. Logging framework has to still maintain default syntax
 * of logging. 'LOG.something(something);'
 */
public class CustomLoggerLoader extends LoggerLoader {

    private Collection translateLogMethods;
    private Collection checkerLogMethods;

    public CustomLoggerLoader() {
        super();
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogger(imports.subList(1, imports.size()));
        setLogFactory(imports.get(0));

        List<String> customCustomizedMethods = null; //Arrays.asList("asd");
        List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal");
        this.checkerLogMethods = generateCheckerMethods(levels);
        this.translateLogMethods = generateTranslateMethods(levels, customCustomizedMethods);
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
        return new String[] {"LogFactory.getLog"};
    }

}
