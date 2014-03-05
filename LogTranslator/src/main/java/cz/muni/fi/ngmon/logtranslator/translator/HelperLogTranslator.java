package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaLexer;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HelperLogTranslator {


    public static void run(LogFile logFile, String lookFor) {
        InputStream antlrInputStream;
        try {
            antlrInputStream = new FileInputStream(logFile.getFilepath());
            ANTLRInputStream ais = new ANTLRInputStream(antlrInputStream);
            JavaLexer lexer = new JavaLexer(ais);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
//            ParseTree tree = parser.compilationUnit();  -- difference??
            RuleContext tree = parser.compilationUnit();

//            System.out.println("ParseTree = " + tree.getText());
            ParseTreeWalker walker = new ParseTreeWalker();
            MethodListener listener = new MethodListener(logFile, lookFor);
            walker.walk(listener, tree);

            //  System.out.println("modified=\n" + listener.getRewriter().getText());


        } catch (IOException e) {
            System.err.println("Unable to handle file=" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class MethodListener extends JavaBaseListener {
    private LogFile logfile;
    private String method;
    private String formalParameters;
    private String varType;
    private String findMethod;

    MethodListener(LogFile logFile, String findMethod) {
        this.findMethod = findMethod;
        this.logfile = logFile;
        this.method = findMethod.substring(0, findMethod.indexOf('('));
        this.formalParameters = findMethod.substring(findMethod.indexOf('('));
        // remove all spaces in formalParameters - much easier to find now
        formalParameters = formalParameters.replace(" ", "");

    }

    @Override
    public void exitMethodDeclaration(@NotNull JavaParser.MethodDeclarationContext ctx) {
        if ((method.equals(ctx.Identifier().getText())) &&
                (formalParameters.equals(ctx.formalParameters().getText()))) {

            System.out.printf("%s%s -> Method=%s %s%s %n", method, formalParameters, ctx.type().getText(),
                    ctx.Identifier().getText(), ctx.formalParameters().getText());

            varType = (ctx.type() == null) ? varType = "void" :  ctx.type().getText();
            System.out.println("Storing" + findMethod + " " + varType);
            logfile.storeVariable(ctx, findMethod, varType, false);
        }

    }
}
