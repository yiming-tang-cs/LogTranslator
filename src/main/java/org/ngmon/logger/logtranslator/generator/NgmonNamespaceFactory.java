package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.LogFile;

import java.util.*;


public class NgmonNamespaceFactory {

    private static Set<NamespaceFileCreator> namespaceFileCreatorSet = new HashSet<>();
    private static Map<String, Set<LogFile>> namespaceCreationMap = new TreeMap<>();

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
//        System.out.println(logFile.getNamespace() + "." + logFile.getNamespaceClass());
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
