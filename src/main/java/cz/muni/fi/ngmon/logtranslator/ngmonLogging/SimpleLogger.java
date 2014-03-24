package cz.muni.fi.ngmon.logtranslator.ngmonLogging;

import org.apache.logging.log4j.LogManager;
import org.ngmon.logger.core.Logger;
import org.ngmon.logger.util.JSONer;

import java.util.List;


public class SimpleLogger implements Logger {

    private org.apache.logging.log4j.Logger log = LogManager.getLogger("Log4jLogger");

    //    public void log(String methodName, List<String> tags, String[] paramNames, Object[] paramValues, int level) {
//        log.debug(JSONer.getEventJson(null, methodName, tags, paramNames, paramValues, level));
//    }

    public void log(String fqnNS, String methodName, List<String> tags, String[] paramNames, Object[] paramValues, int level) {
        log.debug(JSONer.getEventJson(fqnNS, methodName, tags, paramNames, paramValues, level));
    }


}