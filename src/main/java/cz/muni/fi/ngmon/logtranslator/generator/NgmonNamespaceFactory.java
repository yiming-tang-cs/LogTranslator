package cz.muni.fi.ngmon.logtranslator.generator;

import cz.muni.fi.ngmon.logtranslator.common.LogFile;

import java.util.HashSet;
import java.util.Set;

public class NgmonNamespaceFactory {

    private static Set<NamespaceFileCreator> namespaceFileCreatorSet = new HashSet<>();


    /**
     * Decide whether to create new ngmon namespace or just add only methods from
     * this logFile to already created NgmonNamespace file.
     *
     * @param logFile to create ngmon namespace file from or append new methods
     */

    // TODO fix this
    public static void addLogToNgmonNamespace(LogFile logFile) {
        if (logFile == null) {
            throw new IllegalArgumentException("logFile is null!");
        }

        boolean added = false;
        for (NamespaceFileCreator nfc : namespaceFileCreatorSet) {
            if (nfc.getLogFileList().contains(logFile)) {
                nfc.addMethodsToNamespace(logFile);
                added = true;
            }
        }
        if (!added) {
            createNewNamespace(logFile);
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
        FileCreator.create(logFile, nfc);
    }

}
