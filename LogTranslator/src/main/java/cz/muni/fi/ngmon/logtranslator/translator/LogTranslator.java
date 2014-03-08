package cz.muni.fi.ngmon.logtranslator.translator;

import cz.muni.fi.ngmon.logtranslator.antlr.JavaBaseListener;
import cz.muni.fi.ngmon.logtranslator.antlr.JavaParser;
import cz.muni.fi.ngmon.logtranslator.common.Log;
import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.Utils;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LogTranslator extends JavaBaseListener {
    //    BufferedTokenStream bufferedTokens; // intended to be used with multiple channels for handling WHITESPACES and COMMENTS
    static LoggerLoader loggerLoader = null;
    LogFile logFile;
    TokenStreamRewriter rewriter;
    private int currentLine = 0;
    private String logName = null; // reference to original LOG variable name
    private String logType = null; // reference to original LOG variable type
    private boolean isExtending = false;


    public LogTranslator(BufferedTokenStream tokens, LogFile logfile) {
        rewriter = new TokenStreamRewriter(tokens);
        this.logFile = logfile;
//        logFile.setFilepath(filename);
//        rewriter.getTokenStream();
//        List<Token> cmtChannel = tokens.getHiddenTokensToRight(0, 1);
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

    }

    // ------------------------------------------------------------------------


    @Override
    public void enterClassDeclaration(@NotNull JavaParser.ClassDeclarationContext ctx) {
        if (ctx.getText().contains("extends")) {
//            System.err.println("extending! " + ctx.getChild(0).getText() + " " + ctx.getChild(1).getText() +
//              " " + ctx.getChild(2).getText() + " " + ctx.getChild(3).getText());
            isExtending = true;
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
                            "." + ANTLRRunner.getCurrentFile().getNamespaceEnd() + "Namespace";
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
                logFile.storeVariable(ctx, varName, ctx.type().getText(), true);
            } else {
                // Let's hope there are no 2 loggers defined on same line - should be impossible as well
                System.err.println("exitFieldDeclaration variableDeclarator().size() > 1!\n");
            }
        }
    }


    @Override
    public void exitLocalVariableDeclaration(@NotNull JavaParser.LocalVariableDeclarationContext ctx) {
        String varType = ctx.type().getText();
        String[] variables;

        if (ctx.variableDeclarators().variableDeclarator().size() == 1) {
            variables = new String[]{ctx.variableDeclarators().variableDeclarator(0).variableDeclaratorId().getText()};
        } else {
            // Multiple variables are defined on one line. Ugly.. handle.
            variables = new String[ctx.variableDeclarators().getChildCount()];

            for (int i = 0; i < variables.length; i++) {
                variables[i] = ctx.variableDeclarators().getChild(i).getText();
            }
        }

        for (String varName : variables) {
            logFile.storeVariable(ctx, varName, varType, false);
        }
    }

    @Override
    public void exitFormalParameterList(@NotNull JavaParser.FormalParameterListContext ctx) {
        String varName;
        String varType;
        for (JavaParser.FormalParameterContext parameter : ctx.formalParameter()) {
//            System.out.println("param=" + parameter.getText());
            varType = parameter.type().getText();
            varName = parameter.variableDeclaratorId().getText();
            logFile.storeVariable(parameter, varName, varType, false);
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
        System.out.println("constDec=" + ctx.getText() + "\t\t" + logFile.getFilepath());
        if (loggerLoader != null && loggerLoader.containsLogFactory(ctx.getText())) {
            System.out.println("constDeclLoggerloader=" + ctx.getText() + " " + ctx.constantDeclarator(0).Identifier().getText() +
                    " " + ctx.type().getText() + "\n" + logFile.getFilepath());
            if (this.logName == null) this.logName = ctx.constantDeclarator(0).Identifier().getText();
            replaceLogFactory(ctx);

        } else {
            System.out.println("storing constDecl=" + ctx.getText() + " " +
                    ctx.constantDeclarator(0).Identifier().getText() + " " + ctx.type().getText());
            logFile.storeVariable(ctx, ctx.constantDeclarator(0).Identifier().getText(), ctx.type().getText(), true);
//            System.out.printf("stored=%s %s %s%n", ctx, ctx.constantDeclarator(0).Identifier().getText(), ctx.type().getText());
        }
    }

    @Override
    public void enterCatchClause(@NotNull JavaParser.CatchClauseContext ctx) {
        // Store exception into variable list
        String errorVarName = null;
        String errorTypeName;

        if (ctx.getChild(ctx.getChildCount() - 3) != null) {
            errorVarName = ctx.getChild(ctx.getChildCount() - 3).getText();
        }

        // Check for simple 'catch (Exception e)' or
        // multi-exception 'catch (NullPointerException | IllegalArgumentException | IOException ex)' usage
        if (ctx.catchType().getChildCount() == 1) {
//            System.out.println(ctx.getChild(2).getText() + " " + ctx.getChild(3).getText());
            errorTypeName = ctx.getChild(2).getText();
        } else {
            // Store Exception as variable type name (as we can not tell which exception has higher priority)
            errorTypeName = "Exception";
        }
        logFile.storeVariable(ctx, errorVarName, errorTypeName, false);
    }

    @Override
    public void exitEnhancedForControl(@NotNull JavaParser.EnhancedForControlContext ctx) {
        if (ctx.Identifier() != null) {
//            System.out.printf("==LALALA %s %s %s%n", ctx.type().getText(), ctx.Identifier().getText(), ctx.expression().getText());
            logFile.storeVariable(ctx, ctx.Identifier().getText(), ctx.type().getText(), false);
        }
    }

    @Override
    public void exitBlockStatement(@NotNull JavaParser.BlockStatementContext ctx) {
        // Translate "if (LOG.isXEnabled())" statement to "if (LogGlobal.isXEnabled())"
        if ((ctx.statement() != null) && (ctx.statement().getChildCount() > 0)) {
            if (ctx.statement().getChild(0).getText().toLowerCase().equals("if")) {
                JavaParser.ExpressionContext exp = ctx.statement().parExpression().expression();
                if (exp.getText().startsWith(logName + ".")) {
                    // Check if Log call is in current checkerLogMethods() 'isXEnabled()'
                    ParseTree methodCall = exp.expression(0).getChild(exp.expression(0).getChildCount() - 1);

                    if (loggerLoader.getCheckerLogMethods().contains(methodCall.getText())) {
//                    if (exp.expression(0).getChild(exp.expression(0).getChildCount() - 1).getText().matches("is.*Enabled")) {
                        // Now we can safely replace logName by LogGlobal
                        JavaParser.ExpressionContext log = exp.expression(0).expression(0);
                        rewriter.replace(log.start, log.stop,
                                Utils.getQualifiedNameEnd(Utils.getNgmonLogGlobal()));
                    } else {
                        // TODO
                        System.err.println("Not implemented translation of log call! " +
                                "Don't know what to do with '" + exp.getText() + "'." + loggerLoader.getCheckerLogMethods());
                    }
                }
            }
        }
    }

    @Override
    public void exitStatementExpression(@NotNull JavaParser.StatementExpressionContext ctx) {
        // Process LOG.XYZ(stuff);
        if ((logName == null) && isExtending) {
//            if (extending, visit that class and find log declaration :) ) and use it here -- you WISH! it can be impossible to find extending class
//            there might be a chance, that this class extends otherClass, which contains defined LOG.
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
        if (ctx.getText().startsWith(logName + ".")) {
//            System.out.println("exitStmnt     = " + ctx.getText() + " " + ctx.expression().getChildCount());
            if ((ctx.expression().expression(0) != null) && (ctx.expression().expression(0).getChildCount() == 3)) {
                // Get "XYZ" Log call into methodCall
                ParseTree methodCall = ctx.expression().expression(0).getChild(2);

                // if Log.operation is in currentLoggerMethodList - transform it,
                if (loggerLoader.getTranslateLogMethods().contains(methodCall.getText())) {
//                    System.out.println("yes, '" + methodCall +"' is in current logger method list.");

                    Log log = transformMethodStatement(ctx.expression().expressionList());
                    log.generateMethodName();
                    log.setLevel(methodCall.getText());

                    log.setTag(null);
                    logFile.addLog(log);

                    // TODO add transformed method to appropriate XYZNamespace
                    // TODO create mapping file-variables/methods (LogFile - ) @DONE
                }
                // else throw new exception or add it to methodList?
            }
        } else {
            // ok this is not a LOG statements... we can throw it away?
//            System.out.println("===> " + ctx.getText() + logFile.getFilepath() + " " + ctx.start.getLine());
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
                fillCurrentLog(log, ec, isSpecial);
            }
        } else {
            // ExpressionList is empty! That means it is 'Log.X()' statement.
            log.setTag("EMPTY STATEMENT");
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
        }

        int childCount = expression.getChildCount();

        if (childCount == 1) {
            determineLogTypeAndStore(log, expression);
        } else if (childCount == 2) {
            // TODO BIG!! LOG.fatal("Error reported on file " + f + "... exiting", new Exception());
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
            if (expression.expression(1) != null) {
//                System.out.format("var=%s exp(0)=%s exp(1)=%s%n", expression.getText(), expression.expression(0).getText(), expression.expression(1).getText());
                determineLogTypeAndStore(log, expression.expression(1));
            }
            for (JavaParser.ExpressionContext ec : expression.expression().subList(0, expression.expression().size() - 1)) {
//                System.out.println("ec=" + ec.getText());
                fillCurrentLog(log, ec, isSpecial);
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
            LogFile.Variable varProperty = findVariable(expression);
            log.addVariable(varProperty);
        }
    }

    /**
     * Associate input variable with variable from known variables list.
     * This method handles some special cases of 'variable declarations' in log
     * statements. You should create your own, if you find out one.
     * Use ANTLR's grun tool to see proper structure.
     * After separating store variable using storeVariable() method.
     *
     * @param findMe ANTLR's internal representation of JavaParser.ExpressionContext context
     *               which holds variable to find
     */
    private LogFile.Variable findVariable(JavaParser.ExpressionContext findMe) {
//        System.out.println("findMe " + findMe.getText() + logFile.getFilepath());
        LogFile.Variable foundVar = null;
        for (String key : logFile.getVariableList().keySet()) {
            if (findMe.getText().equals(key)) {
                List<LogFile.Variable> variableList = logFile.getVariableList().get(findMe.getText());
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
            }
        }

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
                logFile.storeVariable(findMe, findMe.getText(), "String", false);
                foundVar = returnLastValue(findMe.getText()); //logFile.getVariableList().get(findMe.getText()).get(0);

                /** handle 'new String(data, UTF_8)' or 'new Exception()' */
            } else if (findMe.getText().contains("new")) {
                // declaration of 'new String(data, ENC)' or 'new Exception()'
                if (findMe.creator() != null) {
                    if (findMe.creator().getText().contains("Exception")) {
                        // TODO this _might_ be a problem in future
                        varName = "new " + findMe.creator().getText();
                        varType = "String";
                    } else {
                        varType = findMe.creator().createdName().getText();
                        varName = findMe.creator().classCreatorRest().arguments().expressionList().expression(0).getText();
                    }
                    logFile.storeVariable(findMe, varName, varType, false);
                    foundVar = returnLastValue(varName);

                }

                /** Handle 'this' call */
            } else if (findMe.getText().startsWith("this")) {
                if (findMe.getText().equals("this")) {
                    // do nothing!
                } else {
                    System.err.println("'this.' call found method!" + findMe.getText());
                }
                /**
                 * Handle call of another method in class.
                 * Start another ANTLR process, look for method declarations and return type.
                 * Put it All back here.
                 */
            } else if (findMe.getText().matches("\\w+(.*?)")) {
                List<String> methodArgumentsTypeList = new ArrayList<>();
//                System.out.println("do me!" + findMe.getStart().getLine() + " " + findMe.getText() +
//                        findMe.getChildCount() + findMe.expressionList().getText());

                if (findMe.expressionList() != null) {
                    LogFile.Variable tempList;
//                    System.err.println("formal params there " + findMe.expressionList().getText());
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
                // TODO make deeper method-checking, ie class is extended
                if (!HelperLogTranslator.findMethod(logFile, findMe.getText(), methodArgumentsTypeList)) {
                    // Method has not been found in class. Store it anyway.
                    // Exactly same situation as variable containing "."
                    logFile.storeVariable(findMe, findMe.getText(), "String", false);
                }
                foundVar = returnLastValue(findMe.getText());
//                System.out.println("find me " + findMe.getText());


                /** Mathematical expression - use double as type */
            } else if (Utils.listContainsItem(Utils.MATH_OPERATORS, findMe.getText())) {
//                containsMathOperator(findMe.getText()))
                varName = findMe.getText();
                varType = "double";
                logFile.storeVariable(findMe, varName, varType, false);
                foundVar = returnLastValue(varName);

                /** If variable begins with NEGATION '!' */
            } else if (findMe.getText().startsWith("!")) {
                varName = findMe.getText().substring(1);
//                logFile.storeVariable(findMe, varName, varType, false);
                foundVar = returnLastValue(varName);

                /** We have ran out of luck. Have not found given variable in my known parsing list. */
            } else {
                System.err.println("Unable to find variable " + findMe.getText() + " in file " +
                        findMe.start.getLine() + " :" + logFile.getFilepath() + "\n" + logFile.getVariableList());
                System.exit(100);
            }
        }
        return foundVar;
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

//    /**
//     * String object is composite in case it contain at least one dot following with letter(s)
//     * optionally having brackets. 'a.x, a.x(), a.x.y(a, b), ...'
//     *
//     * @param object - value to put under 'composite' investigation
//     * @return true, if object is composite
//     */
//    public boolean isComposite(String object) {
//        boolean composite = false;
//        //System.out.println("obj=" + object);
//        // TODO - make it more sophisticated ??
//        if (object.contains(".")) {
//            composite = true;
//        }
//
//        return composite;
//    }

    /**
     * Drop quotes, extra spaces, commas, non-alphanum characters
     * into more fashionable way for later ngmon log method naming generation
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
