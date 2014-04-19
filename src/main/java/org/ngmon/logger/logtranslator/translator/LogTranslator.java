package org.ngmon.logger.logtranslator.translator;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.ngmon.logger.logtranslator.antlr.JavaBaseListener;
import org.ngmon.logger.logtranslator.antlr.JavaParser;
import org.ngmon.logger.logtranslator.common.*;
import org.ngmon.logger.logtranslator.generator.GoMatchGenerator;
import org.ngmon.logger.logtranslator.generator.HelperGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class LogTranslator extends JavaBaseListener {
    TokenStreamRewriter rewriter;
    //    BufferedTokenStream bufferedTokens; // intended to be used with multiple channels for handling WHITESPACES and COMMENTS
    private LoggerLoader loggerLoader = null;
    private LogFile logFile;
    private int currentLine = 0;
    private String logName = null; // reference to original LOG variable name
    private String logType = null; // reference to original LOG variable type
    private boolean ignoreLogs = false;
    private String classname;
//    private JavaParser.QualifiedNameContext importContext;  // position for final adding of correct "import log_events.<app-namespace>.CURRENT_NS;"

    public LogTranslator(BufferedTokenStream tokens, LogFile logfile, boolean ignoreLogStatements, boolean isExtending) {
        this.ignoreLogs = ignoreLogStatements;
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
        classname = ctx.Identifier().getText();

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
                    // todo debug()
//                    System.out.println("Starting ANTLR on file " + lf.getFilepath() + " from " + logFile.getFilepath());
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
                    // TODO log debug()
//                    System.out.println("\tFound=" + javaFile);
                    // if this file is not the same file, go into it, else exit method
                    if (!logFile.getFilepath().equals(javaFile)) {
                        LogFile nonLogLogFile = new LogFile(javaFile);
                        TranslatorStarter.addNonLogLogFile(nonLogLogFile);
                        ANTLRRunner.run(nonLogLogFile, true, true);
                        parsedExtendingClass = true;
                        logFile.addConnectedLogFilesList(nonLogLogFile);
//                        TODO  ? finish parsing of variables from java files without?
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
// todo log debug()
// System.out.println("staticImport = " + staticImport);
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
                // Change logger and add current log_events namespace and logGlobal imports
                for (String logImport : loggerLoader.getLogger()) {
                    if (ctx.getText().toLowerCase().equals(logImport.toLowerCase())) {
                        if (getLogType() == null) {
                            logType = ctx.getText();
//                        System.out.println("log=" + logType);
                        }

                        replaceLogImports(ctx);
//                        ANTLRRunner.getCurrentFile().setNamespaceClass();
                        // TODO add "import log_events.Utils.getApplicationNamespace();" + currentNamespace logfileNS
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
        if (loggerLoader != null && loggerLoader.containsLogFactory(ctx.getText())) {
//              todo log.debug()
            if (this.logName == null) this.logName = ctx.constantDeclarator(0).Identifier().getText();
            replaceLogFactory(ctx);

        } else {
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
     * Get type and name of variable from enhanced for-loop.
     * Handle Map, List, Pair<> statements.
     *
     * @param ctx ANTLR's JavaParser.EnhancedForControlContext context
     */
    @Override
    public void exitEnhancedForControl(@NotNull JavaParser.EnhancedForControlContext ctx) {
        if (ctx.Identifier() != null) {
            // TODO !?
//            String varType;
//            boolean typeCast = false;
//            if (Utils.listContainsItem(Utils.NGMON_ALLOWED_TYPES, ctx.type().getText().trim()) != null) {
//                varType = ctx.type().getText();
//            } else {
//                varType = "String";
//                typeCast = true;
//            }
//            logFile.storeVariable(ctx, ctx.Identifier().getText(), varType, false, null);
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
                    if (Utils.listContainsItem(Utils.BOOLEAN_OPERATORS, exp.getText()) != null) {
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
                        JavaParser.ExpressionContext log;
                        if (exp.getText().startsWith(Utils.NEGATION)) {
                            //FIX
                            log = exp.expression(0).expression(0).expression(0);
                        } else {
                            log = exp.expression(0).expression(0);
                        }
                        rewriter.replace(log.start, log.stop,
                            Utils.getQualifiedNameEnd(Utils.getNgmonLogGlobal()));
                    } else {
                        // TODO throw some kind of error
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
//                System.out.println("our extending log statements! " + ctx.expression().expression(0).expression(0).getText() +
//                    " " + logFile.getFilepath());
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
                        log.setOriginalLog(ctx.getText());
                        HelperGenerator.generateMethodName(log);
                        log.setLevel(methodCall.getText());
                        logFile.addLog(log);
                         /* TODO - ADD GOMATCH support here */
                        replaceLogMethod(ctx, log);
                        GoMatchGenerator.createGoMatch(log);
                    }
                    // else throw new exception or add it to methodList?
                }
            }
//        else {
            // ok this is not a LOG statements... we can throw it away
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
        boolean formattedLog = false;

        // TODO if expressions are separated by commas and/or first argument contains '%x' or '{}'
        // delicately handle such situation
        if (expressionList != null) {
            String methodText = expressionList.expression(0).getText();
            if (methodText.contains("{}")) {
                if (loggerLoader.getLogType().equals("slf4j")) {
                    formattedLog = true;
                    log.setFormattingSymbol("{}");
                }
            } else if (methodText.contains("%")) {
                if (loggerLoader.getLogType().equals("commons")) {
                    formattedLog = true;
                    log.setFormattingSymbol("%");
                }
            }
            if (expressionList.expression(0).getText().contains("{}") || expressionList.expression(0).getText().contains("%")) {
//                System.out.println(expressionList.getText() + " is special case!");
//                formattedLog = true;
                // if (currentFramework == slf4j) then handle 2 types of messages: '"msgs {}", logFile' and classic '"das" + das + "dsad"';
                // handle {} and "" ?
            }

            /** start evaluating of parsed value from rightmost element, continuing with left sibling */
            List<JavaParser.ExpressionContext> methodList = expressionList.expression();
            Collections.reverse(methodList);
            int counter = 0;
            for (JavaParser.ExpressionContext ec : methodList) {
                if (formattedLog && counter != methodList.size() - 1) {
                    fillCurrentLog(log, ec, true);
                } else {
                    fillCurrentLog(log, ec, false);
                }
                counter++;
            }
        } else {
            // ExpressionList is empty! That means it is 'Log.X()' statement.
            log.setTag("EMPTY_STATEMENT");
        }
        log.cleanUpCommentList();
        return log;
    }

    /**
     * Parse data from expression nodes - recursive tree of expressions in LOG.X(expression) call.
     * Successfully uses ANTLR's property of tree-building, that successive leaves are built on
     * first node, which leaves second node of tree (expression) as log comment or variable.
     *
     * @param log          Log to be filled with data from this log method statement
     * @param expression   ANTLR's internal representation of JavaParser.ExpressionContext context
     * @param formattedVar If true, first argument/expression contains declaration of whole log ( '%s' '{}')
     */
    private void fillCurrentLog(Log log, JavaParser.ExpressionContext expression, boolean formattedVar) {
        if (expression == null) {
            System.err.println("Expression is null");
            return;
        }

        int childCount = expression.getChildCount();
        if (childCount == 1) {
            determineLogTypeAndStore(log, expression, formattedVar);
        } else if (childCount == 2) {
            // TODO - LOG.fatal("Error reported on file " + f + "... exiting", new Exception()); done?
            // 'new Exception()' found only
            // TODO log trace()
//            System.out.println("2Exception=" + expression.getText() + " " + expression.getChild(0).getText() + " " + expression.creator().getText());
            // new is followed by 'creator context'
            if (expression.getChild(0).getText().equals("new")) {
                determineLogTypeAndStore(log, expression, formattedVar);
            } else {
                System.err.println("Error " + expression.getText());
            }

            /** Recursively call this method to find out more information about *this* statement */
        } else if (childCount == 3) {
//            System.out.println("==" + expression.getText());
            if (expression.expression(1) != null) {
//                System.out.format("var=%s exp(0)=%s exp(1)=%s%n", expression.getText(), expression.expression(0).getText(), expression.expression(1).getText());
                determineLogTypeAndStore(log, expression.expression(1), formattedVar);
            }

            if (expression.expression().size() <= 1) {
//                System.out.println("exp=" + expression.expression(0).getText() + " vs " + expression.getText());
                determineLogTypeAndStore(log, expression, formattedVar);
            } else {
//                System.out.println("+" + expression.expression(0).getText());
                for (JavaParser.ExpressionContext ec : expression.expression().subList(0, expression.expression().size() - 1)) {
//                System.out.println("ec=" + ec.getText());
                    fillCurrentLog(log, ec, formattedVar);
                }
            }
        } else if (childCount == 4) {
            // TODO ?
            List<String> stringList = Arrays.asList("format", "print");
//            System.out.println("asd" + expression.expression(0).getText());
            if (Utils.listContainsItem(stringList, expression.expression(0).getText()) != null) {
                for (JavaParser.ExpressionContext ch : expression.expressionList().expression()) {
//                    System.out.println("ch=" + ch.getText());
                    determineLogTypeAndStore(log, ch, formattedVar);
                }
            } else {
                String text = expression.getText();
//                System.out.println("ELSE=" + text);
                determineLogTypeAndStore(log, expression, formattedVar);
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

    public LogFile.Variable determineLogTypeAndStore(Log log, JavaParser.ExpressionContext expression, boolean formattedVariable) {
        LogFile.Variable varProperty = null;
        if (expression.getText().startsWith("\"")) {
            log.addComment(cultivate(expression.getText()));
        } else {
            varProperty = findVariable(log, expression, formattedVariable);
            log.addVariable(varProperty);
        }
        return varProperty;
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
    private LogFile.Variable findVariable(Log log, JavaParser.ExpressionContext findMe, boolean formattedVar) {
        // TODO Log.trace()
//        System.out.println("findMe " + findMe.getText() + "  " + findMe.start.getLine() + ":" + logFile.getFilepath());
        LogFile.Variable foundVar = findVariableInLogFile(logFile, findMe);
        String ngmonNewName = null;
        String findMeText = findMe.getText();
        boolean skipAddingFormattedVar = false;

        if (foundVar == null) {
            String varType;
            String varName;
            /**
             * When variables were not found, dug deeper. Special cases
             * needed to be handled, generated by special log calls
             */

            /** handle google's Joiner class something like python's (zip(map(", ")."stringList")) **/
            if (findMeText.startsWith("Joiner")) {
                varName = findMe.expressionList().expression(0).getText();
                LogFile.Variable var;
                if ((var = findVariableInLogFile(logFile, findMe.expressionList().expression(0))) != null) {
                    foundVar = var;
                } else {
                    varType = "String";
                    logFile.storeVariable(findMe, varName, varType, false, null);
                    foundVar = returnLastValue(varName);
                }

                /** Hadoop's StringUtils internal function */
            } else if (findMeText.startsWith("StringUtils")) {
//                System.out.println("SU=" + findMeText);
                logFile.storeVariable(findMe, findMeText, "String", false, "StringUtils");
                foundVar = returnLastValue(findMeText);

                /** Handle new Path creation object */
                // findMeText.startsWith("new")
            } else if (findMe.creator() != null && findMeText.contains("Path")) {
//                System.out.println("PATH " + findMeText);
                logFile.storeVariable(findMe, findMeText, "String", false, "newPath");
                foundVar = returnLastValue(findMeText);

                /** handle new Object[] {} definition in log
                 * We have to store manually all but last variables in actual log.
                 * The last variable will be returned as foundVar and stored during normal workflow. */
            } else if (findMe.creator() != null && findMeText.contains("[]") && findMeText.trim().endsWith("}")) {
                // get variables from new Object[] {var1, var2, var3,...} and store them manually
                List<LogFile.Variable> variableList;
                int i = 0;
                List<JavaParser.VariableInitializerContext> varInitList = findMe.creator().arrayCreatorRest().arrayInitializer().variableInitializer();
                // LOOP from end to start (in reversed order)
                Collections.reverse(varInitList);
                List<LogFile.Variable> reversedFormattedList = new ArrayList<>();
                for (JavaParser.VariableInitializerContext var : varInitList) {
                    if ((variableList = logFile.getVariableList().get(var.getText())) == null) {
                        logFile.storeVariable(var.expression(), var.getText(), "Object", false, removeSpecialCharsFromText(var.getText()));
                        foundVar = returnLastValue(var.getText());
                    } else {
                        foundVar = variableList.get(variableList.size() - 1); // get last value
                    }
                    // store manually all but first variable, as we always insert into 0th position.
                    if (i < varInitList.size() - 1) {
//                    if (i != 0) {
                        log.addVariable(foundVar);
                    }
                    reversedFormattedList.add(foundVar);
                    i++;
                }

                if (formattedVar) {
//                    Collections.reverse(reversedFormattedList);
                    for (LogFile.Variable v : reversedFormattedList) {
                        log.addFormattedVariables(v);
                    }
                    skipAddingFormattedVar = true;
                }

                /** handle 'new String(data, UTF_8)' or 'new Exception()' */
            } else if (findMe.creator() != null) {//findMeText.startsWith("new")) {
                // declaration of 'new String(data, ENC)' or 'new Exception()'
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


                /** If variable is array [], find declared variable earlier and use it's type.
                 * If type is not found, use String as default. */
            } else if (findMeText.contains("[") && findMeText.contains("]")) {
                varName = findMeText;

                LogFile.Variable var = findVariable(log, findMe.expression(0), formattedVar);
                if (Utils.listContainsItem(Utils.PRIMITIVE_TYPES, var.getType()) != null) {
                    varType = var.getType();
                } else {
                    varType = "String";
                }
                varName = varName.substring(0, varName.indexOf("[")) + varName.substring(varName.indexOf("]") + 1);
                ngmonNewName = removeSpecialCharsFromText(varName);
//                System.out.println("array=" + varName + " " + findMe.expression(0).getText());
//                logFile.storeVariable(findMe, varName, varType, false, ngmonNewName);
                logFile.storeVariable(findMe, findMeText, varType, false, ngmonNewName);
                foundVar = returnLastValue(findMeText);
                foundVar.setChangeOriginalName(HelperGenerator.addStringTypeCast(findMeText));


                /** if X is instanceof Y,
                 * create a new boolean variable named as 'isInstanceOfY' */
            } else if (findMeText.contains("instanceof")) {
                // Rename variables and set them before ngmon Log itself.  bc instance ofXYZ... => boolean isInstanceOfY = bc;
                // add new parameter to logFile.storeVariable() - newVariableName
                varName = findMe.primary().expression().expression(0).primary().getText();
                varType = "boolean";
                ngmonNewName = "isInstanceOf" + findMe.primary().expression().type().getText();
//                System.out.printf("var=%s newName=%s, original=%s", varName, newNgmonName, findMeText);
                logFile.storeVariable(findMe, varName, varType, false, ngmonNewName);
                foundVar = returnLastValue(varName);

                /** Check for ternary if operator in log */
            } else if (findMeText.contains("?")) {
//                System.out.println(findMe.getChildCount() + " " + findMeText);
                String tmpVarName;
                if (findMeText.startsWith("String.format")) {
                    // String.format() get log statement part after formatting string
                    tmpVarName = findMe.expressionList().expression(1).expression(0).expression(0).primary().getText();
                } else {
                    /**
                     * Create it as "isX",  addComment to log as "isX", store
                     * this boolean variable add tag to this log as "ternary" */
                    tmpVarName = findMe.primary().expression().expression(0).getText();
                }
                StringBuilder terVarName = new StringBuilder(removeSpecialCharsFromText(tmpVarName));
                String operator = Utils.listContainsItem(Utils.BOOLEAN_OPERATORS, terVarName.toString());
                if (operator != null) {
                    terVarName = terVarName.delete(terVarName.indexOf(operator), terVarName.length());
                }
                terVarName.setCharAt(0, Character.toUpperCase(terVarName.charAt(0)));
                ngmonNewName = "is" + terVarName.toString();                                       // varName -> isVarName
//                ngmonNewName = "is" + varName.replace(varName.charAt(0), (Character.toUpperCase(varName.charAt(0)))); // varName -> isVarName
                ngmonNewName = removeSpecialCharsFromText(ngmonNewName);

                varType = "String";
//                varType = findVariableInLogFile(logFile, findMe);
                logFile.storeVariable(findMe, terVarName.toString(), varType, false, ngmonNewName);
                log.setTag("ternary-operator");
                foundVar = returnLastValue(terVarName.toString());
                foundVar.setChangeOriginalName(HelperGenerator.addStringTypeCast(findMeText));

                /** Mathematical expression - use double as type */
            } else if (Utils.listContainsItem(Utils.MATH_OPERATORS, findMeText) != null) {
//                containsMathOperator(findMeText))
                varName = findMeText;
                varType = "long";
                logFile.storeVariable(findMe, varName, varType, false, "mathExpression");
                foundVar = returnLastValue(varName);

                /** Handle System.getenv() method **/
            } else if (findMeText.startsWith("System.getenv().get(")) {
                ngmonNewName = findMeText.substring("System.getenv().get(".length() + 1, findMeText.lastIndexOf(")") - 1);
                varType = "String";
                logFile.storeVariable(findMe, findMeText, varType, false, ngmonNewName);
                foundVar = returnLastValue(findMeText);

                /** Hadoop's Param.toSortedString() ... */
            } else if (findMeText.startsWith("Param.toSortedString(")) {
                varType = "String";
                logFile.storeVariable(findMe, findMeText, varType, false, "parameters");
                foundVar = returnLastValue(findMeText);

                /** Handle special '$(abc)' */
            } else if (findMeText.startsWith("$")) {
                varType = "String";
                ngmonNewName = findMeText.substring(2, findMeText.length() - 1);
                logFile.storeVariable(findMe, findMeText, varType, false, ngmonNewName);
                foundVar = returnLastValue(findMeText);


            } else if (findMeText.startsWith("String.format")) {
                // String.format() get log statement part after formatting string
                //String tmpVarName = findMe.expressionList().expression(1).expression(0).expression(0).primary().getText();
                logFile.storeVariable(findMe, findMeText, "String", false, "formattedVariable");
                foundVar = returnLastValue(findMeText);

                /**
                 * Handle call of another method in class.
                 * Start another ANTLR process, look for method declarations and return type.
                 * Put it All back here. */
            } else if (findMeText.matches("\\w+\\(.*?\\)")) {
                List<String> methodArgumentsTypeList = new ArrayList<>();
//                System.out.println("do me!" + findMe.getStart().getLine() + " " + findMeText +
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
                            // handle "null" objects
                            methodArgumentsTypeList.add("Object");
                        // Todo warn() unable to determine method's arguemnt type from ec.getText (null)
//                            System.err.println("not found " + ec.getText());
                        }
                    }
                } else {
                    methodArgumentsTypeList = null;
                }
                /** Look into extending class for this method call */
//            TODO log trace()
//                System.out.println("looking for=" + findMeText + " " + findMe.start.getLine() + " " + logFile.getFilepath());
                if (!HelperLogTranslator.findMethod(logFile, findMeText, methodArgumentsTypeList)) {
                    /** Method has not been found in class. Store it anyway.
                     * Exactly same situation as variable containing "." */
                    ngmonNewName = findMe.expression(0).getText() + "MethodCall";
                    log.setTag("methodCall");
                    logFile.storeVariable(findMe, findMeText, "String", false, ngmonNewName);
                } else {
                    // found method!
                    log.setTag("methodCall");
                }
                /** methodCall has been stored like any other 'variable' */
                foundVar = returnLastValue(findMeText);


                /**  If expression is composite - has at least one '.', use it as "variable" and change type to String
                 * 'l.getLedgerId()', 'KeeperException.create(code,path).getMessage()', ...*/
            } else if (findMeText.contains(".")) {
                StringBuilder newNgmonName = new StringBuilder(findMeText);
                if (newNgmonName.toString().endsWith(".toString()")) {
                    // delete '.toString()'
                    newNgmonName.delete(newNgmonName.lastIndexOf("."), newNgmonName.length());
                    if (newNgmonName.toString().endsWith("()")) {
                        newNgmonName.delete(newNgmonName.length() - 2, newNgmonName.length());
                    }
                    // remove all dots and brackets from methodCall and raise dot-following letter to upper case
                    newNgmonName.replace(0, newNgmonName.length(), removeSpecialCharsFromText(newNgmonName.toString()));
                    newNgmonName.append("MethodCall");
                    log.setTag("methodCall");
                } else {
                    newNgmonName.replace(0, newNgmonName.length(), removeSpecialCharsFromText(newNgmonName.toString()));
                }

                logFile.storeVariable(findMe, findMeText, "String", false, newNgmonName.toString());
                foundVar = returnLastValue(findMeText);
                foundVar.setChangeOriginalName(HelperGenerator.addStringTypeCast(findMeText));


                /** Handle 'this' call */
            } else if (findMeText.startsWith("this")) {
                if (findMeText.equals("this")) {
                    // change first letter to lowercase and use classname as 'this' ngmon's name
                    ngmonNewName = Character.toLowerCase(classname.charAt(0)) + classname.substring(1);
                    logFile.storeVariable(findMe, "this", "String", false, ngmonNewName);
                    foundVar = returnLastValue(findMeText);
                } else {
                    // We can ignore value assignment as we have parsed this.'variable';
                    System.err.println("'this.' call found method!" + findMeText);
                }


                /** If variable begins with NEGATION '!' */
            } else if (findMeText.startsWith("!")) {
                varName = findMeText.substring(1);
//                logFile.storeVariable(findMe, varName, varType, false);
                foundVar = returnLastValue(varName);

                /** if whole text is uppercase & we have static imports, assume this is static variable
                 * and store it */
            } else if (logFile.isContainsStaticImport() && findMeText.equals(findMeText.toUpperCase())) {
//               TODO Log.warn()
//                System.err.println("Assuming external variable from static import " + findMeText);
                logFile.storeVariable(findMe, findMeText, "String", false, null);
                foundVar = returnLastValue(findMeText);

                /** variable might be null */
            } else if (findMeText.equals("null")) {
                logFile.storeVariable(findMe, "null", "String", false, "null");
                foundVar = null;//returnLastValue(findMeText);

                /** 'variable' is true|false statement */
            } else if (findMeText.equals("true") || findMeText.equals("false")) {
                logFile.storeVariable(findMe, findMeText, "boolean", false, "booleanValue");
                foundVar = returnLastValue(findMeText);

                /** type casting (String) var -> var */
            } else if (findMeText.startsWith("(") && (findMeText.indexOf(")") != findMeText.length())) {
                varName = findMeText.substring(findMeText.indexOf(")") + 1).trim();
                varType = findMeText.substring(1, findMeText.indexOf(")")).trim();
                logFile.storeVariable(findMe, varName, varType, false, null);
                foundVar = returnLastValue(varName);
                foundVar.setChangeOriginalName(HelperGenerator.addStringTypeCast(varName));

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
                System.err.println("Unable to find variable " + findMeText + " in file " +
                    findMe.start.getLine() + " :" + logFile.getFilepath() + "\n" + logFile.getVariableList().keySet());
                if (Utils.ignoreParsingErrors) {
                    return null;
                } else {
                    Thread.dumpStack();
                    System.exit(100);
                }
            }
        }
        if (formattedVar && !skipAddingFormattedVar) {
            log.addFormattedVariables(foundVar);
        }
        return foundVar;
    }

    /**
     * Search for variable in given LogFile's variable list.
     *
     * @param logFile to search for this variable list
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
//        System.out.println("returning " + list + " " + logFile.getFilepath());
        if (list == null) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * Method used for dropping unnecessary symbols in comments.
     * Drop quotes, extra spaces, commas, non-alphanum characters
     * into more fashionable way for later NGMON log method naming generation.
     *
     * @param str string to be changed
     */
    private String cultivate(String str) {
//        System.out.print("cultivating  " + str);
        str = str.replace("\'", "");
        str = str.substring(1, str.length() - 1).trim(); // remove leading and trailing " "
        str = str.replaceAll("\\d+", "");   // remove all digits as well
        str = str.replaceAll("%\\w", ""); // remove all single chars
        str = str.replaceAll("\\W", " ").replaceAll("\\s+", " ").trim();
//        System.out.print("  -->" + str + "\n");
        str = str.toLowerCase();
        return str;
    }

    /**
     * Method removes dots from text and upper-cases the following letter after ".".
     * Also removes empty brackets and brackets with content are substituted with "_".
     *
     * @param text to be changed
     * @return text without dots
     */
    private String removeSpecialCharsFromText(String text) {

        // remove quotations
        text = text.replace("\"", "");

        // remove brackets, empty brackets first
        // array
        text = text.replace("[]", "");
        text = text.replace("[", "_").replace("]", "_").replace("__", "");

        text = text.replace("()", "");
        text = text.replace("(", "_").replace(")", "_").replace("__", "");
        if (text.endsWith("_")) {
            text = text.substring(0, text.lastIndexOf("_"));
        }
        if (text.startsWith("_")) {
            text = text.substring(1);
        }
        // remove commas
        text = text.replace(",", "AND");
        // remove dots
        StringBuilder newText = new StringBuilder(text);
        int dotsCount = text.length() - text.replace(".", "").length();
        int dotPos;
        for (int i = 0; i < dotsCount; i++) {
            dotPos = text.indexOf(".");
            newText.deleteCharAt(dotPos);
            newText.setCharAt(dotPos, Character.toUpperCase(newText.charAt(dotPos)));
            text = newText.toString();
        }
        return newText.toString();
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

    /**
     * Method rewrites and adds custom imports in this LogFile ANTLR's run.
     * Rewriting of imports starts from first qualified import name.
     *
     * @param context ANTLR's QualifiedNameContext - imports part
     */
    private void replaceLogImports(JavaParser.QualifiedNameContext context) {
        String namespaceImport = Utils.getNgmongLogEventsImportPrefix() + "." +
            ANTLRRunner.getCurrentFile().getNamespace() + "." +
            ANTLRRunner.getCurrentFile().getNamespaceClass() + ";";
        String logGlobalImport = "import " + Utils.getNgmonLogGlobal();
        String simpleLoggerImport = "import " + Utils.getNgmonSimpleLoggerImport() + ";";
        // Change Log import with Ngmon Log, currentNameSpace and LogGlobal imports
        rewriter.replace(context.start, context.stop, namespaceImport + "\n" +
            simpleLoggerImport + "\n" + logGlobalImport);
    }

    /**
     * Method rewrites LogFactory declaration of current Logging framework.
     *
     * @param ctx ANTLR's current rule context
     */
    private void replaceLogFactory(ParserRuleContext ctx) {
        String logFieldDeclaration = ANTLRRunner.getCurrentFile().getNamespaceClass() +
            " LOG = LoggerFactory.getLogger(" + ANTLRRunner.getCurrentFile().getNamespaceClass() + ".class, new SimpleLogger());";
//            System.out.println("replacing " + ctx.getStart() + ctx.getText() + " with " + logFieldDeclaration);
        rewriter.replace(ctx.getStart(), ctx.getStop(), logFieldDeclaration);
    }

    /**
     * Method rewrites current log method with all parsed variables into NGMON
     * syntax. Method is generated in Log object as 'generatedReplacementLog'.
     *
     * @param ctx ANTLR's JavaParser.StatementExpressionContext context
     * @param log current log instance with generated replacement log method
     */
    private void replaceLogMethod(JavaParser.StatementExpressionContext ctx, Log log) {
        String ngmonLogReplacement = HelperGenerator.generateLogMethod(logName, log);
        // TODO debug()
//        System.out.println(log.getOriginalLog());
//        System.out.println(ngmonLogReplacement + "\n");
        String commentedOriginalLog = "/* " + log.getOriginalLog() + " */";
        String spaces = HelperGenerator.generateEmptySpaces(ctx.start.getCharPositionInLine());
        rewriter.replace(ctx.start, ctx.stop, commentedOriginalLog + "\n" + spaces + ngmonLogReplacement);
        Statistics.addChangedLogMethodsCount();
    }

}
