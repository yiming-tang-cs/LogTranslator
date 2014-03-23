package cz.muni.fi.ngmon.logtranslator.generator;


import cz.muni.fi.ngmon.logtranslator.common.LogFile;
import cz.muni.fi.ngmon.logtranslator.common.Utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileCreator {

    // TODO
    // create files in applicationHome.directory/ngmon/namespaces/ or in application.directory/app/src/
    public static boolean create(LogFile logFile, NamespaceFileCreator nfc) {
        String ngmonPathString = Utils.getApplicationHome();
        Path ngmonPath = FileSystems.getDefault().getPath(ngmonPathString);
        nfc.setFilePath("pathToThisNamespace");
        System.out.println(logFile.getFilepath());
        return false;
    }
}
