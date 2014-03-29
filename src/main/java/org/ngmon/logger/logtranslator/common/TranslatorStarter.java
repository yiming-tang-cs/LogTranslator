package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.generator.FileCreator;
import org.ngmon.logger.logtranslator.generator.HelperGenerator;
import org.ngmon.logger.logtranslator.generator.LogGlobalGenerator;
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
        long start = System.currentTimeMillis();
        /** 0) Initialize property file */
        Utils.initialize();

        /** 1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal} */
        logFiles = LogFilesFinder.commenceSearch(Utils.getApplicationHome());

// START OF DEBUGGING PURPOSES ONLY!
        for (LogFile lf : logFiles) {
            if (lf.getFilepath().equals("/home/mtoth/example-app-all/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/conf/Configuration.java")) {
                tempList.add(lf);
            }
        }
// END OF DEBUGGING PURPOSES ONLY!

        /** 2) Find & set namespaces. */
        HelperGenerator.generateNamespaces(logFiles);

        /** 3) Visit each logFile and parse variables, imports, log definitions, methods
             Main part of this program */
//        for (LogFile logFile : tempList) { // REMOVE DEBUGGING LINE ONLY!!
        for (LogFile logFile : logFiles) {
            if (!logFile.isFinishedParsing()) {
                // TODO log.info()
                LOG.startingParseFile(logFile.getFilepath());
                ANTLRRunner.run(logFile, false, false);
            }

            if (logFile.isFinishedParsing()) {
                // Add this file to namespaces map
                NgmonNamespaceFactory.addToNamespaceCreationMap(logFile);
            }
        }
        System.out.printf("\nProcessed %d of %d files. Extra files parsed by extending %d.%n%n", counter - nonLogLogFiles.size(), logFiles.size(), nonLogLogFiles.size());

        /** 4) Rewrite files from logFiles - logs/imports by ANTLR */
        for (LogFile logFile : logFiles) {
            FileCreator.createFile(FileCreator.createPathFromString(logFile.getFilepath()),
                    logFile.getRewrittenJavaContent());
            System.out.println("R=" + logFile.getFilepath());
        }

        /** 5) Create NGMON namespaces from associated parsed logFiles */
        NgmonNamespaceFactory.createNamespaces();

        /** 6) Write NGMON namespaces on filesystem */
        FileCreator.flushNamespaces();

        /** 7) Add "dummy" LogGlobal logger, which handles isXEnabled() -> true */
        LogGlobalGenerator.create();
        System.out.println("LogGlobal=" + LogGlobalGenerator.path);

        /** 8) Just put all logs to one "random" file */
        FileCreator.createFile(FileCreator.createPathFromString("/tmp/ngmonold-newfiles"), Utils.getOldNewLogList());
        long stop = System.currentTimeMillis();
        System.out.println("Finished in " + ((double) (stop - start) / 1000) + " seconds.");
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
