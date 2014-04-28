package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.generator.*;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.ngmon.logger.logtranslator.translator.ANTLRRunner;

import java.util.Set;
import java.util.TreeSet;

public class TranslatorStarter {

    protected static Set<LogFile> logFiles;
    private static Set<LogFile> tempList = new TreeSet<>();
    private static LogTranslatorNamespace LOG = Utils.getLogger();

    public static void main(String[] args) {
        String propertyFilePath = null;
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.contains(".properties")) {
                    propertyFilePath = arg;
                }
            }
        }
        LOG.startingLogTranslation(Statistics.startTiming()).debug();
        /** 0) Initialize property file */
        Utils.initialize(propertyFilePath);

        /** 1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal} */
        logFiles = LogFilesFinder.commenceSearch(Utils.getApplicationHome());

        System.out.println("Files to process: " + logFiles.size());
// START OF DEBUGGING PURPOSES ONLY!
        String testFile = "testdir1/ThreadUtil.java";
        if (logFiles.size() != 0) {
            for (LogFile lf : logFiles) {
                if (lf.getFilepath().contains(testFile)) {
//                    tempList.add(lf);
                }
            }
            if (tempList.size() != 0) {
                logFiles = tempList ;
            }
// END OF DEBUGGING PURPOSES ONLY!

            /** 2) Find & set namespaces. */
            NgmonNamespaceFactory.generateNamespaces(logFiles);

            /** 3) Visit each logFile and parse variables, imports, log definitions, methods
             Main part of this program */
            for (LogFile logFile : logFiles) {
                System.out.println(Statistics.counter + "  " + logFile.getFilepath());
                if (!logFile.isFinishedParsing()) {
                    LOG.antlrParsingFile(Statistics.counter, logFile.getFilepath()).debug();
                    ANTLRRunner.run(logFile, false, false);
                }
                if (logFile.isFinishedParsing()) {
                    // Add this file to namespaces map
                    NgmonNamespaceFactory.addToNamespaceCreationMap(logFile);
                }
            }

            /** 4) Rewrite files from logFiles - logs/imports by ANTLR */
            for (LogFile logFile : logFiles) {
                FileCreator.createFile(FileCreator.createPathFromString(logFile.getFilepath()), logFile.getRewrittenJavaContent());
                LOG.createdFile(logFile.getFilepath()).info();
            }

            /** 5) Create NGMON namespaces from associated parsed logFiles */
            NgmonNamespaceFactory.createNamespaces();

            /** 6) Create GoMatch patterns */
            GoMatchGenerator.createGoMatch(logFiles);

            /** 7) Create Maven project from generated files
             * After completion of this method, we should be able to install/package
             * created maven application and add as dependency to target's app pom.xml. */
            createLogTranslatorMavenProject();

            /** 8) Put all generated 'lines of code' below original log to one debug file */
            FileCreator.createFile(FileCreator.createPathFromString(Utils.debugOutputLocation), Utils.getOldNewLogList(logFiles));

            /** 9) Put GoMatch patterns into one file */
            FileCreator.createFile(FileCreator.createPathFromString(Utils.goMatchLocation), GoMatchGenerator.getGoMatchPatternListToString());
        }
        /** Print runtime lenght and simple statistics */
        System.out.println(Statistics.publishRunInfo());
    }

    /**
     * Creation of maven project structure from generated files and
     * creating and/or copying files to appropriate location in
     * target application's home.
     */
    private static void createLogTranslatorMavenProject() {
        /** Write NGMON namespaces on filesystem */
        FileCreator.flushNamespaces();

        /** Add "dummy" LogGlobal logger, which handles isXEnabled() -> true */
        LogGlobalGenerator.create();
        LOG.createdFile(LogGlobalGenerator.getPath()).info();

        /** Create SimpleLogger file - a bridge between NGMON logging and Log4j logging implementation */
        SimpleLoggerGenerator.create();
        LOG.createdFile(SimpleLoggerGenerator.getPath()).info();

        /** Create LogTranslator's default pom.xml to target location */
        LogTranslatorPom.create();
        LOG.createdFile(LogTranslatorPom.getPath()).info();
    }

    public static Set<LogFile> getLogFiles() {
        return logFiles;
    }


}
