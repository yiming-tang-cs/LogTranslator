package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.Arrays;
import java.util.List;

public class Log4jLoggingLoader extends LoggerLoader {

    public Log4jLoggingLoader() {
        //log4j_logger=org.org.apache.log4j.Logger
        //log4j_logfactory=org.org.apache.log4j
    }

    @Override
    public List<String> getAvailableLogMethods() {
        // TODO
        return Arrays.asList("trace", "debug", "info", "warn", "error", "fatal");
    }
}
