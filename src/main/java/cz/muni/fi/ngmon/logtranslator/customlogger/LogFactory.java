package cz.muni.fi.ngmon.logtranslator.customlogger;

public class LogFactory {

    public static Logger getLog(Class clazz) {
        return new Logger(clazz);
    }

    public void info(Object... objects) {
    }

    public void log(Object... objects) {

    }

    public void debug(Object... objects) {

    }

    public void trace(Object... objects) {

    }

    public boolean isDebugEnabled() {
        return true;
    }
}
