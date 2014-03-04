package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.common.FileInfo;
import cz.muni.fi.ngmon.logtranslator.common.LogFilesFinder;
import cz.muni.fi.ngmon.logtranslator.common.Utils;

import java.nio.file.Path;
import java.util.Map;

public class TranslatorStarter {

    public static void main(String[] args) {
//        0) Initialize property file
        Utils.initialize();
//        1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal}
        Map<Path, String> javaLogFiles = LogFilesFinder.startSearchIn(Utils.getLoggingApplicationHome());
        String namespace;

        for (Path file : javaLogFiles.keySet()) {
            FileInfo fileInfo = new FileInfo(file.toString());
//        2) Find & set namespace
            // generate namespaces for classes and use them
            namespace = javaLogFiles.get(file);
            fileInfo.setNamespace(namespace);
//        3) If new namespace, flush/write actual data into file
//        4) Visit file
            System.out.println("processing " + file);
            ANTLRRunner.run(fileInfo);
        }
//        5)

        // TODO change to fileNames

//        fileInfo = new FileInfo("/home/mtoth/skola/dp/LogFilterBase/LogTranslator/src/main/resources/sourceExamples/TestingClass.java");
//        fileInfo = new FileInfo("/home/mtoth/skola/dp/LogFilterBase/LogTranslator/src/main/resources/sourceExamples/WebApp.java");
//        ANTLRRunner.run(fileInfo);

    }
}
