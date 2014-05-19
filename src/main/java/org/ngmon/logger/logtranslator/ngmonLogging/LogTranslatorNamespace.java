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

    public AbstractNamespace goMatchPatternError(int waveCounter, String newGoMatch, String originalLog) {
        return this;
    }

    public AbstractNamespace extending_search_file_isPackage(String extendingFileTosearch, boolean isPackage) {
        return this;
    }

    public AbstractNamespace null_variable(String variable) {
        return this;
    }

    public AbstractNamespace log_name(String name) {
        return this;
    }

    public AbstractNamespace new_namespace(String namespace) {
        return this;
    }

    public AbstractNamespace original_replacement_log(String originalLog, String ngmonLogReplacement) {
        return this;
    }

    public AbstractNamespace replacing(String original, String replacement) {
        return this;
    }

    public AbstractNamespace lookingFor(String variable) {
        return this;
    }

    public AbstractNamespace unableToDetermineMethodsArgument(String method) {
        return this;
    }

    public AbstractNamespace formal_parameters(String text) {
        return this;
    }

    public AbstractNamespace storing_array(String varName, String text) {
        return this;
    }

    public AbstractNamespace path(String text) {
        return this;
    }

    public AbstractNamespace assuming_external_variable_from_static_import(String text) {
        return this;
    }

    public AbstractNamespace lookingForInFile(String text, String filepath, int lineNumber) {
        return this;
    }

    public AbstractNamespace string_utils(String text) {
        return this;
    }

    public AbstractNamespace unableToChangeLogCallsLogFactoryNotDefined(String filepath, String text) {
        return this;
    }

    public AbstractNamespace translation_of_log_call_not_implemented(String text) {
        return this;
    }

    public AbstractNamespace log_type(String type) {
        return this;
    }

    public AbstractNamespace loggerloader_logFactory(String logFactory, String text) {
        return this;
    }

    public AbstractNamespace no_logging_framework(String actualLoggingFramework, String text) {
        return this;
    }

    public AbstractNamespace static_import(String staticImport) {
        return this;
    }

    public AbstractNamespace found(String javaFile) {
        return this;
    }

    public AbstractNamespace not_found_yet_digging_deeper(String fileNameFromImport) {
        return this;
    }

    public AbstractNamespace starting_antlr_on_file(String filepath, String fromFilePath) {
        return this;
    }

    public AbstractNamespace no_log_definition_files(int noLogDefinitionFilesSize) {
        return this;
    }
}
