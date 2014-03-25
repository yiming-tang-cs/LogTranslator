package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.generator.FileCreator;
import org.ngmon.logger.logtranslator.generator.HelperGenerator;
import org.ngmon.logger.logtranslator.generator.NgmonNamespaceFactory;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.ngmon.logger.logtranslator.translator.ANTLRRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TranslatorStarter {

    private static List<LogFile> logFiles;
    private static Set<LogFile> nonLogLogFiles = new HashSet<>();
    private static List<LogFile> tempList = new ArrayList<>();
    private static int counter = 0;
    private static LogTranslatorNamespace LOG = Utils.getLogger();

    public static void main(String[] args) {
//        0) Initialize property file
        Utils.initialize();
//        1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal}
        logFiles = LogFilesFinder.commenceSearch(Utils.getApplicationHome());

// START OF DEBUGGING PURPOSES ONLY!
        for (LogFile lf : logFiles) {
            if (lf.getFilepath().equals("/home/mtoth/example-app-all/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/server/blockmanagement/BlockManager.java")) {
                tempList.add(lf);
        }}
// END OF DEBUGGING PURPOSES ONLY!

//        2) Find & set namespace. If new namespace, flush/write actual data into logFile
        HelperGenerator.generateNamespaces(logFiles);

        for (LogFile logFile : tempList) { // REMOVE DEBUGGING LINE ONLY!!
//        for (LogFile logFile : logFiles) {
//        3) Visit logFile
            if (!logFile.isFinishedParsing()) {
                // TODO both - info()
                LOG.startingParseFile(logFile.getFilepath());
                System.out.println("Starting " + logFile.getFilepath());
                ANTLRRunner.run(logFile, false, false);
                System.out.printf("Processed %d of %d files. Extra files parsed by extending %d.%n", counter - nonLogLogFiles.size(), logFiles.size(), nonLogLogFiles.size());
            }

            if (logFile.isFinishedParsing()) {
                // a) generate namespace if not already generated
                // b) fill with new log methods
                NgmonNamespaceFactory.addToNamespaceCreationMap(logFile);
            }
        }

        NgmonNamespaceFactory.prepareNamespaces();

        FileCreator.flush();
    }

    public static List<LogFile> getLogFiles() {
        return logFiles;
    }

    public static void addNonLogLogFile(LogFile logFile) {
        nonLogLogFiles.add(logFile);
    }

    /**
     * Before exiting of processed java file by ANTLR,
     * raise counter by one.
     */
    public static void addProcessedFilesCounter() {
        counter++;
    }
}
