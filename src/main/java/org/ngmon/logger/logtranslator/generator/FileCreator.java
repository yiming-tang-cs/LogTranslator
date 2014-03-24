package org.ngmon.logger.logtranslator.generator;


import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.Utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileCreator {

    // TODO
    // create files in applicationHome.directory/ngmon/namespaces/ or in application.directory/app/src/
    public static boolean prepareNamespace(LogFile logFile, NamespaceFileCreator nfc) {
        String ngmonPathString = Utils.getApplicationHome();
        Path ngmonPath = FileSystems.getDefault().getPath(ngmonPathString);
        nfc.setFilePath("pathToThisNamespace");
        System.out.println(logFile.getFilepath());
        return false;
    }

    /**
     * Write all files into appropriate locations
     */
    public static void flush() {
        for (NamespaceFileCreator nfc : NgmonNamespaceFactory.getNamespaceFileCreatorSet()) {
//            createFile(nfc);
        }
    }
}
