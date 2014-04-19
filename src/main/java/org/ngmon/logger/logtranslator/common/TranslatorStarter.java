package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.generator.*;
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
        LOG.startingLogTranslation(start).debug();
        /** 0) Initialize property file */
        Utils.initialize();

        /** 1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal} */
        logFiles = LogFilesFinder.commenceSearch(Utils.getApplicationHome());

// START OF DEBUGGING PURPOSES ONLY!
        for (LogFile lf : logFiles) {
            if (lf.getFilepath().equals("/home/mtoth/tmp/rewritting/hadoop-common/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/org/apache/hadoop/yarn/webapp/Dispatcher.java")) {
                tempList.add(lf);
            }
        }
// END OF DEBUGGING PURPOSES ONLY!

        /** 2) Find & set namespaces. */
        NgmonNamespaceFactory.generateNamespaces(logFiles);

        /** 3) Visit each logFile and parse variables, imports, log definitions, methods
         Main part of this program */
//        for (LogFile logFile : tempList) { // REMOVE DEBUGGING LINE ONLY!!
        for (LogFile logFile : logFiles) {
            if (!logFile.isFinishedParsing()) {
                LOG.antlrParsingFile(logFile.getFilepath()).debug();
                ANTLRRunner.run(logFile, false, false);
            }

            if (logFile.isFinishedParsing()) {
                // Add this file to namespaces map
                NgmonNamespaceFactory.addToNamespaceCreationMap(logFile);
            }
        }

        System.out.printf("Changed %d log methods.%n", Statistics.getChangedLogMethodsCount());
        System.out.printf("\nProcessed %d of %d files. Extra files parsed by extending %d.%n%n", counter - nonLogLogFiles.size(), logFiles.size(), nonLogLogFiles.size());
        LOG.processed_log_and_extra_files(counter - nonLogLogFiles.size(), nonLogLogFiles.size()).debug();

        /** 4) Rewrite files from logFiles - logs/imports by ANTLR */
        for (LogFile logFile : logFiles) {
            // TODO() -- uncomment to work again!
//            FileCreator.createFile(FileCreator.createPathFromString(logFile.getFilepath()),
//                logFile.getRewrittenJavaContent());

            LOG.createdFile(logFile.getFilepath()).info();
//            System.out.println(logFile.getFilepath());
        }

        /** 5) Create NGMON namespaces from associated parsed logFiles */
        NgmonNamespaceFactory.createNamespaces();

        /** 6) Write NGMON namespaces on filesystem */
        FileCreator.flushNamespaces();

        /** 7) Add "dummy" LogGlobal logger, which handles isXEnabled() -> true */
        LogGlobalGenerator.create();
        LOG.createdFile(LogGlobalGenerator.path).info();
//        System.out.println("LogGlobal=" + LogGlobalGenerator.path);

        /** 8) Create SimpleLogger file - a bridge between NGMON logging and Log4j logging implementation */
        SimpleLoggerGenerator.create();
        LOG.createdFile(SimpleLoggerGenerator.path).info();
//        System.out.println("SimpleLogger=" + SimpleLoggerGenerator.path);

        /** 9) Just put all logs to one "random" file */
        FileCreator.createFile(FileCreator.createPathFromString("logs/ngmonold-newfiles"), Utils.getOldNewLogList(logFiles));
        long stop = System.currentTimeMillis();
        LOG.translationProcessFinishTime(((double) (stop - start) / 1000)).info();
//        System.out.println("Finished in " + ((double) (stop - start) / 1000) + " seconds.");

        /** 10) Put GoMatch patterns into one file */
        FileCreator.createFile(FileCreator.createPathFromString("logs/go-match.patterns"), GoMatchGenerator.getGoMatchPatternListToString());
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
