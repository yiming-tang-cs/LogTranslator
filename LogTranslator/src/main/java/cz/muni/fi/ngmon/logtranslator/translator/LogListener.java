package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.ANTLRRunner;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.NotNull;
import java.util.Map;


public class LogListener extends JavaBaseListener {
    //    BufferedTokenStream bufferedTokens; // intended to be used with multiple channels for handling WHITESPACES and COMMENTS
    static LoggerLoader loggerLoader = new CustomLoggerLoader();
    Variable var;
    TokenStreamRewriter rewriter;
    private String logName = null; // reference to original LOG variable name
    private String logType = null; // reference to original LOG variable type


    public LogListener(BufferedTokenStream tokens, String filename) {
        rewriter = new TokenStreamRewriter(tokens);
        var = new Variable();
        var.setFileName(filename);
//        rewriter.getTokenStream();
//        List<Token> cmtChannel = tokens.getHiddenTokensToRight(0, 1);
    }

    public TokenStreamRewriter getRewriter() {
        return rewriter;
    }

    public String getLogType() {
        if (logType == null) {
            return null;
        } else {
            // return only last part of QN
            return logType.substring(logType.lastIndexOf(".") + 1);
        }
    }

    void checkAndStoreVariable(String variableName, String variableType, int lineNumber,
                               int lineStartPosition, int lineStopPosition, int startPosition, int stopPosition) {
        Variable.Properties p = var.new Properties();

        if (variableName == null || variableType == null) {
            throw new NullPointerException("Variable name or type are null!");
        } else {
            p.setName(variableName);
            p.setType(variableType);
        }

        p.setLineNumber(lineNumber);
        p.setStartPosition(lineStartPosition);
        p.setStopPosition(lineStopPosition);
        p.setFileStartPosition(startPosition);
        p.setFileStopPosition(stopPosition);
        var.putVariableList(variableName, p);
    }


    /**
     * enterEveryRule is executed always before enterAnyRule (first).
     * exitEveryRule is executed always after exitAnyRule (last).
     */
    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
        // exitEvery rule is always before exitX visit ?? ou really?
    }

    @Override
    public void exitCompilationUnit(@NotNull JavaParser.CompilationUnitContext ctx) {
//        Map<String, Variable.Properties> map = var.getVariableList();
//        for (String key : map.keySet()) {
//            System.out.println(key + " " + map.get(key));
//        }
    }

    // ------------------------------------------------------------------------
    @Override
    public void enterQualifiedName(@NotNull JavaParser.QualifiedNameContext ctx) {
        if (ctx.getParent().getClass() == JavaParser.ImportDeclarationContext.class) {
            // Change logger factory
            if (ctx.getText().toLowerCase().contains(loggerLoader.getLogFactory().toLowerCase())) {
                rewriter.replace(ctx.getStart(), ctx.getStop(), loggerLoader.getNgmonLogFactoryImport());
            }
            // Change logger and add namespace, logGlobal imports
            if (ctx.getText().toLowerCase().contains(loggerLoader.getLogger().toLowerCase())) {
                if (getLogType() == null) {
                    logType = ctx.getText();
                }

                String namespaceImport = "import " + ANTLRRunner.getCurrentFileInfo().getNamespace() +
                        "." + ANTLRRunner.getCurrentFileInfo().getNamespaceEnd() + "Namespace";
                String logGlobalImport = "import " + loggerLoader.getNgmonLogGlobal();
                // Change Log import with Ngmon Log, currentNameSpace and LogGlobal imports
                rewriter.replace(ctx.start, ctx.stop, loggerLoader.getNgmonLogImport() + ";\n"
                        + namespaceImport + "\n" + logGlobalImport);

                ANTLRRunner.getCurrentFileInfo().setNamespaceClass(
                        ANTLRRunner.getCurrentFileInfo().getNamespaceEnd() + "Namespace");
            }
        }
    }

    @Override
    public void exitFieldDeclaration(@NotNull JavaParser.FieldDeclarationContext ctx) {
        // Logger LOG = LogFactory.getLog(TestingClass.class);  ->
        // private static final XNamespace LOG = LoggerFactory.getLogger(XNamespace.class);
        String varName = ctx.variableDeclarators().variableDeclarator(0).variableDeclaratorId().getText();

        // TODO: log names should be in some dictionary form no "log" only
        // Test for equality of Log variable name and type
        if ((varName.toLowerCase().contains("log")) && ctx.type().getText().equals(getLogType())) {
            // store LOG variableName for further easier searching assistance
            if (logName == null) {
                logName = varName;
            }

            String logFieldDeclaration = ANTLRRunner.getCurrentFileInfo().getNamespaceClass() +
                " LOG = LoggerFactory.getLogger(" + ANTLRRunner.getCurrentFileInfo().getNamespaceClass() + ".class);";
//            System.out.println("replacing " + ctx.getStart() + ctx.getText() + " with " + logFieldDeclaration);
            rewriter.replace(ctx.getStart(), ctx.getStop(), logFieldDeclaration);

        } else {
            // It is not LOG variable, so let's store information about it for further log transformations
            if (ctx.variableDeclarators().variableDeclarator().size() == 1) {
                checkAndStoreVariable(varName, ctx.type().getText(), ctx.start.getLine(),
                        ctx.getStart().getCharPositionInLine(), ctx.getStop().getCharPositionInLine(),
                        ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
            } else {
                // List size is more then 1 - never happened so far
                System.err.println("exitFieldDeclaration variableDeclarator().size() > 1!\n");
            }
        }
    }

    @Override
    public void exitLocalVariableDeclaration(@NotNull JavaParser.LocalVariableDeclarationContext ctx) {
        String varName = null;
        String varType = null;
        if (ctx.variableDeclarators().variableDeclarator().size() == 1) {
            varName = ctx.variableDeclarators().variableDeclarator(0).variableDeclaratorId().getText();
            varType = ctx.type().getText();
        } else {
            // List size is more then 1 - never happened so far
            System.err.println("exitLocalVariableDeclaration variableDeclarator().size() > 1!\n");
        }
//        System.out.printf("type=%8s  name=%8s  start=%d:%d-%d ~ %d-%d   %50s\n",
//                varType, varName, ctx.start.getLine(),
//                ctx.getStart().getCharPositionInLine(), ctx.getStop().getCharPositionInLine(),
//                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());

        checkAndStoreVariable(varName, varType, ctx.start.getLine(),
                ctx.getStart().getCharPositionInLine(), ctx.getStop().getCharPositionInLine(),
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }

    @Override
    public void exitConstantDeclarator(@NotNull JavaParser.ConstantDeclaratorContext ctx) {
        // Should change log definition and store variable as well,
        // but it seems like this construction is not used at all.
        System.err.println("constant!" + ctx.getText());
        System.exit(100);
        // Maybe not used at all?!
    }

    @Override
    public void exitConstDeclaration(@NotNull JavaParser.ConstDeclarationContext ctx) {
        // This is not duplicate!
        // Should change log definition and store variable as well,
        // but it seems like this construction is not used at all.
        System.err.println("constDec=" + ctx.getText() + " in file=" + var.getFileName());
        System.exit(100);
    }


    @Override
    public void exitBlockStatement(@NotNull JavaParser.BlockStatementContext ctx) {
        // Translate "if (LOG.isXEnabled())" statement to "if (LogGlobal.isXEnabled())"
        if ((ctx.statement() != null) && (ctx.statement().getChildCount() > 0)) {
            if (ctx.statement().getChild(0).getText().toLowerCase().equals("if")) {
                JavaParser.ExpressionContext exp = ctx.statement().parExpression().expression();
                if (exp.getText().startsWith(logName + ".")) {
                    // Check if Log call matches regexp "isXEnabled"
                    if (exp.expression(0).getChild(exp.expression(0).getChildCount() - 1).getText()
                            .matches("is.*Enabled")) {
                        // Now we can safely replace logName by LogGlobal
                        JavaParser.ExpressionContext log = exp.expression(0).expression(0);
                        rewriter.replace(log.start, log.stop,
                                loggerLoader.getQualifiedNameEnd(loggerLoader.getNgmonLogGlobal()));
                    } else {
                        System.err.println("Not implemented translation of log call! " +
                                "Don't know what to do with '" + exp.getText() + "'.");
                    }
                }
            }
        }
    }

    @Override
    public void exitStatementExpression(@NotNull JavaParser.StatementExpressionContext ctx) {
        // Process LOG.X(stuff);
        if (ctx.getText().startsWith(logName + ".")) {
            System.out.println("exitStmnt     = " + ctx.getText() + " " + ctx.expression().getChildCount());

            // if Log.operation is in currentLoggerMethodList - transform it,
            // else throw new exception or add it to methodList?


        }
    }
}
