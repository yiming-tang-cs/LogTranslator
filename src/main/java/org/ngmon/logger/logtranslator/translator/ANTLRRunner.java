package org.ngmon.logger.logtranslator.translator;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ngmon.logger.logtranslator.antlr.JavaLexer;
import org.ngmon.logger.logtranslator.antlr.JavaParser;
import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ANTLRRunner {
    static CommonTokenStream tokens;
    private static LogFile currentFile;
    private static LogTranslatorNamespace LOG = Utils.getLogger();

    public static void run(LogFile logFile, boolean ignoreLogStatements, boolean isExtendingClass) {
        currentFile = logFile;
        String file = logFile.getFilepath();
        InputStream antlrInputStream;


        try {
            // -- ANTLR part --
            antlrInputStream = new FileInputStream(file);
            ANTLRInputStream ais = new ANTLRInputStream(antlrInputStream);
            JavaLexer lexer = new JavaLexer(ais);
            tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            RuleContext tree = parser.compilationUnit();
            ParseTreeWalker walker = new ParseTreeWalker();
            LogTranslator listener = new LogTranslator(tokens, logFile, ignoreLogStatements, isExtendingClass);
            walker.walk(listener, tree);
            logFile.setRewrittenJavaContent(listener.getRewriter().getText());

        } catch (IOException e){
            LOG.fileError(e.toString()).error();
//            System.err.println("Unable to handle file=" + e.toString());
        } catch (NullPointerException exc) {
            LOG.exception("NullPointerException", logFile.getFilepath()).error();
//            System.err.println("NullPointerException! " + logFile.getFilepath());
            exc.printStackTrace();
            System.exit(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LogFile getCurrentFile() {
        return currentFile;
    }
}
