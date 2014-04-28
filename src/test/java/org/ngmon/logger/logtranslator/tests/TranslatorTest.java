package org.ngmon.logger.logtranslator.tests;

import org.ngmon.logger.logtranslator.common.TranslatorStarter;
import org.ngmon.logger.logtranslator.generator.FileCreator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.testng.Assert.assertEquals;


/**
 * This test compares translation of each log statement in java files,
 * provided in test application home directory.
 * Compares "static" java files ending with "-original" from ProjectTesting
 * with newly translated files from TranslatedProject folder.
 * <p/>
 * In the next step, test checks for successful compilation of generated namespaces.
 * And compilation of simpler java files.??
 */
public class TranslatorTest extends TestBase {

    @DataProvider()
    private Object[][] JavaFilesInput() {
        Object[][] mappingArray = new Object[5][2];
        String appDir = System.getProperty("user.dir");
        /** Simple mapping of static files with expected translated values
         * LogFilterBase/src/test/resources/ExpectedTranslatedProject and translated files */
        String file1 = appDir + ".src.test.resources.ExpectedTranslatedProject.".replace(".", sep) + "MBeans.java";
        String file2 = appDir + ".src.test.resources.ExpectedTranslatedProject.".replace(".", sep) + "MetricsSystemImpl.java";
        String file3 = appDir + ".src.test.resources.ExpectedTranslatedProject.".replace(".", sep) + "ReflectionUtils.java";
        String file4 = appDir + ".src.test.resources.ExpectedTranslatedProject.".replace(".", sep) + "ThreadUtil.java";
        String file5 = appDir + ".src.test.resources.ExpectedTranslatedProject.".replace(".", sep) + "UserGroupInformation.java";
        mappingArray[0][0] = file1;
        mappingArray[1][0] = file2;
        mappingArray[2][0] = file3;
        mappingArray[3][0] = file4;
        mappingArray[4][0] = file5;

        mappingArray[0][1] = testDirectory + sep + "testdir2" + sep + "MBeans.java";
        mappingArray[1][1] = testDirectory + sep + "testdir2" + sep + "MetricsSystemImpl.java";
        mappingArray[2][1] = testDirectory + sep + "testdir2" + sep + "ReflectionUtils.java";
        mappingArray[3][1] = testDirectory + sep + "testdir1" + sep + "ThreadUtil.java";
        mappingArray[4][1] = testDirectory + sep + "UserGroupInformation.java";

        return mappingArray;
    }

    @BeforeClass
    private Set<String> translateTestProject() {
        Set<String> translatedFiles = new TreeSet<>();
        String[] input = new String[]{System.getProperty("user.dir") + ".src.test.resources.".replace(".", sep) + "logtranslator-test.properties"};
        TranslatorStarter.main(input);
        return translatedFiles;
    }

    /**
     * Test compares equality of content of two java classes.
     * The first argument holds translated java class object.
     * The second argument is "static" expected result of parsing of this object.
     * Assert true, when they are same (converted correctly).
     *
     * @param testFile     java file converted by LogTranslator project
     * @param expectedFile static java file hodling an expected translated content
     */
    @Test(dataProvider = "JavaFilesInput")
    private void translationTest(String testFile, String expectedFile) {
        try {
            List<String> testFileLines = Files.readAllLines(FileCreator.createPathFromString(testFile),
                Charset.defaultCharset());

            List<String> expectedFileLines = Files.readAllLines(FileCreator.createPathFromString(expectedFile),
                Charset.defaultCharset());

            assertEquals(testFileLines.size(), expectedFileLines.size());

            System.out.println("Comparing files: " +testFile + "\n" + expectedFile);

            for (int i = 0; i < testFileLines.size(); i++) {
                assertEquals(testFileLines.get(i), expectedFileLines.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
