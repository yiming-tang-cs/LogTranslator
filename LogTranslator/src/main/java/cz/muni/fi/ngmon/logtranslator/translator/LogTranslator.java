package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import cz.muni.fi.ngmon.logtranslator.common.Log;
import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.LogFilesFinder;
import cz.muni.fi.ngmon.logtranslator.common.Utils;
import cz.muni.fi.ngmon.logtranslator.generator.HelperGenerator;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LogTranslator extends JavaBaseListener {
    TokenStreamRewriter rewriter;
    //    BufferedTokenStream bufferedTokens; // intended to be used with multiple channels for handling WHITESPACES and COMMENTS
    private LoggerLoader loggerLoader = null;
    private LogFile logFile;
    private int currentLine = 0;
    private String logName = null; // reference to original LOG variable name
    private String logType = null; // reference to original LOG variable type
    private boolean isExtending;
    private boolean ignoreLogs = false;

    public LogTranslator(BufferedTokenStream tokens, LogFile logfile, boolean ignoreLogStatements, boolean isExtending) {
        this.ignoreLogs = ignoreLogStatements;
        this.isExtending = isExtending;
        rewriter = new TokenStreamRewriter(tokens);
        this.logFile = logfile;
        if (isExtending && !ignoreLogStatements) {
            LoggerFactory.setActualLoggingFramework(null);
        }
    }

    public TokenStreamRewriter getRewriter() {
        return rewriter;
    }


    /**
     * enterEveryRule is executed always before enterAnyRule (first).
     * exitEveryRule is executed always after exitAnyRule (last).
     */
    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
        currentLine = ctx.getStart().getLine();
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
        // exitEvery rule is always before exitX visit ?? ou really?
    }

    /**
     * Do clean up of resources when exiting given Java source code file.
     *
     * @param ctx ANTLR's internal context of entry point for Java source code
     *            JavaParser.CompilationUnitContext
     */
    @Override
    public void exitCompilationUnit(@NotNull JavaParser.CompilationUnitContext ctx) {
//        Map<String, List<LogFile.Variable>> map = logFile.getVariableList();
//        for (String key : map.keySet()) {
//            System.out.println(key + " " + map.get(key));
//        }
        // do cleanUp()
        LoggerFactory.setActualLoggingFramework(null);
        loggerLoader = null;
        logFile.setFinishedParsing(true);
        TranslatorStarter.addProcessedFilesCounter();
    }

    // ------------------------------------------------------------------------

    /**
     * If extending class type is not full package qualified name, look for imports to determine package &
     * appropriate file path location to get all variables from that file. (might be already parsed
     * by ANTLR and stored needed variables. - look for isField = true) Else parse file.
     *
     * @param ctx ANTLR's JavaParser.ClassDeclarationContext context
     */
    @Override
    public void enterClassDeclaration(@NotNull JavaParser.ClassDeclarationContext ctx) {

        if (ctx.type() != null) {

            String extendingFileTosearch = null;
            boolean isPackage = false;

            // Get class type & look up filepath - based on childCount (number of dots in type) resolve correct type
//            System.out.println(ctx.type().classOrInterfaceType().getChildCount() + "  " + ctx.type().classOrInterfaceType().getText());
            switch (ctx.type().classOrInterfaceType().getChildCount()) {
                case 1:
                    // no dots - pure type - has to be in application as source file
                    extendingFileTosearch = ctx.type().getText();
                    break;
                case 2:
                    // Class<DiamondType> - handle same as 3
                case 3:
                    // inner class of some other class - get Main class type
                    extendingFileTosearch = ctx.type().classOrInterfaceType().Identifier(0).getText();
                    break;
                default:
                    // it's a package type - if it is not "our application's namespace" - ignore it
                    if (ctx.type().getText().startsWith(Utils.getApplicationNamespace())) {
                        isPackage = true;
                        extendingFileTosearch = ctx.type().getText();
                    }
            }
//            System.out.println(extendingFileTosearch + " is package? " + isPackage);
            if (extendingFileTosearch != null) addExtendingClassVariables(extendingFileTosearch, isPackage);

        }
    }

    /**
     * From given extending class type, find appropriate file path to this extending class.
     * Use current import list to determine whole package and then look for files.
     * From import list use only relevant parts - based on application's namespace.
     *
     * @param extendingFileToSearch search for this class type (or package if isPackage is true)
     * @param isPackage             true if extendingFileToSearch Class type is qualified name package
     * @return true if we successfully parsed extending class and added new variables to current logFile
     */
    private boolean addExtendingClassVariables(String extendingFileToSearch, boolean isPackage) {
//        System.out.println("lookfor=" + extendingFileToSearch);
        String fileNameFromImport;
        String tempFileImport = null;
        boolean parsedExtendingClass = false;

        if (isPackage) {
            tempFileImport = extendingFileToSearch;
        } else {
            for (String fileImport : logFile.getImports()) {
                if (Utils.getQualifiedNameEnd(fileImport).equals(extendingFileToSearch)) {
                    tempFileImport = fileImport;
                    break;
                }
            }
        }

        if (tempFileImport == null) {
            // Not found in imports && not package itself, it has to be single class type name.
            // Class type is definitely from this package, so append logFile's current package name
            tempFileImport = logFile.getPackageName() + "." + extendingFileToSearch;
        }

        fileNameFromImport = tempFileImport.replaceAll("\\.", File.separator) + ".java";
//        System.out.println(tempFileImport + " vs " + extendingFileToSearch + " =>" + fileNameFromImport);

        List<LogFile> lfiles = TranslatorStarter.getLogFiles();
        for (LogFile lf : lfiles) {
            if (lf.getFilepath().contains(fileNameFromImport)) {
//                System.out.println("GOT IT " + logFile.getFilepath() + " " + lf.getFilepath());
                if (!lf.isFinishedParsing() && (!logFile.getFilepath().equals(lf.getFilepath()))) {
                    // parseFile & connect it with this logFile
                    System.out.println("Starting ANTLR on file " + lf.getFilepath() + " from " + logFile.getFilepath());
                    ANTLRRunner.run(lf, false, true);
                    parsedExtendingClass = true;
                    logFile.addConnectedLogFilesList(lf);
                }
            }
        }

        if (!parsedExtendingClass) {
            // We haven't found/added variables from extending class - search from all files
//            System.out.println("\nHaven't found " + fileNameFromImport + " yet, digging deeper\n");
            for (String javaFile : LogFilesFinder.getAllJavaFiles()) {
//                System.out.println(javaFile + " x " + fileNameFromImport );
                if (javaFile.contains(fileNameFromImport)) {
                    System.out.println("\tFound=" + javaFile);
                    // if this file is not the same file, go into it, else exit method
                    if (!logFile.getFilepath().equals(javaFile)) {
                        LogFile nonLogLogFile = new LogFile(javaFile);
                        TranslatorStarter.addNonLogLogFile(nonLogLogFile);
                        ANTLRRunner.run(nonLogLogFile, true, true);
                        parsedExtendingClass = true;
                        logFile.addConnectedLogFilesList(nonLogLogFile);
//                        TODO  ? finish parsing of variables from java files without
                    }
                }
            }
        }
        return parsedExtendingClass;
    }

    /**
     * Add logFile's packageName, if this class was not parsed by LogFilesFinder before.
     *
     * @param ctx ANTLR's JavaParser.PackageDeclarationContext context
     */
    @Override
    public void enterPackageDeclaration(@NotNull JavaParser.PackageDeclarationContext ctx) {
        if (logFile.getPackageName() == null) {
            logFile.setPackageName(ctx.qualifiedName().getText());
        }
    }

    @Override
    public void exitImportDeclaration(@NotNull JavaParser.ImportDeclarationContext ctx) {
        // Store import classes - might be used later for extending purposes and finding appropriate class
        logFile.addImport(ctx.qualifiedName().getText());
        if (ctx.getText().contains("static")) {
            logFile.setContainsStaticImport(true);
            if (ctx.getText().substring(0, ctx.getText().length() - 1).endsWith("*")) {
                int star = ctx.getText().length() - 3;
                int lastDot = ctx.getText().substring(0, ctx.getText().length() - 4).lastIndexOf(".") + 1;
                String staticImport = ctx.getText().substring(lastDot, star);
                System.out.println("staticImport = " + staticImport);
                logFile.addStaticImports(staticImport);
            }
        }
    }

    /**
     * Method visits qualified names of import declarations, in case if it is import statement,
     * evaluate it and create new LoggerLoader for this Java source code file.
     * Change import to NGMON's LogFactory, Logger and add namespace import using
     * TokenStreamRewriter class.
     *
     * @param ctx ANTLR's internal context of JavaParser.QualifiedNameContext
     */
    @Override
    public void enterQualifiedName(@NotNull JavaParser.QualifiedNameContext ctx) {
        if (ctx.getParent().getClass() == JavaParser.ImportDeclarationContext.class) {
            // Determine actual logging framework
            if (LoggerFactory.getActualLoggingFramework() == null) {
                loggerLoader = LoggerFactory.determineCreateLoggingFramework(ctx.getText());

                if (loggerLoader == null) {
                    // this is not log import, we can safely skip it
//                    System.err.println("No loggingFw=" + LoggerFactory.getActualLoggingFramework() + " " + ctx.getText());
                    return;
                }
            }
            if (loggerLoader != null) {
                // Change logger factory import
                if (loggerLoader.getLogFactory() != null) {
                    if (ctx.getText().toLowerCase().contains(loggerLoader.getLogFactory().toLowerCase())) {
//                System.out.println("loggerLoader.LogFactory=" + loggerLoader.getLogFactory());
//                System.out.println("logfactory=" + ctx.getText());
                        rewriter.replace(ctx.getStart(), ctx.getStop(), Utils.getNgmonLogFactoryImport());
                    }
//                else {
//                    System.err.println("ERROR!" + ctx.getText() + "in \n" + ctx.start.getLine() + " " + logFile.getFilepath());
//                }
                }
                // Change logger and add namespace, logGlobal imports
                for (String logImport : loggerLoader.getLogger()) {
                    if (ctx.getText().toLowerCase().equals(logImport.toLowerCase())) {
                        if (getLogType() == null) {
                            logType = ctx.getText();
//                        System.out.println("log=" + logType);
                        }

                        String namespaceImport = "import " + ANTLRRunner.getCurrentFile().getNamespace() +
                                "." + ANTLRRunner.getCurrentFile().getNamespaceEnd() + "Namespace;";
                        String logGlobalImport = "import " + Utils.getNgmonLogGlobal();
                        // Change Log import with Ngmon Log, currentNameSpace and LogGlobal imports
                        rewriter.replace(ctx.start, ctx.stop, Utils.getNgmonLogImport() + ";\n"
                                + namespaceImport + "\n" + logGlobalImport);

                        ANTLRRunner.getCurrentFile().setNamespaceClass(
                                ANTLRRunner.getCurrentFile().getNamespaceEnd() + "Namespace");
                    }
                }
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
            replaceLogFactory(ctx);

        } else {
            // It is not LOG variable, so let's store information about it for further log transformations
            if (ctx.variableDeclarators().variableDeclarator().size() == 1) {
                logFile.storeVariable(ctx, varName, ctx.type().getText(), true, null);
            } else {
                // Let's hope there are no 2 loggers defined on same line - should be impossible as well
//                System.err.println("exitFieldDeclaration variableDeclarator().size() > 1!" + ctx.getText() + "\n" + logFile.getFilepath());
                // There are 2+ variables defined on single line (with same type) let's handle it
                String varType = ctx.type().getText();
                for (JavaParser.VariableDeclaratorContext varContext : ctx.variableDeclarators().variableDeclarator()) {
                    varName = varContext.variableDeclaratorId().getText();
                    logFile.storeVariable(varContext, varName, varType, true, null);
                }
            }
        }
    }

    /**
     * Store one/more local variable declarations to variable list
     *
     * @param ctx ANTLR's JavaParser.LocalVariableDeclarationContext context
     */
    @Override
    public void exitLocalVariableDeclaration(@NotNull JavaParser.LocalVariableDeclarationContext ctx) {
        String varType = ctx.type().getText();

        if (ctx.variableDeclarators().variableDeclarator().size() == 1) {
            String variable = ctx.variableDeclarators().variableDeclarator(0).variableDeclaratorId().getText();
            logFile.storeVariable(ctx, variable, varType, false, null);
        } else {
            // Multiple variables are defined on one line. Ugly.. handle.
            List<JavaParser.VariableDeclaratorContext> variables = ctx.variableDeclarators().variableDeclarator();
            for (JavaParser.VariableDeclaratorContext var : variables) {
                String varName = var.variableDeclaratorId().getText();
//                System.out.println("Storing var=" + varType + " " + varName + " " + ctx.getText());
                logFile.storeVariable(ctx, varName, varType, false, null);
            }
        }
    }

    /**
     * Parse variable names and types from method formal parameters.
     *
     * @param ctx ANTLR's JavaParser.FormalParameterListContext context
     */
    @Override
    public void exitFormalParameterList(@NotNull JavaParser.FormalParameterListContext ctx) {
        String varName;
        String varType;
        for (JavaParser.FormalParameterContext parameter : ctx.formalParameter()) {
//            System.out.println("param=" + parameter.getText());
            varType = parameter.type().getText();
            varName = parameter.variableDeclaratorId().getText();
            logFile.storeVariable(parameter, varName, varType, false, null);
        }
    }

    /**
     * Change log definition or store variable.
     * Used in interface, declaration part of static variables.
     *
     * @param ctx ANTRL's context
     */
    @Override
    public void exitConstDeclaration(@NotNull JavaParser.ConstDeclarationContext ctx) {
//        System.out.println("constDec=" + ctx.getText() + "\t\t" + logFile.getFilepath());
        if (loggerLoader != null && loggerLoader.containsLogFactory(ctx.getText())) {
            System.out.println("constDeclLoggerloader=" + ctx.getText() + " " + ctx.constantDeclarator(0).Identifier().getText() +
                    " " + ctx.type().getText() + "\n" + logFile.getFilepath());
            if (this.logName == null) this.logName = ctx.constantDeclarator(0).Identifier().getText();
            replaceLogFactory(ctx);

        } else {
//            System.out.println("storing constDecl=" + ctx.getText() + " " +
//                    ctx.constantDeclarator(0).Identifier().getText() + " " + ctx.type().getText());
            logFile.storeVariable(ctx, ctx.constantDeclarator(0).Identifier().getText(), ctx.type().getText(), true, null);
        }
    }

    /**
     * Store exception variables from 'catch clause' statements.
     *
     * @param ctx ANTLR's JavaParser.CatchClauseContext context
     */
    @Override
    public void enterCatchClause(@NotNull JavaParser.CatchClauseContext ctx) {
        String errorVarName = null;
        String errorTypeName;

        if (ctx.getChild(ctx.getChildCount() - 3) != null) {
            errorVarName = ctx.getChild(ctx.getChildCount() - 3).getText();
        }

        /** Check for simple 'catch (Exception e)' or multi-exception
         *  'catch (NullPointerException | IllegalArgumentException | IOException ex)' usage */
        if (ctx.catchType().getChildCount() == 1) {
//            System.out.println(ctx.getChild(2).getText() + " " + ctx.getChild(3).getText());
            errorTypeName = ctx.getChild(2).getText();
        } else {
            // Store Exception as variable type name (as we can not tell which exception has higher priority)
            errorTypeName = "Exception";
        }
        logFile.storeVariable(ctx, errorVarName, errorTypeName, false, "Exception");
    }

    /**
     * Get type and name of variable from enhanced for-loop
     *
     * @param ctx ANTLR's JavaParser.EnhancedForControlContext context
     */
    @Override
    public void exitEnhancedForControl(@NotNull JavaParser.EnhancedForControlContext ctx) {
        if (ctx.Identifier() != null) {
            logFile.storeVariable(ctx, ctx.Identifier().getText(), ctx.type().getText(), false, null);
        }
    }

    /**
     * Translate log checking method to our static/global checker -
     * LOG.isLevelEnabled() -> LogGlobal.isLevelEnabled()
     *
     * @param ctx ANTLR's JavaParser.BlockStatementContext context
     */
    @Override
    public void exitBlockStatement(@NotNull JavaParser.BlockStatementContext ctx) {
        // Translate "if (LOG.isXEnabled())" statement to "if (LogGlobal.isXEnabled())"
        if ((ctx.statement() != null) && (ctx.statement().getChildCount() > 0)) {
            if (ctx.statement().getChild(0).getText().toLowerCase().equals("if")) {
                JavaParser.ExpressionContext exp = ctx.statement().parExpression().expression();
                if (exp.getText().contains(logName + ".")) {
//                    System.out.println("expression=" + exp.getText());
                    if (Utils.listContainsItem(Utils.BOOLEAN_OPERATORS, exp.getText())) {
                        // if (abc && LOG.isX() || xyz) get LOG statement context
                        for (JavaParser.ExpressionContext ec : exp.expression()) {
                            if (ec.getText().startsWith(logName + ".")) {
                                exp = ec;
                                break;
                            }
                        }
                    }

                    // Check if Log call is in current checkerLogMethods() 'isXEnabled()'
                    ParseTree methodCall;
                    if (exp.start.getText().startsWith(Utils.NEGATION)) {
                        methodCall = exp.expression(0).expression(0).getChild(exp.expression(0).expression(0).getChildCount() - 1);
                    } else {
                        methodCall = exp.expression(0).getChild(exp.expression(0).getChildCount() - 1);

                    }
//                    System.out.printf("exp=%s, method=%s%n", exp.getText(), methodCall.getText() );
//                    if (loggerLoader == null) {
//                        System.err.println("loggerLoader is null, but some log call is here " + methodCall.getText() + " " + logFile.getFilepath());
//                    }
                    if (loggerLoader.getCheckerLogMethods().contains(methodCall.getText())) {
                        // Now we can safely replace logName by LogGlobal
                        JavaParser.ExpressionContext log = exp.expression(0).expression(0);
                        rewriter.replace(log.start, log.stop,
                                Utils.getQualifiedNameEnd(Utils.getNgmonLogGlobal()));
                    } else {
                        // TODO throw some kind of error - handle negation!
                        System.err.println("Not implemented translation of log call! " +
                                "Don't know what to do with '" + exp.getText() + "'." + loggerLoader.getCheckerLogMethods());
                    }
                }
            }
        }
    }

    /**
     * TODO docs + fix - main rewriting part of ANTLR
     *
     * @param ctx ANTLR's JavaParser.StatementExpressionContext context
     */
    @Override
    public void exitStatementExpression(@NotNull JavaParser.StatementExpressionContext ctx) {
        // Process LOG.XYZ(stuff);
        if ((logName == null) && !ignoreLogs) {
            /** If (extending, visit that class and find log declaration :) ) and use it here -- you WISH!
             * It can be unnecessary hard to find extending class, There might be a chance, that this class
             * extends otherClass, which contains defined LOG. So we will go with failsafe logger
             */

//            System.err.println("Unable to change log calls, when log factory has not been defined! Error. Exiting. " +
//                    logFile.getFilepath() + "\n " + ctx.getText());
            if (ctx.getText().toLowerCase().startsWith("log")) {
                System.out.println("our extending log statements! " + ctx.expression().expression(0).expression(0).getText() +
                        " " + logFile.getFilepath());
                logName = ctx.expression().expression(0).expression(0).getText();
                if (LoggerFactory.getActualLoggingFramework() == null) {
                    loggerLoader = LoggerFactory.determineCreateLoggingFramework("failsafe");
                }
            }
        }

        // should we only parse variables?
        if (!ignoreLogs) {
            if (ctx.getText().startsWith(logName + ".")) {
//            System.out.println("exitStmnt     = " + ctx.getText() + " " + ctx.expression().getChildCount());
                if ((ctx.expression().expression(0) != null) && (ctx.expression().expression(0).getChildCount() == 3)) {
                    // Get "XYZ" Log call into methodCall
                    ParseTree methodCall = ctx.expression().expression(0).getChild(2);

                    // if Log.operation is in currentLoggerMethodList - transform it,
                    if (loggerLoader.getTranslateLogMethods().contains(methodCall.getText())) {
//                    System.out.println("yes, '" + methodCall +"' is in current logger method list.");

                        Log log = transformMethodStatement(ctx.expression().expressionList());
                        HelperGenerator.generateMethodName(log);
                        log.setLevel(methodCall.getText());
                        logFile.addLog(log);

//                        System.out.println(log);
//                        System.out.println(logFile.getVariableList());
                        /** TODO 1) add transformed method to appropriate XYZNamespace
                         * TODO 2) ADD GOMATCH support here */
                        // rewrite this log
                        String ngmonLogReplacement = HelperGenerator.generateLogMethod(logName, log);
                        System.out.println(ngmonLogReplacement);
//                        rewriter.replace(null, null, null);
                    }
                    // else throw new exception or add it to methodList?
                }
            }
//        else {
            // ok this is not a LOG statements... we can throw it away?
//            System.out.println("===> " + ctx.getText() + logFile.getFilepath() + " " + ctx.start.getLine());
//        }
        }
    }

    /**
     * Choose how to transform log statement input, based on logging framework
     * or construction of statement itself. Whether it contains commas, pluses or '{}'.
     *
     * @param expressionList expressionList statement to be evaluated (method_call)
     */

    private Log transformMethodStatement(JavaParser.ExpressionListContext expressionList) {
        Log log = new Log();
        boolean isSpecial = false;
        // Handle 'plus' separated log - transformation of 'Log.X("This is " + sparta + "!")'
        // Handle comma separated log statement 'Log.X("this is", sparta)'

        // TODO if expressions are separated by commas and/or first argument contains '%x' or '{}'
        // delicately handle such situation
        if (expressionList != null) {
            if (expressionList.expression(0).getText().contains("{}") || expressionList.expression(0).getText().contains("%")) {
//                System.out.println(expressionList.getText() + " is special case!");
                isSpecial = true;
                // if (currentFramework == slf4j) then handle 2 types of messages: '"msgs {}", logFile' and classic '"das" + das + "dsad"';
                // handle {} and "" ?
            }


            for (JavaParser.ExpressionContext ec : expressionList.expression()) {
//                System.out.println("ec=" + ec.getText());
                fillCurrentLog(log, ec, isSpecial);
            }
        } else {
            // ExpressionList is empty! That means it is 'Log.X()' statement.
            log.setTag("EMPTY_STATEMENT");
            // TODO
        }
        return log;
    }

    /**
     * Parse data from expression nodes - recursive tree of expressions in LOG.X(expression) call.
     * Successfully uses ANTLR's property of tree-building, that successive leaves are built on
     * first node, which leaves second node of tree (expression) as log comment or variable.
     *
     * @param log        Log to be filled with data from this log method statement
     * @param expression ANTLR's internal representation of JavaParser.ExpressionContext context
     * @param isSpecial  If true, first argument/expression contains declaration of whole log ( '%s' '{}')
     */
    private void fillCurrentLog(Log log, JavaParser.ExpressionContext expression, boolean isSpecial) {
        if (expression == null) {
            System.err.println("Expression is null");
            return;
        }

        if (isSpecial) {
            System.out.println("MAGIC BEGINS here! " + expression.getText());
            // TODO - purpose?
        }

        int childCount = expression.getChildCount();

        if (childCount == 1) {
            determineLogTypeAndStore(log, expression);
        } else if (childCount == 2) {
            // TODO - LOG.fatal("Error reported on file " + f + "... exiting", new Exception()); done?
            // 'new Exception()' found only
            System.out.println("2Exception=" + expression.getText() + " " + expression.getChild(0).getText() + " " + expression.creator().getText());
            // new is followed by 'creator context'
            if (expression.getChild(0).getText().equals("new")) {
                determineLogTypeAndStore(log, expression);
            } else {
                System.err.println("Error " + expression.getText());
            }
//            if (expression.getText().contains("new") && expression.getText().toLowerCase().contains("exception")) {


            /** Recursively call this method to find out more information about *this* statement */
        } else if (childCount == 3) {
//            System.out.println("==" + expression.getText());
            if (expression.expression(1) != null) {
//                System.out.format("var=%s exp(0)=%s exp(1)=%s%n", expression.getText(), expression.expression(0).getText(), expression.expression(1).getText());
                determineLogTypeAndStore(log, expression.expression(1));
            }

            if (expression.expression().size() <= 1) {
                determineLogTypeAndStore(log, expression.expression(0));
                fillCurrentLog(log, expression.expression(0), isSpecial);
            } else {
//                System.out.println("+" + expression.expression(0).getText());

                for (JavaParser.ExpressionContext ec : expression.expression().subList(0, expression.expression().size() - 1)) {
//                System.out.println("ec=" + ec.getText());
                    fillCurrentLog(log, ec, isSpecial);
                }
            }

//        } else if (expression.children.size() == 4) {
        } else if (childCount == 4) {
            // TODO ?
            List<String> stringList = Arrays.asList("format", "print");
//            System.out.println("asd" + expression.expression(0).getText());
            if (Utils.listContainsItem(stringList, expression.expression(0).getText())) {
                for (JavaParser.ExpressionContext ch : expression.expressionList().expression()) {
//                    System.out.println("ch=" + ch.getText());
                    determineLogTypeAndStore(log, ch);
                }
            }
        } else {

            System.err.printf("Error! ChildCount=%d: %s %d:%s%n", childCount, expression.getText(), expression.getStart().getLine(), logFile.getFilepath());
        }
    }

    /**
     * Method determines type of variable and stores it for this particular logger.
     *
     * @param log        current log instance to store variables for given method
     * @param expression ANTLR's internal representation of JavaParser.ExpressionContext context
     *                   which holds information about variable
     */

    public void determineLogTypeAndStore(Log log, JavaParser.ExpressionContext expression) {
        if (expression.getText().startsWith("\"")) {
            log.addComment(cultivate(expression.getText()));
        } else {
            LogFile.Variable varProperty = findVariable(log, expression);
            log.addVariable(varProperty);
        }
    }

    /**
     * Associate input variable with variable from known variables list.
     * This method handles some special cases of 'variable declarations' in log
     * statements. You should create your own, if you find out one.
     * Use ANTLR's grun gui tool to see proper structure.
     * After separating name and type, store variable using storeVariable() method.
     *
     * @param findMe ANTLR's internal representation of JavaParser.ExpressionContext context
     *               which holds variable to find
     */
    private LogFile.Variable findVariable(Log log, JavaParser.ExpressionContext findMe) {
//        System.out.println("findMe " + findMe.getText() + "  " + findMe.start.getLine() + ":" + logFile.getFilepath());
        LogFile.Variable foundVar = findVariableInLogFile(logFile, findMe);
        String ngmonNewName = null;

        if (foundVar == null) {
            String varType;
            String varName;
            /**
             * When variables were not found, dug deeper. Special cases
             * needed to be handled, generated by special log calls
             *
             * If expression is composite - has at least one '.', use it as "variable" and change type to String
             * 'l.getLedgerId()', 'KeeperException.create(code,path).getMessage()', ...
             */
            if (findMe.getText().contains(".")) {
//                System.out.println("looking for " + findMe.getText());
                // TODO change newNgmonName to simpler version?

                logFile.storeVariable(findMe, findMe.getText(), "String", false, null);
                foundVar = returnLastValue(findMe.getText()); //logFile.getVariableList().get(findMe.getText()).get(0);

                /** handle 'new String(data, UTF_8)' or 'new Exception()' */
            } else if (findMe.getText().contains("new")) {
                // declaration of 'new String(data, ENC)' or 'new Exception()'
                if (findMe.creator() != null) {
                    if (findMe.creator().getText().contains("Exception")) {
                        // This _might_ be a problem in future
                        varName = "new " + findMe.creator().getText();
                        varType = "String";
                        ngmonNewName = "exception";
                        log.setTag("Error");

                    } else if (findMe.creator().getText().contains("Throwable")) {
                        // we don't want to have "new Throwable()" as variable -
                        // on contrary, set "Error" as ngmon_log_tag
                        varName = "new " + findMe.creator().getText();
                        varType = "String";
                        ngmonNewName = "throwable";
                        log.setTag("Error");
                    } else {
                        varType = findMe.creator().createdName().getText();
                        varName = findMe.creator().classCreatorRest().arguments().expressionList().expression(0).getText();
                    }
                    logFile.storeVariable(findMe, varName, varType, false, ngmonNewName);
                    foundVar = returnLastValue(varName);

                }

                /** Handle 'this' call */
            } else if (findMe.getText().startsWith("this")) {
                if (findMe.getText().equals("this")) {
                    // TODO - check it or negate if expression!
                    // We can ignore value assignment // so return all after DOT: this.'variable'
//                    foundVar = logFile.getVariableList().get()
                } else {
                    System.err.println("'this.' call found method!" + findMe.getText());
                }

                /** If variable is array [], find declared variable earlier and use it's type.
                 * If type is not found, use String as default. */
            } else if (findMe.getText().contains("[") && findMe.getText().contains("]")) {
                varName = findMe.getText();

                LogFile.Variable var = findVariable(log, findMe.expression(0));
                if (Utils.listContainsItem(Utils.PRIMITIVE_TYPES, var.getType())) {
                    varType = var.getType();
                } else {
                    varType = "String";
                }
//                System.out.println("array=" + varName + " " + findMe.expression(0).getText());
                logFile.storeVariable(findMe, varName, varType, false, null);
                foundVar = returnLastValue(varName);

                /** if X is instanceof Y,
                 * create new boolean variable named as 'isInstanceOfY' */
            } else if (findMe.getText().contains("instanceof")) {
                // TODO Done? #1 Rename variables and set them before ngmon Log itself.  bc instance ofXYZ... => boolean isInstanceOfY = bc;
                // add new parameter to logFile.storeVariable() - newVariableName
//                System.out.println("instanceof=" + findMe.getText());
                varName = findMe.primary().expression().expression(0).primary().getText();//.expression(0).type().getText();
                varType = "boolean";
                ngmonNewName = "isInstanceOf" + findMe.primary().expression().type().getText();
//                System.out.printf("var=%s newName=%s, original=%s", varName, newNgmonName, findMe.getText());
                logFile.storeVariable(findMe, varName, varType, false, ngmonNewName);


                /**
                 * Handle call of another method in class.
                 * Start another ANTLR process, look for method declarations and return type.
                 * Put it All back here. */
            } else if (findMe.getText().matches("\\w+\\(.*?\\)")) {
                List<String> methodArgumentsTypeList = new ArrayList<>();
//                System.out.println("do me!" + findMe.getStart().getLine() + " " + findMe.getText() +
//                        findMe.getChildCount() + findMe.expressionList().getText());

                if (findMe.expressionList() != null) {
                    LogFile.Variable tempList;
//                    System.err.println("formal params there " + findMe.expressionList().getText());
                    /** get types of formal parameters for correct method finding */
                    for (JavaParser.ExpressionContext ec : findMe.expressionList().expression()) {
                        if (ec.getText().startsWith("\"") && ec.getText().endsWith("\"")) {
                            methodArgumentsTypeList.add("String");

                        } else if ((tempList = returnLastValue(ec.getText())) != null) {
                            methodArgumentsTypeList.add(tempList.getType());
                        } else {
                            System.err.println("not found " + ec.getText());
                        }
                    }
                } else {
                    methodArgumentsTypeList = null;
                }
                /** Look into extending class for this method call */
                System.out.println("looking for=" + findMe.getText() + " " + findMe.start.getLine() + " " + logFile.getFilepath());
                if (!HelperLogTranslator.findMethod(logFile, findMe.getText(), methodArgumentsTypeList)) {
                    /** Method has not been found in class. Store it anyway.
                     * Exactly same situation as variable containing "." */
                    ngmonNewName = findMe.expression(0).getText() + "MethodCall";
                    log.setTag("methodCall");
                    logFile.storeVariable(findMe, findMe.getText(), "String", false, ngmonNewName);
                }
                foundVar = returnLastValue(findMe.getText());

                /** Mathematical expression - use double as type */
            } else if (Utils.listContainsItem(Utils.MATH_OPERATORS, findMe.getText())) {
//                containsMathOperator(findMe.getText()))
                varName = findMe.getText();
                varType = "double";
                logFile.storeVariable(findMe, varName, varType, false, null);
                foundVar = returnLastValue(varName);

                /** If variable begins with NEGATION '!' */
            } else if (findMe.getText().startsWith("!")) {
                varName = findMe.getText().substring(1);
//                logFile.storeVariable(findMe, varName, varType, false);
                foundVar = returnLastValue(varName);

                /** Check for ternary if operator in log */
            } else if (findMe.getText().contains("?")) {
                /**
                 * Create it as "isX",  addComment to log as "isX", store
                 * this boolean variable add tag to this log as "ternary" */

                varName = findMe.primary().expression().expression(0).getText();
                ngmonNewName = "is" + varName.replace(varName.charAt(0), (Character.toUpperCase(varName.charAt(0)))); // isVarName
                varType = "boolean";
                logFile.storeVariable(findMe, varName, varType, false, ngmonNewName);
                log.setTag("boolean");
                foundVar = returnLastValue(varName);

                /** if whole text is uppercase & we have static imports, assume this is static variable
                 * and store it */
            } else if (logFile.isContainsStaticImport() && findMe.getText().equals(findMe.getText().toUpperCase())) {
                System.err.println("Assuming external variable from static import " + findMe.getText());
                logFile.storeVariable(findMe, findMe.getText(), "String", false, null);

                /** Last chance - look into extending class and their variables */
            } else if (logFile.getConnectedLogFilesList() != null) {
                for (LogFile lf : logFile.getConnectedLogFilesList()) {
                    foundVar = findVariableInLogFile(lf, findMe);
                }
                if (foundVar != null) {
                    logFile.storeVariable(findMe, foundVar.getName(), foundVar.getType(), foundVar.isField(), foundVar.getNgmonName());
                }

                /** We have ran out of luck. Have not found given variable in my known parsing list. */
            } else {
                System.out.println("Unable to find variable " + findMe.getText() + " in file " +
                        findMe.start.getLine() + " :" + logFile.getFilepath() + "\n" + logFile.getVariableList().keySet());
                if (Utils.ignoreParsingErrors) {
                    return null;
                } else {
                    Thread.dumpStack();
                    System.exit(100);
                }
            }
        }
        return foundVar;
    }

    /**
     * Search for variable in given LogFile.
     *
     * @param logFile to search in
     * @param findMe  variable to look for
     * @return found Variable in given logFile, null if not found
     */
    public LogFile.Variable findVariableInLogFile(LogFile logFile, JavaParser.ExpressionContext findMe) {
        LogFile.Variable foundVar = null;
        boolean isArray = isArray(findMe);
        for (String key : logFile.getVariableList().keySet()) {
            if (findMe.getText().equals(key)) {
                List<LogFile.Variable> variableList = logFile.getVariableList().get(findMe.getText());
                // search for non-array variable value
                if (variableList.size() > 1) {
                    // get closest line number (or field member)
                    int closest = currentLine;
                    for (LogFile.Variable p : variableList) {
                        if (currentLine - p.getLineNumber() < closest) {
                            closest = currentLine - p.getLineNumber();
                            foundVar = p;
                        }
                    }
                } else {
                    foundVar = variableList.get(0);
                }
            } else if (isArray) {
                // look for array[] definition (without any stuff inside)
                if (isArray(key)) {
                    if ((findMe.getChildCount() > 3) && (findMe.expression(1).getText() != null)) {
                        // search for array declaration in variable list
//                        System.out.printf("search for %s[] \n", findMe.expression(0).getText() );
                        if (key.equals(findMe.expression(0).getText() + "[]")) {
                            foundVar = logFile.getVariableList().get(key).get(0);
                        }
                    }
                }
            }
        }
        return foundVar;
    }

    private boolean isArray(JavaParser.ExpressionContext context) {
        return (context.getText().contains("[") && context.getText().contains("]"));
    }

    private boolean isArray(String text) {
        return (text.contains("[") && text.contains("]"));
    }

    /**
     * Always return last added variable from variable list. This is guaranteed
     * by LinkedHashSet().
     *
     * @param variable to look up for
     * @return returns Variable object for given variable input
     */
    private LogFile.Variable returnLastValue(String variable) {
//        System.out.println("looking for" + variable + " " + logFile.getFilepath());
        List<LogFile.Variable> list = logFile.getVariableList().get(variable);
//        System.out.println("returning " + list);
        return list.get(list.size() - 1);
    }

    /**
     * Drop quotes, extra spaces, commas, non-alphanum characters
     * into more fashionable way for later NGMON log method naming generation
     *
     * @param str string to be changed
     */
    private String cultivate(String str) {
//        System.out.print("cultivating  " + str);
        str = str.substring(1, str.length() - 1).trim();
        str = str.replaceAll("\\d+", "");   // remove all digits as well?
        str = str.replaceAll("%\\w", " "); // remove all single chars
        str = str.replaceAll("\\W", " ").replaceAll("\\s+", " ").trim();
//        System.out.print("  -->" + str + "\n");
        return str;
    }

    /**
     * Method returns last part of actual log type from import.
     * Used for searching of declaration of 'old/to be changed' logger
     *
     * @return class name of currently used logger in java file
     */
    public String getLogType() {
        if (logType == null) {
            return null;
        } else {
            // return only last part of QN
            return logType.substring(logType.lastIndexOf(".") + 1);
        }
    }


    public void replaceLogFactory(ParserRuleContext ctx) {
        String logFieldDeclaration = ANTLRRunner.getCurrentFile().getNamespaceClass() +
                " LOG = LoggerFactory.getLogger(" + ANTLRRunner.getCurrentFile().getNamespaceClass() + ".class);";
//            System.out.println("replacing " + ctx.getStart() + ctx.getText() + " with " + logFieldDeclaration);
        rewriter.replace(ctx.getStart(), ctx.getStop(), logFieldDeclaration);
    }
}
