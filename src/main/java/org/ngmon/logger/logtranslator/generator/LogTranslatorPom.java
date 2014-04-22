package org.ngmon.logger.logtranslator.generator;

import org.ngmon.logger.logtranslator.common.Utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class LogTranslatorPom {

    private static String LOGTRANSLATOR_POM = "src" + Utils.sep + "main" +Utils.sep +
            "resources" + Utils.sep + "logtranslatorPom.xml";

    public static void create() {
        Path pomPath = FileSystems.getDefault().getPath(LOGTRANSLATOR_POM);
        Path projectPath = FileSystems.getDefault().getPath(Utils.getLogTranslatorGeneratedProject() + "pom.xml");
        try {
            if (Files.exists(projectPath)) {
                Files.delete(projectPath);
            }
            Files.copy(pomPath, projectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPath() {
        return LOGTRANSLATOR_POM;
    }
}
