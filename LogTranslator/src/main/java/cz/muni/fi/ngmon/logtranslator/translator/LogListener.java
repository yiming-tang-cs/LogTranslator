package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Map;

public class LogListener extends JavaBaseListener {
    //    BufferedTokenStream bufferedTokens;

    static LoggerLoader loggerLoader = new CustomLoggerLoader();
    Variable var;
    TokenStreamRewriter rewriter;


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
        Map<String, Variable.Properties> map = var.getVariableList();
//        for (String key : map.keySet()) {
//            System.out.println(key + " " + map.get(key));
//        }
    }

    @Override
    public void exitImportDeclaration(@NotNull JavaParser.ImportDeclarationContext ctx) {
    }

    @Override
    public void enterVariableDeclarator(@NotNull JavaParser.VariableDeclaratorContext ctx) {
        // Logger <LOG> = LogFactory .... (if or look into FieldDeclaration
        //System.out.println("enVarDec " + ctx.variableDeclaratorId().getText() + " " + ctx.variableInitializer().expression().getText());
    }

    @Override
    public void exitVariableDeclarator(@NotNull JavaParser.VariableDeclaratorContext ctx) {
        super.exitVariableDeclarator(ctx);
    }

    // ------------------------------------------------------------------------
    @Override
    public void enterQualifiedName(@NotNull JavaParser.QualifiedNameContext ctx) {
        if (ctx.getParent().getClass() == JavaParser.ImportDeclarationContext.class) {
            // Change logger factory
            if (ctx.getText().toLowerCase().contains(loggerLoader.getLogFactory().toLowerCase())) {
                rewriter.replace(ctx.getStart(), ctx.getStop(), loggerLoader.getNgmonLogFactoryImport());
            }
            // Change logger and add namespace
            if (ctx.getText().toLowerCase().contains(loggerLoader.getLogger().toLowerCase())) {
                rewriter.replace(ctx.start, ctx.stop, loggerLoader.getNgmonLogImport());
            }
            // TODO: Add another import namespace?
        }
    }

    @Override
    public void exitFieldDeclaration(@NotNull JavaParser.FieldDeclarationContext ctx) {
        // Logger LOG = LogFactory.getLog(TestingClass.class);  ->
        // private static final XNamespace LOG = LoggerFactory.getLogger(XNamespace.class);
        String varName = ctx.variableDeclarators().variableDeclarator(0).variableDeclaratorId().getText();

        // TODO log names should be in some dictionary form no "log" only
        if (varName.toLowerCase().contains("log")) {
            String logFieldDeclaration = loggerLoader.getLoggingApplicationNamespaceShort() +
                    " LOG = LoggerFactory.getLogger(" + loggerLoader.getLoggingApplicationNamespaceShort() + ".class);";
            rewriter.replace(ctx.getStart(), logFieldDeclaration);
        } else {
            // it is not LOG variable, so let's store information about it for further log transformations
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
    public void exitConstantDeclarator(@NotNull JavaParser.ConstantDeclaratorContext ctx) {
        System.err.println("constant!" + ctx.getText());
        System.exit(100);
        // Maybe not used at all?!
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
    public void exitConstDeclaration(@NotNull JavaParser.ConstDeclarationContext ctx) {
        System.err.println("constDec=" + ctx.getText() + " in file=" + var.getFileName());

        System.exit(100);
    }
}
