package broadwick.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.MarkerFactory;

/**
 * Test cases for broadwick.io.FileUtils class.
 */
@Slf4j
public class FileUtilsTest {

    public FileUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @ClassRule // the magic is done here
    public static TestRule classWatchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            log.info(MarkerFactory.getMarker("TEST"), "    Running tests from {} ...", description.getClassName());
        }

    };
    @Rule
    public TestRule watchman = new TestWatcher() {
        @Override
        public Statement apply(Statement base, Description description) {
            log.info(MarkerFactory.getMarker("TEST"), "        Running {} ...", description.getMethodName());
            return base;
        }

    };
    @Test
    public void testRenameFileExtension() {
        String file1 = "test.dat";
        String file2 = "test.csv";
        String file3 = "test.txt";
        String file4 = "test";

        assertEquals(FileUtils.renameFileExtension(file1, "data"), "test.data");
        assertEquals(FileUtils.renameFileExtension(file2, "1.csv"), "test.1.csv");
        assertEquals(FileUtils.renameFileExtension(file2, "_1.csv"), "test._1.csv");
        assertEquals(FileUtils.renameFileExtension(file3, "txt"), file3);
        assertEquals(FileUtils.renameFileExtension(file3, "txt"), "test.txt");
        assertEquals(FileUtils.renameFileExtension(file4, "png"), "test.png");
    }

    @Test
    public void testGetFileExtension() {
        String file1 = "test.dat";
        String file2 = "test.csv";
        String file3 = "test.txt";

        assertEquals(FileUtils.getFileExtension(file1), "dat");
        assertEquals(FileUtils.getFileExtension(file2), "csv");
        assertEquals(FileUtils.getFileExtension(file3), "txt");
    }

}
