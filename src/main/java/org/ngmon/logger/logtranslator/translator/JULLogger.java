package org.ngmon.logger.logtranslator.translator;

import java.util.Collection;

/**
 * Class represents Java Util logging logger,
 * it's definitions and usage for LogTranslator itself.
 */
public class JULLogger extends LoggerLoader {
    // TODO not implemented! as it has not been used in Apache Hadoop

    @Override
    public Collection getTranslateLogMethods() {
        return null;
    }

    @Override
    public Collection<String> getCheckerLogMethods() {
        return null;
    }

    @Override
    public String[] getFactoryInitializations() {
        return new String[] {"Logger.getLogger"};
    }
}
