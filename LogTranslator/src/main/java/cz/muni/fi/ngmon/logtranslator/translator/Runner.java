package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaLexer;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Runner {
    static CommonTokenStream tokens;

    public static CommonTokenStream getTokens() {
        return tokens;
    }

    public static void main(String[] args) {
        LoggerLoader propLoader = new CustomLoggerLoader();
        InputStream antlrInputStream;
        // TODO change to fileName
        String file = propLoader.getTestingFile();

        try {
            antlrInputStream = new FileInputStream(file);
            ANTLRInputStream ais = new ANTLRInputStream(antlrInputStream);
            JavaLexer lexer = new JavaLexer(ais);
            tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            // create token rewriter using existing tokens
//            TokenStreamRewriter rewriter = new TokenStreamRewriter(tokens);
//            ParseTree tree = parser.compilationUnit();  -- difference??
            RuleContext tree = parser.compilationUnit();

//            System.out.println("ParseTree = " + tree.getText());
            ParseTreeWalker walker = new ParseTreeWalker();

            LogListener listener = new LogListener(tokens, file);
            walker.walk(listener, tree);

//            System.out.println("original=" + listener.getRewriter().toString());
//            System.out.println("modified=\n" + listener.getRewriter().getText());



        } catch (IOException e){
            System.err.println("Unable to handle file=" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
