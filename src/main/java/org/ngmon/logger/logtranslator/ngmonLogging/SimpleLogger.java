package org.ngmon.logger.logtranslator.ngmonLogging;

import org.apache.logging.log4j.LogManager;
import org.ngmon.logger.core.Logger;
import org.ngmon.logger.level.Level;
import org.ngmon.logger.util.JSONer;

import java.util.List;

/**
 * SimpleLogger class is used to log NGMON events and passes it to other (bridged) logging framework.
 * In our case, LogTranslator project uses Log4j2 as underlying logging framework.
 *
 * Class is not participating with LogTranslation process. It is used as an example logging framework
 * and handles various log events as would any other logging framework have done.
 */
public class SimpleLogger implements Logger {

    private org.apache.logging.log4j.Logger LOG4J_LOGGER = LogManager.getLogger(this);

//    @Override
//    public void log(String fqnNS, String methodName, List<String> tags, String[] paramNames, Object[] paramValues, int level) {
//        LOG4J_LOGGER.debug(JSONer.getEventJson(fqnNS, methodName, tags, paramNames, paramValues, level));
//    }

	@Override
	public void log(String fqnNS, String methodName, List<String> tags, String[] paramNames, Object[] paramValues,
			Level level) {
		LOG4J_LOGGER.debug(JSONer.getEventJson(fqnNS, methodName, tags, paramNames, paramValues, level));
	}
}