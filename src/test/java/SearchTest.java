import org.ngmon.logger.logtranslator.common.Utils;
import org.ngmon.logger.logtranslator.ngmonLogging.LogTranslatorNamespace;
import org.testng.annotations.BeforeTest;

import java.io.File;

public class SearchTest {
    /**
     * Create environment - create 4 test java files and 1 non java file.
     *
     */


    private static LogTranslatorNamespace LOG = Utils.getLogger();
    private File testFile;

    @BeforeTest
    private void prepareEnvironment() {

        System.setProperty("application_home", HelperMethods.TESTS_HOME);

    }




}
