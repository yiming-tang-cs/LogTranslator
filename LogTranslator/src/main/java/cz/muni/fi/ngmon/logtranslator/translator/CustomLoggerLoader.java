package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class CustomLoggerLoader extends LoggerLoader {

    private Collection translateLogMethods;
    private Collection checkerLogMethods;
    private List<String> levels = Arrays.asList("trace", "debug", "info", "warn", "error", "fatal");

    public CustomLoggerLoader() {
        super();
        List<String> imports = LoggerFactory.getActualLoggingImports();
        setLogger(imports.subList(1, imports.size()));
        setLogFactory(imports.get(0));

        List<String> customCustomizedMethods = null; //Arrays.asList("asd");
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
