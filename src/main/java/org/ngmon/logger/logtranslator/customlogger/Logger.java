package org.ngmon.logger.logtranslator.customlogger;

public class Logger {

    private Class clazz;

    public Logger(Class clazz) {
        this.clazz = clazz;
    }


    public void MOUNT_NULLOP_CLIENT(Object client) {
        //return log(client);
    }

    public void MOUNT_MNT_PATH_CLIENT(Object path, Object client) {
       // return log(path, client);
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
