package cz.muni.fi.ngmon.logtranslator.generator;

import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.util.HashSet;
import java.util.Set;

//package org.apache.hadoop.hdfs.nfs;
//
//        import cz.muni.fi.annotation.Namespace;
//        import cz.muni.fi.logger.AbstractNamespace;
//
//@Namespace
//public class NfsNamespace extends AbstractNamespace {
//
//    public AbstractNamespace MOUNT_MNT_PATH_CLIENT(String path, String client) {
//        return log(path, client);
//    }
//    public AbstractNamespace FAIL_PENDING_WRITE_NEXTOFFSET(String key_getMin, String key_getMax, String getNextOffsetUnprotected) {
//        return log(key_getMin, key_getMax, getNextOffsetUnprotected);
//    }
//}


public class NamespaceFileCreator {

    private final String NAMESPACE_JAVA_CLASS_TEMPLATE =
              "package <package-name>;\n\n"
            + "import cz.muni.fi.annotation.Namespace;\n"
            + "import cz.muni.fi.logger.AbstractNamespace;\n\n"
            + Utils.getNgmonAnnotationNamespaceImport()+ "\n"
            + "public class <NAMESPACE_CLASSNAME> extends AbstractNamespace {\n\n"
            + "    <methods>"
            + "}";


    private ST template = new ST(NAMESPACE_JAVA_CLASS_TEMPLATE);
    private Set<LogFile> logFileList = new HashSet<>();


    public void WriteOutTemplate() {
        System.out.println(template.render());
    }
}
