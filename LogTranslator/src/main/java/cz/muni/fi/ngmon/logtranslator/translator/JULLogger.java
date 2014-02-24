package cz.muni.fi.ngmon.logtranslator.translator;

import java.util.Collection;

public class JULLogger extends LoggerLoader {

    // TODO not implemented!


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
