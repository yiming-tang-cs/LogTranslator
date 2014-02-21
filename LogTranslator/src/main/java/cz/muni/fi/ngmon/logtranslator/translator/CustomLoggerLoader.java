package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.Properties;

public class CustomLoggerLoader extends LoggerLoader {
    public CustomLoggerLoader() {
        super();
        Properties props = super.getProperties();
        setLogger(props.getProperty("custom_logger"));
        setLogFactory(props.getProperty("custom_logfactory"));


    }
}
