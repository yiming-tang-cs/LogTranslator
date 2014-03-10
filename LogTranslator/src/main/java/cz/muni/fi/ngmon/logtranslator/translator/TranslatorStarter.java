package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.LogFilesFinder;
import cz.muni.fi.ngmon.logtranslator.common.Utils;

import java.util.List;

public class TranslatorStarter {

    public static void main(String[] args) {
//        0) Initialize property file
        Utils.initialize();
//        1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal}
        List<LogFile> logFiles = LogFilesFinder.commenceSearch(Utils.getLoggingApplicationHome());
//        logFiles = Arrays.asList(new LogFile("/home/mtoth/example-app/hadoop-hdfs-project/hadoop-hdfs/src/main/java/org/apache/hadoop/hdfs/DFSClient.java"));
//        2) Find & set namespace. If new namespace, flush/write actual data into logFile
        Utils.generateNamespaces(logFiles);
        int counter = 0;
        for (LogFile logFile : logFiles) {
//        3) Visit logFile
            ANTLRRunner.run(logFile);
            counter++;
            System.out.printf("Processed %d of %d files.%n", counter, logFiles.size());
        }
    }
}
