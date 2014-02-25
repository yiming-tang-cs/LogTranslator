package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.ANTLRRunner;

public class TranslatorStarter {

    private static FileInfo fileInfo;

    public static void main(String[] args) {
//        LoggerLoader propLoader = new CustomLoggerLoader();

        // Search through all ".java" files in given directory. Look for "log.{debug,warn,error,fatal}

        // TODO change to fileNames
        fileInfo = new FileInfo("/home/mtoth/skola/dp/LogFilterBase/LogTranslator/src/main/resources/sourceExamples/TestingClass.java");
        fileInfo = new FileInfo("/home/mtoth/skola/dp/LogFilterBase/LogTranslator/src/main/resources/sourceExamples/WebApp.java");
        ANTLRRunner.run(fileInfo);

    }
}
