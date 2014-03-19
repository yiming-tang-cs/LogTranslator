package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.LogFilesFinder;
import cz.muni.fi.ngmon.logtranslator.common.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TranslatorStarter {

    private static List<LogFile> logFiles;
    private static Set<LogFile> nonLogLogFiles = new HashSet<>();
    private static List<LogFile> tempList = new ArrayList<>();

    public static void main(String[] args) {
//        0) Initialize property file
        Utils.initialize();
//        1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal}
        logFiles = LogFilesFinder.commenceSearch(Utils.getApplicationHome());


// START OF DEBUGGING PURPOSES ONLY!
        for (LogFile lf : logFiles) {
            if (lf.getFilepath().equals("/home/mtoth/example-app-all/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/HftpFileSystem.java")) {
                tempList.add(lf);
            }
        }
// END OF DEBUGGING PURPOSES ONLY!
//        2) Find & set namespace. If new namespace, flush/write actual data into logFile
        Utils.generateNamespaces(logFiles);
        int counter = 0;
//        for (LogFile logFile : tempList) { // REMOVE DEBUGGING ONLY!!
        for (LogFile logFile : logFiles) {
//        3) Visit logFile
            if (!logFile.isFinishedParsing()) {
                System.out.println("Starting " + logFile.getFilepath());
                ANTLRRunner.run(logFile, false);
                counter++;
                System.out.printf("Processed %d of %d files.%n", counter, logFiles.size());
            }
        }
    }

    public static List<LogFile> getLogFiles() {
        return logFiles;
    }

    public static void addNonLogLogFile(LogFile logFile) {
        nonLogLogFiles.add(logFile);
    }

}
