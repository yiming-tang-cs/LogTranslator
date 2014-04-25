package org.ngmon.logger.logtranslator.common;

import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.ngmon.logger.logtranslator.translator.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locate all files, where there is any log call.
 */
public class LogFilesFinder {

    static List<LogFile> processFiles = new ArrayList<>();
    static List<LogFile> processFilesNoLogDeclaration = new ArrayList<>();
    static SortedSet<String> allJavaFiles = new TreeSet<>();
    private static LogTranslatorNamespace LOG = Utils.getLogger();
    protected static Set<String> excludeFilesList = null;

    public static List<LogFile> commenceSearch(String loggingApplicationHome) {
        Path path = Paths.get(loggingApplicationHome);

        try {
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(path) && Files.isReadable(path)) {
                Files.walkFileTree(path, new JavaLogFinder());
            } else {
                LOG.locationDoesNotExists(loggingApplicationHome).error();
//                System.err.format("Location %s does not exist.%n", loggingApplicationHome);
                System.exit(15);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("NO LOG def found=" + processFilesNoLogDeclaration.size());
        processFiles.addAll(processFilesNoLogDeclaration);
        return processFiles;
    }

    public static SortedSet<String> getAllJavaFiles() {
        return allJavaFiles;
    }


    protected static boolean isFileOnExcludeList(String logFilePath) {
        if (excludeFilesList == null) {
            excludeFilesList = new HashSet<>();
            Path excludePath = FileSystems.getDefault().getPath("src.main.resources.exclude-list".replaceAll("\\.", Utils.sep));
            try {
                excludeFilesList.addAll(Files.readAllLines(excludePath, Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return excludeFilesList.contains(logFilePath);
    }

}

/**
 * Class looks for 'log' statements in files from current directory and adds
 * files to list of files, which will be later translated by ANTLR.
 */
class JavaLogFinder extends SimpleFileVisitor<Path> {

    private static List<String> importList;
    private static List<String> classStartList = Arrays.asList("class", "interface", "enum", "annotation");
    private static LogTranslatorNamespace LOG = Utils.getLogger();

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
                if ((!path.toString().contains("/src/test/") || !path.toString().contains("/target/classes")) && (path.toString().endsWith(".java"))) {
                    count++;
                    LogFilesFinder.allJavaFiles.add(path.toString());
                }
            }
        }
        // Skip this tree, it contains no directories or no java files
        if (count == 0) {
//           TODO debug() System.out.println("skipping tree " + dir);
            LOG.skippingDirectoryTree(dir.toString()).debug();
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // Exclude all files in maven test/ directory and process only "java" files
        if (file.toString().endsWith(".java") && (!file.toString().contains("/src/test/"))
                && (!file.toString().contains("/Test")) && (!file.toString().contains("/target/"))) {

            // If this file contains 'import *log*;' or '*log*.(*);' statement
            // add file to processFileList -- make list rather bigger then shorter
            try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {

                /**
                 * 1) Search imports first - iterate import in our map if it contains "log"
                 * 2) If foundLog, add file to processFiles
                 * 3) if not foundLog in imports, look for suspicious LOG
                 */

//                String logSearch = "^\\s*\\.*?log[a-z]*\\.(trace|debug|info|warn|error|fatal|log)\\(.*\\).*$";
                Pattern logSearch = Pattern.compile("^\\s*\\.*?log[a-z]*\\.(trace|debug|info|warn|error|fatal|log)\\(\\.*");
                Matcher matcher;
                boolean foundLog = false;
                boolean foundImport = false;
                boolean searchLogsOnly = false;
                String line;
                String packageName;

                LogFile logFile = new LogFile(file.toString());

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!searchLogsOnly) {
                        if (line.startsWith("package ")) {
                            packageName = line.substring(8, line.length() - 1);
                            logFile.setPackageName(packageName);
                        }
                        if (line.startsWith("import")) {
                            String lineTemp = line.substring("import ".length(), line.length() - 1);
                            foundImport = Utils.itemInList(importList, lineTemp);
                        }
                        if (Utils.listContainsItem(classStartList, line) != null) {
                            // search logs only from now, but be stricter/more effective
                            searchLogsOnly = true;
                        }
                    } else {
                        /** There is high possibility that there is no logger.
                         *  Quick search only for 'log.method(*)' in file. */
                        matcher = logSearch.matcher(line.toLowerCase());
                        if (matcher.find()) {
                            foundLog = true;
                        }
                        if (foundLog) {
                            // TODO debug() System.out.println("XXX found log call! " + line + " " + file);
                            LOG.foundLogCall(line, file.toString()).trace();
                        }
                    }

                    if (foundImport) {
                        if (!LogFilesFinder.isFileOnExcludeList(file.toString())) {
                            LogFilesFinder.processFiles.add(logFile);
                        }
                        break;
                    } else if (foundLog) {
                        if (!LogFilesFinder.isFileOnExcludeList(file.toString())) {
                            LogFilesFinder.processFilesNoLogDeclaration.add(logFile);
                        }
                        break;
                    }
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        LOG.fileError(exc.toString()).error();
//        System.err.println("File error!");
        return FileVisitResult.CONTINUE;
    }
}
