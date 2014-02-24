package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class CustomLoggerLoader extends LoggerLoader {




    public CustomLoggerLoader() {
        super();
        Properties props = super.getProperties();
        setLogger(props.getProperty("custom_logger"));
        setLogFactory(props.getProperty("custom_logfactory"));


    }

    @Override
    public List<String> getAvailableLogMethods() {
        return Arrays.asList("trace", "debug", "info", "warn", "error", "fatal");
    }
}
