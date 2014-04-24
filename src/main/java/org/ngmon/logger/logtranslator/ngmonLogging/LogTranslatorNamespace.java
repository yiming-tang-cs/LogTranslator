package org.ngmon.logger.logtranslator.ngmonLogging;

import org.ngmon.logger.core.AbstractNamespace;


/**
 * Methods used by NGMON logging system in this current project.
 * This is not part of translator program, but example of NGMON
 * usage as example project.
 */
public class LogTranslatorNamespace extends AbstractNamespace {

    public AbstractNamespace antlrParsingFile(int fileNumber, String filepath) {
        return this;
    }

    public AbstractNamespace startingLogTranslation(long startTime) {
        return this;
    }

    public AbstractNamespace processed_log_and_extra_files(int logFiles, int extraFiles) {
        return this;
    }

    public AbstractNamespace createdFile(String filepath) {
        return this;
    }

    public AbstractNamespace translationProcessFinishTime(double seconds) {
        return this;
    }

    public AbstractNamespace locationDoesNotExists(String nonExistentLocation) {
        return this;
    }

    public AbstractNamespace skippingDirectoryTree(String directory) {
        return this;
    }

    public AbstractNamespace foundLogCall(String line, String file) {
        return this;
    }

    public AbstractNamespace fileError(String error) {
        return this;
    }

    public AbstractNamespace exception(String exceptionType, String message) {
        return this;
    }

    public AbstractNamespace writingNamespace(String filepath) {
        return this;
    }

    public AbstractNamespace unableToCreateDirectory(String directory) {
        return this;
    }


    public AbstractNamespace applicationNamespaceLength(int applicationNamespaceLength) {
        return this;
    }

    public AbstractNamespace emptyPackageNameInFile(String filepath) {
        return this;
    }


    public AbstractNamespace variablesInLog(String variables) {
        return this;
    }

    public AbstractNamespace replacementLogOriginalLog(String replacementLog, String originalLog) {
        return this;
    }

    public AbstractNamespace namespaceNamespaceClass(String namespaceWhole) {
        return this;
    }

    public AbstractNamespace deletedFile(String filepath) {
        return this;
    }

    public AbstractNamespace changedMethodsCount(int changedLogMethodsCount) {
        return this;
    }
}
