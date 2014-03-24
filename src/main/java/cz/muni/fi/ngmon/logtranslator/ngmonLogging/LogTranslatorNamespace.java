package cz.muni.fi.ngmon.logtranslator.ngmonLogging;

import org.ngmon.logger.core.AbstractNamespace;


public class LogTranslatorNamespace extends AbstractNamespace {


    public AbstractNamespace event1(String param1, int param2) {
        return this;
    }

    public AbstractNamespace methodDeclarationFailed(String lala, int i, long l) {
        return this;
    }

    public AbstractNamespace startingParseFile(String filepath) {
        System.out.println("=====HERE!!!=====");
        return this;
    }
}
