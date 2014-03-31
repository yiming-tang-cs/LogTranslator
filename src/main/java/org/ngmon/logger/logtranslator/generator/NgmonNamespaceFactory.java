package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;

import java.util.*;


/**
 *  Factory for creation of NGMON's namespace files.
 */
public class NgmonNamespaceFactory {

    private static Set<NamespaceFileCreator> namespaceFileCreatorSet = new HashSet<>();
    private static Map<String, Set<LogFile>> namespaceCreationMap = new TreeMap<>();
    private static LogTranslatorNamespace LOG = Utils.getLogger();

    public static Set<NamespaceFileCreator> getNamespaceFileCreatorSet() {
        return namespaceFileCreatorSet;
    }

    /**
     * Add LogFile object to namespaceCreationMap. When all files are parsed, use this map
     * to create new files.
     *
     * @param logFile to add to namespaceCreationMap
     */
    public static void addToNamespaceCreationMap(LogFile logFile) {
        if (logFile == null) {
            throw new IllegalArgumentException("logFile is null!");
        }

        LOG.namespaceNamespaceClass(logFile.getNamespace(), logFile.getNamespaceClass()).trace();
        Set<LogFile> logFiles;
        if (namespaceCreationMap.containsKey(logFile.getNamespace()))  {
            logFiles = namespaceCreationMap.get(logFile.getNamespace());
        } else {
            logFiles = new TreeSet<>();
        }
        logFiles.add(logFile);
        namespaceCreationMap.put(logFile.getNamespace(), logFiles);
    }

    /**
     * Create new NgmonNamespace using templates and create it on appropriate location.
     */
    public static void createNamespaces() {
        for (String namespace : namespaceCreationMap.keySet()) {
            NamespaceFileCreator nfc = new NamespaceFileCreator(namespace, (TreeSet<LogFile>) namespaceCreationMap.get(namespace));
            namespaceFileCreatorSet.add(nfc);
        }
    }
}
