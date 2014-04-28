package org.ngmon.logger.logtranslator.tests;

import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.generator.FileCreator;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

public class TestBase {

    protected static String sep = Utils.sep;
    protected static final String TESTS_ORIG_LOCATION = System.getProperty("user.dir") + ".src.test.resources.ProjectTesting".replace(".", sep);
    protected static Path testDirectory;  // location where to copy tmp files
    private static LogTranslatorNamespace LOG = Utils.getLogger();

    @BeforeSuite(alwaysRun = true)
    protected static void initialize() {
        String propertyFile = "src/test/resources/logtranslator-test.properties";
        Properties properties = new Properties();

        try {
            InputStream is = new FileInputStream(propertyFile);
            properties.load(is);
            testDirectory = FileCreator.createPathFromString(properties.getProperty("testing.directory"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeSuite(alwaysRun = true, dependsOnMethods = {"initialize"})
    public void copyTestingDirectory() {
        Path testsOrigLocation = FileCreator.createPathFromString(TESTS_ORIG_LOCATION);

        try {
            if (Files.exists(testDirectory)) {
                if (Files.isDirectory(testDirectory)) {
                    /** delete this directory */
                    Files.walkFileTree(testDirectory, new DeleteFilesFoldersWalker());
                }
            }
            Files.createDirectories(testDirectory);
            Files.walkFileTree(testsOrigLocation, new CopyTestDirectory(testsOrigLocation, testDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void deleteTestingDirectory() {
        if (Files.exists(testDirectory)) {
            try {
                Files.walkFileTree(testDirectory, new DeleteFilesFoldersWalker());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Copy files from one directory to another with all
     * files and subdirectories in source directory.
     */
    private class CopyTestDirectory extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;

        public CopyTestDirectory(Path fromPath, Path toPath) {
            this.fromPath = fromPath;
            this.toPath = toPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path target = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(target)) {
                Files.createDirectory(target);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path target = toPath.resolve(fromPath.relativize(file));
            Files.copy(file, target);
            return FileVisitResult.CONTINUE;
        }
    }


    /**
     * Delete files from current directory and subdirectories.
     */
    class DeleteFilesFoldersWalker extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }
}
