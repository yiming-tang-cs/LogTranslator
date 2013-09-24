package cz.muni.fi.ngmon.logchanger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author mtoth
 */
public class ConvertorStarter {

    private static final Logger LOG = Logger.getLogger(ConvertorStarter.class.getCanonicalName());
    private static final String JAVA_CLASS_PATH = System.getProperty("java.class.path");
    private static final String PROJECT_PATH = System.getProperty("user.dir")
            + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;

    private static final StringBuilder classpath = new StringBuilder();

    private static final String filesToCompile = "/home/mtoth/skola/dp/LogFilterBase/forceAssertions/java7/Example.java";
//    private static final String fileToCompile = "/home/mtoth/Desktop/CodeAnalyzer.java";
    private static final String errorOutput = "/home/mtoth/Desktop/errors.txt";
    private static final String GENERATED_DIR = "/home/mtoth/Desktop/generated-sources";

    
    public static void main(String[] args) {
//        checkCreateDir(GENERATED_DIR);
//        compileProcessor();

// change java source file
        classpath.append(File.pathSeparator);
        classpath.delete(0, classpath.length());
        System.out.println("Classpath=" + classpath);        

//        classpath.append(JAVA_CLASS_PATH);
//        classpath.append("/cz/muni/fi/ngmon/logchanger/");
        classpath.append(GENERATED_DIR);
//        classpath.append(File.pathSeparator).append(".").append(File.pathSeparator).append(JAVA_CLASS_PATH);
        LOG.log(Level.INFO, "CLASSPATH = {0}", classpath);

        Iterable<String> options = Arrays.asList("-cp", classpath.toString(),
                "-processor", ForceAssertions.class.getCanonicalName(), "-printsource", "-d", GENERATED_DIR);

        for (String s : options) {
            System.out.print(s + " ");
        }
        System.out.println();
        compile(options, Arrays.asList(filesToCompile));

    }

    //
    public static void compileProcessor() {
        String processorPath = PROJECT_PATH
                + ForceAssertions.class.getCanonicalName().replace(".", File.separator) + ".java";
//        LOG.log(Level.INFO, "PROCESSOR PATH = {0}", processorPath);
        classpath.append(System.getProperty("java.home")).append("/../lib/tools.jar");

//        Iterable<String> options = Arrays.asList("-cp", classpath.toString(), "-d", JAVA_CLASS_PATH);
        Iterable<String> options = Arrays.asList("-cp", classpath.toString(), "-d", GENERATED_DIR);
        compile(options, Arrays.asList(processorPath));
    }

    //
    public static Boolean compile(Iterable<String> compilerOptions, List<String> compileList) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyDiagnosticListener myListener = new MyDiagnosticListener();
        DiagnosticCollector collector = new DiagnosticCollector();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(myListener, null, null);
        Iterable compilationUnits = fileManager.getJavaFileObjectsFromStrings(compileList);
        Boolean compilationResult;

        try {
            Writer out = new FileWriter(errorOutput);
            JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, collector, compilerOptions, null, compilationUnits);
            compilationResult = task.call();

            List<Diagnostic> diagnostics = collector.getDiagnostics();
            for (Diagnostic d : diagnostics) {
                System.out.println(d.getMessage(null));
            }

            if (compilationResult) {
                LOG.log(Level.INFO, "Compilation of file {0} was SUCCESSFUL", compilationUnits);
            } else {
                LOG.log(Level.INFO, "Compilation of file {0} has FAILED", compilationUnits);
            }
            return compilationResult;

        } catch (IOException ex) {
            Logger.getLogger(JavaTreeWalker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public static Boolean checkCreateDir(String path) {
        File directory = new File(path);

        if (directory.exists()) {
            if (directory.isDirectory()) {
//              remove everything in directory
                LOG.severe("Remove content of following directory " + directory.toString());
            }
        } else {
            directory.mkdir();
        }

        return false;
    }

    static class MyDiagnosticListener implements DiagnosticListener {

        @Override
        public void report(Diagnostic diagnostic) {
            System.out.println("Code->" + diagnostic.getCode());
            System.out.println("Column Number->" + diagnostic.getColumnNumber());
            System.out.println("End Position->" + diagnostic.getEndPosition());
            System.out.println("Kind->" + diagnostic.getKind());
            System.out.println("Line Number->" + diagnostic.getLineNumber());
            System.out.println("Message->" + diagnostic.getMessage(Locale.ENGLISH));
            System.out.println("Position->" + diagnostic.getPosition());
            System.out.println("Source" + diagnostic.getSource());
            System.out.println("Start Position->" + diagnostic.getStartPosition());
            System.out.println("\n");
        }
    }
}
