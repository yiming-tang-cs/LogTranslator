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
     * Generate few namespaces for this logging application. Resolve number of namespaces
     * for this app based on applicationNamespaceLength property and set them to LogFiles.
     *
     * @param logFileList input list of logFiles, which contain only filepath and package qualified name.
     * @return same list of logFiles, but each of them has filled appropriate namespace.
     */
    public static Set<LogFile> generateNamespaces(Set<LogFile> logFileList) {
        LOG.applicationNamespaceLength(Utils.getApplicationNamespaceLength()).trace();
        for (LogFile lf : logFileList) {
            if (lf.getPackageName() == null) {
                LOG.emptyPackageNameInFile(lf.getFilepath()).error();
//                System.err.println("null packageName in file " + lf.getFilepath());
            }
            String namespace = createNamespace(lf.getPackageName());
//            System.out.println("NS=" + namespace);
            lf.setNamespace(namespace);
        }

        return logFileList;
    }

    /**
     * Create NGMON log namespace which will contain all calls for this logs.
     * This method sets granularity level of NGMON log messages.
     * If original packageName length is longer then applicationNamespaceLength
     * property, make it shorter.
     *
     * @param packageName string to change
     * @return shortened packageName from NGMON length rules
     */
    private static String createNamespace(String packageName) {
        int numberOfDots = Utils.countOfSymbolInText(packageName, ".");

        if (numberOfDots < Utils.getApplicationNamespaceLength()) {
            return packageName;
        } else {
            StringBuilder newPackageName = new StringBuilder();
            String[] pckgs = packageName.split("\\.", Utils.getApplicationNamespaceLength() + 1);
            pckgs[pckgs.length - 1] = "";
            for (String p : pckgs) {
                if (!p.equals("")) newPackageName.append(p).append(".");
            }
            // remove last extra dot
            newPackageName.deleteCharAt(newPackageName.length() - 1);
            return newPackageName.toString();
        }
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

        LOG.namespaceNamespaceClass(logFile.getWholeNamespace()).trace();
        Set<LogFile> logFiles;
        if (namespaceCreationMap.containsKey(logFile.getWholeNamespace()))  {
            logFiles = namespaceCreationMap.get(logFile.getWholeNamespace());
        } else {
            logFiles = new TreeSet<>();
        }
        logFiles.add(logFile);
        namespaceCreationMap.put(logFile.getWholeNamespace(), logFiles);
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
