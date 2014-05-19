package org.ngmon.logger.logtranslator.translator;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ngmon.logger.logtranslator.antlr.JavaBaseListener;
import org.ngmon.logger.logtranslator.antlr.JavaLexer;
import org.ngmon.logger.logtranslator.antlr.JavaParser;
import org.ngmon.logger.logtranslator.common.LogFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class walks file, which is needed by LogTranslator,
 * when Java file is extended by other class and we are
 * looking for variable or method from this file.
 */
class HelperLogTranslator {

    /**
     * ANTLR parsing/walking of given file - LogFile object
     *
     * @param logFile to look up method
     * @param lookFor this method in ANTLR's run
     * @param methodArgumentTypes formal arguments of this methods
     * @return true if all passed well
     */
    private static boolean run(LogFile logFile, String lookFor, List<String> methodArgumentTypes) {
        InputStream antlrInputStream;
        MethodListener listener;
        try {
            antlrInputStream = new FileInputStream(logFile.getFilepath());
            ANTLRInputStream ais = new ANTLRInputStream(antlrInputStream);
            JavaLexer lexer = new JavaLexer(ais);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            ParseTree tree = parser.compilationUnit();

            ParseTreeWalker walker = new ParseTreeWalker();
            listener = new MethodListener(logFile, lookFor, methodArgumentTypes);
            walker.walk(listener, tree);
            return listener.isFound();

        } catch (Exception e) {
            System.err.println("helper: Unable to handle file=" + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decide whether to look for method with or without arguments
     *
     * @param logFile to look for in
     * @param methodName to look up in given logFile
     * @param typeArguments method formal arguments
     * @return true if method was found
     */
    public static boolean findMethod(LogFile logFile, String methodName, List<String> typeArguments) {
        // TODO log debug()
//        System.out.println("Searching for method=" + methodName);
        if (typeArguments == null) {
            return HelperLogTranslator.run(logFile, methodName, null);
        } else {
            return HelperLogTranslator.run(logFile, methodName, typeArguments);
        }
    }
}

/**
 * Implementation of ANTLR walker for HelperLogTranslator, when
 * looking for specific method in extending class.
 */
class MethodListener extends JavaBaseListener {
    private final List<String> argumentTypes;
    private LogFile logfile;
    private String findMethod;
    private boolean found;
    private List<String> foundMethodTypes = new ArrayList<>();

    MethodListener(LogFile logFile, String findMethod, List<String> argumentTypes) {
        this.argumentTypes = argumentTypes;
        this.findMethod = findMethod;
        this.logfile = logFile;
        found = false;
    }

    public boolean isFound() {
        return found;
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
                        String type = argumentTypes.get(i);
                        if (type.equals("Object")) {
                            // consider this type to be the same. skip iteration
                            correct++;
                            foundMethodTypes.add(fpc.type().getText());
                        } else if (type.equals(fpc.type().getText())) {
                            correct++;
                            foundMethodTypes.add(fpc.type().getText());
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

    /**
     * Determine method's return type and store method as variable.
     *
     * @param ctx ANTLR's method declaration context
     */
    void store(JavaParser.MethodDeclarationContext ctx) {
        String varType = (ctx.type() == null) ? "void" : ctx.type().getText();
        /** Store method as 'variable' and set its newNgmonName to variable - not method call) */
        logfile.storeVariable(ctx, findMethod, varType, false, ctx.Identifier().getText());
        found = true;
    }
}
