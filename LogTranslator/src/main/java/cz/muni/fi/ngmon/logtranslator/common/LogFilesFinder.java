package cz.muni.fi.ngmon.logtranslator.common;

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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Locate all files, where there is any log call.
 */
public class LogFilesFinder {

    static Map<Path, String> processFileList = new LinkedHashMap<>();

    public static Map<Path, String> startSearchIn(String loggingApplicationHome) {
        Path path = Paths.get(loggingApplicationHome);

        try {
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(path) && Files.isReadable(path)) {
                Files.walkFileTree(path, new JavaLogFinder());
                System.out.println(processFileList.size());
            } else {
                System.err.format("Location %s does not exist.%n", loggingApplicationHome);
                System.exit(15);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processFileList;
    }
}


class JavaLogFinder extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        int count = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
            for (Path path : files) {
                if (Files.isDirectory(path)) count++;
                if ((!path.toString().contains("/src/test/")) && (path.toString().endsWith(".java"))) {
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
                String importSearch = "^import.*?log.*;$";
                String logSearch = "^\\s+log[a-z]*.(.*);$";
                boolean found;
                String line;
                String packageName = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("package ")) {
                        packageName = line.substring(8, line.length()-1);
                    }
                    found = (line.toLowerCase().matches(logSearch) ||
                            (line.toLowerCase().matches(importSearch)));
                    if (found) {
//                        System.out.format("%s: line=%s%n", file.toString(), line);
                        LogFilesFinder.processFileList.put(file, packageName);
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
