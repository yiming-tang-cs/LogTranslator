package org.ngmon.logger.logtranslator.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class LoggerLoader {

    private List<String> logger;
    private String logFactory;


    public List<String> getLogger() {
        return logger;
    }

    public void setLogger(List<String> logger) {
        this.logger = logger;
    }

    public String getLogFactory() {
        return logFactory;
    }

    public void setLogFactory(String logFactory) {
        this.logFactory = logFactory;
    }


    public abstract Collection getTranslateLogMethods();

    public abstract Collection getCheckerLogMethods();

    public abstract String[] getFactoryInitializations();


    public Collection<String> generateTranslateMethods(List<String> levels, List<String> customMethods) {
        List<String> methods = new ArrayList<>();
        for (String level : levels) {
            methods.add(level);
        }
        if (customMethods != null) {
            for (String customMethod : customMethods) {
                methods.add(customMethod);
            }
        }
        return Collections.unmodifiableCollection(methods);
    }

    /**
     * Method generates 'checker methods' for given logging system.
     * Checker methods are in following design 'isLevelEnabled'.
     * Level is passed into this method by parameter levels.
     *
     * @param levels list of levels to generate methods from
     * @return collection of generated checker methods
     */
    public Collection generateCheckerMethods(List<String> levels) {
        List<String> list = new ArrayList<>(levels.size());
        for (String level : levels) {
            level = (level.length() > 0) ? Character.toUpperCase(level.charAt(0)) + level.substring(1) : "";
            list.add("is" + level + "Enabled");
        }
        return Collections.unmodifiableCollection(list);
    }

    public boolean containsLogFactory(String declaration) {
        for (String factory : this.getFactoryInitializations()) {
            if (declaration.contains(factory)) {
                return true;
            }
        }
        return false;
    }


}
