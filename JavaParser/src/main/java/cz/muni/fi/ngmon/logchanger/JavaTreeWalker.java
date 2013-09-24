package cz.muni.fi.ngmon.logchanger;


import com.sun.source.tree.CompilationUnitTree;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author mtoth
 */
public class JavaTreeWalker {

    private final File file;
    private Logger LOG = Logger.getLogger(JavaTreeWalker.class.getCanonicalName());

    public JavaTreeWalker(File file) {
        this.file = file;
    }

    public void initialize() {
        JavaCompiler jcompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager sjfm = jcompiler.getStandardFileManager(null, null, null);

        Iterable< ? extends JavaFileObject> compilationUnits = sjfm.getJavaFileObjects(file);
        Iterator iter = compilationUnits.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            LOG.info(o.toString());
        }

        CompilationTask task = jcompiler.getTask(null, sjfm, null, null, null, compilationUnits);
        if (task.call()) {
            LOG.log(Level.INFO, "Compilation of file {0} SUCCESSFUL", file.toString());
        } else {
            LOG.log(Level.INFO, "Compilation of file {0} FAILED", file.toString());
        }

        CompilationUnitTree cut;
    }

    public void initialize2() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyDiagnosticListener myListener = new MyDiagnosticListener();
        DiagnosticCollector collector = new DiagnosticCollector();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(myListener, null, null);

        
        String fileToCompile = "/home/mtoth/Desktop/CodeAnalyzer.java";
        OutputStream errorStream = null;
        
        try {
            Writer out = new FileWriter("/home/mtoth/Desktop/errors.txt");

//        try {
//            errorStream = new FileOutputStream("/home/mtoth/Desktop/errors.txt");
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(JavaTreeWalker.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        int compilationResult = compiler.run(null, null, errorStream, fileToCompile);
//        int compilationResult = compiler.run(System.in, System.out, System.err, "-verbose", fileToCompile);
            Iterable filesToCompile = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileToCompile));

            CompilationTask task = compiler.getTask(out, fileManager, collector, null, null, filesToCompile);
            Boolean compilationResult = task.call();

            List<Diagnostic> diagnostics = collector.getDiagnostics();
            for (Diagnostic d : diagnostics) {
                System.out.println(d.getMessage(null));
            }
            if (compilationResult) {
                LOG.log(Level.INFO, "Compilation of file {0} was SUCCESSFUL", fileToCompile);
            } else {
                LOG.log(Level.INFO, "Compilation of file {0} has FAILED", fileToCompile);
            }
        } catch (IOException ex) {
            Logger.getLogger(JavaTreeWalker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class MyDiagnosticListener implements DiagnosticListener {

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
