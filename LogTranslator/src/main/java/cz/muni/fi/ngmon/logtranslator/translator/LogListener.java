package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class LogListener extends JavaBaseListener {
    TokenStreamRewriter rewriter;

    public LogListener(TokenStream tokens) {
        rewriter = new TokenStreamRewriter(tokens);
    }

    ParseTreeProperty<Changer> storage = new ParseTreeProperty<>();
    LoggerLoader loggerLoader = new CustomLoggerLoader();
    Changer actualChanger;

    public ParseTreeProperty<Changer> getStorage() {
        return storage;
    }

    public Changer getStorageValue(ParseTree ctx) {
        return storage.get(ctx);
    }

    public void setStorageValue(ParseTree ctx, Changer value) {
        storage.put(ctx, value);
    }

    public TokenStreamRewriter getRewriter() {
        return rewriter;
    }

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
        actualChanger = new Changer();
        actualChanger.setChanged(false);
        setStorageValue(ctx, actualChanger);
        System.out.printf("enterEveryRule=%s changed=%b content=%s\n", ctx.getText(), getStorageValue(ctx).isChanged(), getStorageValue(ctx).getContent());

    }


    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
        // exitEvery rule is always before exitX visit ?? ou really?
        if (!getStorageValue(ctx).isChanged()) {
            System.out.println("context was not changed! Storing=" + ctx.getText());
            setStorageValue(ctx, getStorageValue(ctx));
        }
        System.out.printf("exitEveryRule=%s --> %s\n", ctx.getText(), getStorageValue(ctx).getContent());
    }

    @Override
    public void exitCompilationUnit(@NotNull JavaParser.CompilationUnitContext ctx) {
//        actualChanger = new Changer();

        StringBuilder builder = new StringBuilder();
        System.out.println(ctx.getChildCount());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree tree = ctx.getChild(i);
            if (getStorageValue(tree) != null) {
                System.out.printf("child %d=%s\n", i, getStorageValue(tree).getContent());
                builder.append(getStorageValue(tree).getContent());
            }
        }

        actualChanger.setContent(builder.toString());
        actualChanger.setChanged(true);
        setStorageValue(ctx, actualChanger);
        System.out.println("exitCU " + ctx.getText());
        System.out.println("storage=");
    }


    @Override
    public void exitImportDeclaration(@NotNull JavaParser.ImportDeclarationContext ctx) {
        // change import to cz.muni.fi.ngmon.blabla
//        actualChanger = new Changer();
//        System.out.println("ExitImportDeclaration" + ctx.getText());

        CommonTokenStream tokens = Runner.getTokens();
        tokens.getSourceName();

        rewriter.insertAfter(ctx.start, " cz.muni.fi.blabol");

        if (ctx.qualifiedName().getText().toLowerCase().contains(loggerLoader.getLogFactory().toLowerCase())) {
            System.out.printf("%s ---> %s\n", ctx.qualifiedName().getText(), loggerLoader.getNgmonLogImport());
            actualChanger.setContent("import " + loggerLoader.getNgmonLogImport() + "\n");

        } else if (ctx.qualifiedName().getText().toLowerCase().contains(loggerLoader.getLogger().toLowerCase())) {
            System.out.printf("%s --> %s\n", ctx.qualifiedName().getText(), loggerLoader.getNgmonLogFactoryImport());
            actualChanger.setContent("Ximport " + loggerLoader.getNgmonLogFactoryImport() + "\n");

        } else {
            actualChanger.setContent("import " + ctx.qualifiedName().getText());
        }

        actualChanger.setChanged(true);
        setStorageValue(ctx, actualChanger);
        System.out.println("ExitImportDeclaration " + ctx.getText() + ctx.getRuleIndex());
        System.out.printf("Changed= %s --> %s\n", actualChanger.getContent(), getStorageValue(ctx).getContent());

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
}
