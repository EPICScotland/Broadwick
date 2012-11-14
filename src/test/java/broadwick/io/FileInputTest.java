package broadwick.io;

import java.io.IOException;
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
 * Test cases for broadwick.io.FileInput class.
 */
@Slf4j
public class FileInputTest {

    public FileInputTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        testsFileName = FileInputTest.class.getClass().getResource("/FileInputDummy.csv").getFile();

        // TODO BUG there is a bug in the windows impelmentation of java that prepends a 
        // "/" to the file name. We need to strip it here
        if (System.getProperty("os.name").startsWith("Windows")
                && testsFileName.startsWith("/")) {
            testsFileName = testsFileName.substring(1);
        }
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
    public void testclose() {
        try {
            FileInput instance = new FileInput(testsFileName);
            instance.readLine();
            instance.readLine();
            instance.close();
            try {
                instance.readLine();
                fail("Expected IOException to be thrown after using a method on a closed FileInput object.");
            } catch (IOException ex) {
                // we expect an IOException to be caught here because the FileInput object is closed when the readLine 
                // method is called.
            }
        } catch (IOException ex) {
            fail("Caught IOException in testClose()");
        }
    }

    private static String testsFileName;
}
