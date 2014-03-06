package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaLexer;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HelperLogTranslator {


    public static void run(LogFile logFile, String lookFor, List<String> methodArgumentTypes) {
        InputStream antlrInputStream;
        try {
            antlrInputStream = new FileInputStream(logFile.getFilepath());
            ANTLRInputStream ais = new ANTLRInputStream(antlrInputStream);
            JavaLexer lexer = new JavaLexer(ais);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            ParseTree tree = parser.compilationUnit();

            ParseTreeWalker walker = new ParseTreeWalker();
            MethodListener listener = new MethodListener(logFile, lookFor, methodArgumentTypes);
            walker.walk(listener, tree);

        } catch (IOException e) {
            System.err.println("Unable to handle file=" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lookMethod(LogFile logFile, String methodName, List<String> typeArguments) {
        if (typeArguments == null) {
            HelperLogTranslator.run(logFile, methodName, null);
        } else {
            HelperLogTranslator.run(logFile, methodName, typeArguments);
        }
    }
}


class MethodListener extends JavaBaseListener {
    private final List<String> argumentTypes;
    private LogFile logfile;
    private String findMethod;

    MethodListener(LogFile logFile, String findMethod, List<String> argumentTypes) {
        this.argumentTypes = argumentTypes;
        this.findMethod = findMethod;
        this.logfile = logFile;
    }

    /**
     * Look for method declarations in same file. If method definition is same
     * as one we are looking for, find out return type and store it all into
     * variable list.
     *
     * @param ctx ANTLR's method declaration context
     */
    @Override
    public void exitMethodDeclaration(@NotNull JavaParser.MethodDeclarationContext ctx) {
        if ((ctx.formalParameters().formalParameterList() != null)) {
            if (argumentTypes != null) {
                // if formalArguments count in method == argumentTypes.size() - proceed
                if (((int) Math.ceil(ctx.formalParameters().formalParameterList().getChildCount() / 2.0)) == argumentTypes.size()) {
                    int i = 0;
                    int correct = 0;
                    for (JavaParser.FormalParameterContext fpc : ctx.formalParameters().formalParameterList().formalParameter()) {
                        if (argumentTypes.get(i).equals(fpc.type().getText())) {
                            correct++;
                        }
                        i++;
                    }
                    if (i == correct) {
                        // Found corresponding method, store it.
                        store(ctx);
                    }
                }
            }
        } else {
            if (ctx.Identifier().getText().equals(findMethod.substring(0, findMethod.indexOf('(')))) {
//                System.out.println("storing " + ctx.Identifier().getText() + ctx.formalParameters().getText());
                store(ctx);
            }
        }
    }

    public void store(JavaParser.MethodDeclarationContext ctx) {
        String varType = (ctx.type() == null) ? varType = "void" : ctx.type().getText();
//        System.out.println("Storing " + findMethod + " " + varType);
        logfile.storeVariable(ctx, findMethod, varType, false);
    }
}
