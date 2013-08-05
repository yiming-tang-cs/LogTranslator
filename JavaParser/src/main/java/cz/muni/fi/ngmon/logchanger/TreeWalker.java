/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.ngmon.logchanger;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.monperrus.jexast.AstExtractor;

/**
 *
 * @author mtoth
 */
public class TreeWalker {

    public TreeWalker() {
    }

    public static CompilationUnitDeclaration[] createTree(File filePath) {
        CompilationUnitDeclaration[] tree = null;
        try {
            tree = AstExtractor.createAST(filePath);
        } catch (Throwable ex) {
            Logger.getLogger(TreeWalker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tree;
    }

    public static String[] walk(CompilationUnitDeclaration[] units) {

        int i = 0;
        for (CompilationUnitDeclaration unit : units) {

            TypeDeclaration[] types = unit.types; // unit.localTypes;
            for (TypeDeclaration t : types) {
                System.out.println(t.binding);
//                Statement statement = t.methods[0].statements[0];
//                if (statement != null) {
//                    StringBuffer str = new StringBuffer();
//                    statement.printStatement(0, str);
//                }
            }
        }

        return null; //Map of variables <String, String> - <Name, Type>

    }

    public static void writeArray(Object[] array) {
        if (array == null) {
            System.out.println("null");;
            return;
        }
        for (Object array1 : array) {
            System.out.println(array1);
        }
    }

}
