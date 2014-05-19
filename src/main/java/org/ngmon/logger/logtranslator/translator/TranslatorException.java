package org.ngmon.logger.logtranslator.translator;

/**
 * TranslatorException not used for now, but
 * can be used in future, for some special cases.
 */

public class TranslatorException extends Exception  {

    public TranslatorException() {
        super();
    }

    public TranslatorException(String message) {
        super(message);
    }

    public TranslatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
