package cz.muni.fi.ngmon.logtranslator.translator;

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
