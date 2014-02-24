package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonsLoggingLoader extends LoggerLoader {

    // Use org.apache.commons.logging.* imports
    private List<String> availableLogMethods = new ArrayList<>(
            Arrays.asList("trace", "debug", "info", "warn", "error", "fatal"));

    public CommonsLoggingLoader(List<String> availableLogMethods) {
        super();
//        commons_logger=org.apache.commons.logging.Log
//        commons_logfactory=org.apache.commons.logging.LogFactory
    }

    @Override
    public List<String> getAvailableLogMethods() {
        return availableLogMethods;
    }
}
