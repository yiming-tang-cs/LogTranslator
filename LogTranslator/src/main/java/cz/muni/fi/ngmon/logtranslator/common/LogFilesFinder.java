package cz.muni.fi.ngmon.logtranslator.common;

import cz.muni.fi.ngmon.logtranslator.translator.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Locate all files, where there is any log call.
 */
public class LogFilesFinder {

    static List<LogFile> processFiles = new ArrayList<>();

    public static List<LogFile> commenceSearch(String loggingApplicationHome) {
        Path path = Paths.get(loggingApplicationHome);

        try {
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(path) && Files.isReadable(path)) {
                Files.walkFileTree(path, new JavaLogFinder());
//                System.out.println(processFileList.size());
            } else {
                System.err.format("Location %s does not exist.%n", loggingApplicationHome);
                System.exit(15);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processFiles;
    }
}

/**
 * Class looks for 'log' statements in files from current directory and adds
 * files to list of files, which will be later translated by ANTLR.
 *
 */
class JavaLogFinder extends SimpleFileVisitor<Path> {

    private static List<String> importList;
    private static List<String> classStartList = Arrays.asList("class", "interface", "enum", "annotation");

    static {
        Map<String, List<String>> logFws = LoggerFactory.getLoggingFrameworks();
        List<String> helper = new ArrayList<>();
        importList = new ArrayList<>();

        for (String key : logFws.keySet()) {
            helper.addAll(logFws.get(key));
        }

        for (String item : helper) {
            if (item != null) {
                importList.add(item);
            }
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        int count = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
            for (Path path : files) {
                if (Files.isDirectory(path)) {
                    count++;
                    continue;
                }
                if ((!path.toString().contains("/src/test/") || !path.toString().contains("/target/classes"))  && (path.toString().endsWith(".java"))) {
                    count++;
                }
            }
        }
        // Skip this tree, it contains no directories or no java files
        if (count == 0) {
            System.out.println("skipping tree " + dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // Exclude all files in maven test/ directory and process only "java" files
        if (file.toString().endsWith(".java") && (!file.toString().contains("/src/test/"))) {
            // If this file contains 'import *log*;' or '*log*.(*);' statement
            // add file to processFileList -- make list rather bigger then shorter
            try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {

                /**
                 * 1) Search imports first - iterate import in our map if it contains "log"
                 * 2) If foundLog, add file to processFiles
                 * 3) if not foundLog in imports, look for suspicious LOG
                 */

                String logSearch = "^\\.*?log[a-z]*\\.(trace|debug|warn|error|fatal|log)\\(.*\\).*$";
                boolean foundLog = false;
                boolean foundImport = false;
                boolean searchLogsOnly = false;
                String line;
                String packageName = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!searchLogsOnly) {
                        if (line.startsWith("package ")) {
                            packageName = line.substring(8, line.length()-1);
                        }
                        if (line.startsWith("import")) {
                            foundImport = Utils.listContainsItem(importList, line);
                        }
                        if (Utils.listContainsItem(classStartList, line)) {
                            // search logs only from now, but be stricter/more effective
                            searchLogsOnly = true;
                        }
                    } else {
                        /** There is high possibility that there is no logger. Quick search only */
                        foundLog = line.toLowerCase().matches(logSearch);
//                        if (foundLog) System.err.println("XXX found log call! " + line + " " + file);
                    }
                    if (foundLog || foundImport) {
//                        System.out.format("%s: line=%s %s%n", file.toString(), packageName, line);
                        LogFile logFile = new LogFile(file.toString());
                        logFile.setPackageName(packageName);
                        LogFilesFinder.processFiles.add(logFile);
                        break;
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("File error!");
        return FileVisitResult.CONTINUE;
    }
}
