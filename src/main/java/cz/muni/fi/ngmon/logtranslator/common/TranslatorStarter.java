package cz.muni.fi.ngmon.logtranslator.common;

import cz.muni.fi.ngmon.logtranslator.generator.FileCreator;
import cz.muni.fi.ngmon.logtranslator.generator.HelperGenerator;
import cz.muni.fi.ngmon.logtranslator.generator.NgmonNamespaceFactory;
import cz.muni.fi.ngmon.logtranslator.ngmonLogging.LogTranslatorNamespace;
import cz.muni.fi.ngmon.logtranslator.translator.ANTLRRunner;

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
            if (lf.getFilepath().equals("/home/mtoth/example-app-all/hadoop-common-project/hadoop-auth-examples/src/main/java/org/apache/hadoop/security/authentication/examples/RequestLoggerFilter.java")) {
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
                NgmonNamespaceFactory.addLogToNgmonNamespace(logFile);
            }
        }

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
