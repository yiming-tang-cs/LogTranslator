package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.LogFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

    public static void prepareNamespaces() {
        for (String key : namespaceCreationMap.keySet()) {
            for (LogFile lf : namespaceCreationMap.get(key)) {

                System.out.println(key + ":\t" + lf.getFilepath());
            }
        }

    }

    /**
     * Create new NgmonNamespace using templates and create it on appropriate location.
     *
     * @param logFile to be created new NgmonNamespace
     */
    private static void createNewNamespace(LogFile logFile) {
        NamespaceFileCreator nfc = new NamespaceFileCreator(logFile);
        namespaceFileCreatorSet.add(nfc);
        // create new java file from file path/namespace/package
        FileCreator.prepareNamespace(logFile, nfc);
    }


}
