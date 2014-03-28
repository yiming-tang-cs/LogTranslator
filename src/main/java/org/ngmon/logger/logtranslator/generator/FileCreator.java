package org.ngmon.logger.logtranslator.generator;


import org.ngmon.logger.logtranslator.common.Utils;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCreator {

    static String ngmonLogsDir;
    static final String sep = File.separator;
    /**
     * Create directory for all NGMON's log events for this particular application.
     * By default it is in <applicationHome>/src/main/java/log_events/<app-namespace>
     */
    public static void createNGMONLogDirectoryPath() {

        StringBuilder newNgmonPath = new StringBuilder(sep + "src" + sep + "main" + sep + "java" + sep + "log_events" + sep);
        String appHome = Utils.getApplicationHome();
        if (appHome.endsWith("\\") || appHome.endsWith("/")) {
            appHome = appHome.substring(0, appHome.length()-1);
        }
        ngmonLogsDir = appHome + newNgmonPath;
    }

    // TODO
    public static void addLogGlobalLogger() {

    }

    /**
     * Create default NGMON's log_events directory.
     * Create all appropriate namespace files and appropriate directories.
     * Write all files into appropriate locations.
     * Add LogGlobal to handle all native calls like 'isXEnabled()'
     */
    public static void flushNamespaces() {
        createNGMONLogDirectoryPath();
        for (NamespaceFileCreator nfc : NgmonNamespaceFactory.getNamespaceFileCreatorSet()) {
            String dir = ngmonLogsDir + nfc.getNamespace().replace(".", File.separator);
            String filepath = dir + sep + nfc.getNamespaceClassName() + ".java";

            System.out.println("Path=" + dir);
            createDirectory(createPathFromString(dir));

            System.out.println("File=" + filepath);
            createFile(createPathFromString(filepath), nfc.getNamespaceFileContent());
        }
    }


    /**
     * Create directory on filesystem.
     *
     * @param dir directory to be created.
     * @return Path object to newly created directory
     */
    public static Path createDirectory(Path dir) {

        Path dirPath = null;
        try {
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                dirPath = Files.createDirectories(dir);
            } else {
                dirPath = dir;
            }
        } catch (IOException e) {
            System.err.println("Unable to create NGMON directory " + dir.toString());
            e.printStackTrace();
        }
        return dirPath;
    }

    public static Path createFile(Path file, ST template) {
        Path filepath = null;
        try {
            if (!Files.exists(file)) {
                // create new file
                filepath = Files.createFile(file);
            } else if (Files.exists(file) && Files.isRegularFile(file)) {
                // replace old file by new one
                Files.delete(file);
                Files.createFile(file);
            } else {
                throw new FileAlreadyExistsException("Unable to crate file, already exists. " + file.toString());
            }
            Files.write(file, template.render().getBytes());
        } catch (IOException e) {
            System.err.println("Unable to create NGMON directory " + file.toString());
            e.printStackTrace();
        }
        return filepath;
    }

    public static Path createPathFromString(String path) {
        return FileSystems.getDefault().getPath(path);
    }
}
