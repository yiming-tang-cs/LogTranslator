package org.ngmon.logger.logtranslator.tests;

import org.ngmon.logger.logtranslator.common.LogFile;
import org.ngmon.logger.logtranslator.common.LogFilesFinder;
import org.ngmon.logger.logtranslator.common.Utils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class SearchTest extends TestBase {
    /**
     * Test finding of 'log.x' statements in Java files. If file contains
     * at least one logging call or at least one import declaration from any
     * known logging frameworks by application, it should return it for
     * further processing.
     * <p/>
     * Tests are being run on testing environment called "ProjectTesting".
     */

    private String sep = Utils.sep;
    private Set<String> expectedFilesToProcess;

    @BeforeClass
    public void prepareEnvironment() {
        expectedFilesToProcess = new HashSet<>();

        String dir1 = ".testdir1.".replace(".", sep);
        String dir2 = ".testdir2.".replace(".", sep);

        expectedFilesToProcess.add("UserGroupInformation.java");
        expectedFilesToProcess.add(dir1 + "ThreadUtil.java");
        expectedFilesToProcess.add(dir2 + "MBeans.java");
        expectedFilesToProcess.add(dir2 + "MetricsSystemImpl.java");
        expectedFilesToProcess.add(dir2 + "ReflectionUtils.java");
    }

    @Test
    public void searchTest() {
        Set<String> testFilesToProcess = new HashSet<>();
        Set<LogFile> filesToProcess = LogFilesFinder.commenceSearch(TestBase.testDirectory.toString());
        for (LogFile logFile : filesToProcess) {
            testFilesToProcess.add(logFile.getFilepath());
        }
        int found = 0;
        int expectedFound = testFilesToProcess.size();
        for (String testFile : testFilesToProcess) {
            for (String expectedFile : expectedFilesToProcess) {
                if (testFile.contains(expectedFile)) {
                    found++;
                    break;
                }
            }
        }
//        System.out.println("testFiles=" + testFilesToProcess);
        assertEquals(found, expectedFound);
    }


}
