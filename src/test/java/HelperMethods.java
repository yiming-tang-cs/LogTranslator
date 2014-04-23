import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelperMethods {

    private static LogTranslatorNamespace LOG = Utils.getLogger();
    protected static final String TESTS_HOME = System.getProperty("user.dir") + "/tests";
    private static final String TEST_FILE_CONTENT = "";


    protected static File createFile(String content, String filepath) {
        Path path = FileSystems.getDefault().getPath(filepath);
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                LOG.deletedFile(filepath).tag("test").info();
            }
            Files.createFile(path);
            Files.write(path, content.getBytes());
            LOG.createdFile(filepath).tag("test").info();

            return path.toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
