package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.common.FileInfo;
import cz.muni.fi.ngmon.logtranslator.common.Utils;

public class TranslatorStarter {

    private static FileInfo fileInfo;

    public static void main(String[] args) {
//        LoggerLoader propLoader = new CustomLoggerLoader();

//        0) Initialize property file
          Utils.initialize();
//        1) Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal}
//        2) Find & set namespace
//        3) Visit file

        // TODO change to fileNames
        fileInfo = new FileInfo("/home/mtoth/skola/dp/LogFilterBase/LogTranslator/src/main/resources/sourceExamples/TestingClass.java");
        fileInfo = new FileInfo("/home/mtoth/skola/dp/LogFilterBase/LogTranslator/src/main/resources/sourceExamples/WebApp.java");
        ANTLRRunner.run(fileInfo);

    }
}
