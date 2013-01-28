package broadwick.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
        try (FileInput instance = new FileInput(testsFileName)) {
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

    @Test
    public void testReadNextLineAsTokensUsingCommaSeparator() {
        try (FileInput instance = new FileInput(testsFileName, ",")) {

            List<String> line = instance.readLine();
            assertTrue(Arrays.asList("token1", "token2", "token3").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("apples", "oranges", "pears").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("one", "two", "three").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("a", "b", "c", "d", "e", "f").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("unos", "dos", "tres", "", "quatorze").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("last", "complete", "line", "").equals(line));

            line = instance.readLine();
            assertTrue(line.isEmpty());
        } catch (IOException ex) {
            fail("Caught IOException in testReadNextLine()");
        }
    }

    @Test
    public void testReadNextLineAsTokensUsingDefaultSeparator() {
        try (FileInput instance = new FileInput(testsFileName)) {
            List<String> line = instance.readLine();
            assertTrue(Arrays.asList("token1", "token2", "token3").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("apples", "", "oranges", "", "pears").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("one", "", "", "two", "", "", "three").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("a", "b", "c", "d", "e", "f").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("unos", "", "dos", "tres", "", "quatorze").equals(line));

            line = instance.readLine();
            assertTrue(Arrays.asList("last", "", "complete", "", "line", "").equals(line));

            line = instance.readLine();
            assertTrue(line.isEmpty());
        } catch (IOException ex) {
            fail("Caught IOException in testReadNextLine()");
        }
    }

    @Test
    public void testReadNextLine() {
        try (FileInput instance = new FileInput(testsFileName)) {

            String line = instance.readNextLine();
            assertTrue("token1,token2,token3".equals(line));

            line = instance.readNextLine();
            assertTrue("apples, oranges, pears".equals(line));

            line = instance.readNextLine();
            assertTrue("one , two , three".equals(line));

            line = instance.readNextLine();
            assertTrue("a,b,c,d,e,f".equals(line));

            line = instance.readNextLine();
            assertTrue("unos, dos,tres,,quatorze".equals(line));

            line = instance.readNextLine();
            assertTrue("last, complete, line,".equals(line));

            line = instance.readNextLine();
            assertNull(line);

        } catch (IOException ex) {
            fail("Caught IOException in testReadNextLine()");
        }
    }

    @Test
    public void testRead() {
        try (FileInput instance = new FileInput(testsFileName)) {

            String str = "token1,token2,token3\n\napples, oranges, pears\none , two , three\na,b,c,d,e,f\nunos, dos,tres,,quatorze\n\nlast, complete, line,\n# comment1, comment2, comment3\n\n";
            String contents = instance.read();
            assertTrue(str.equals(contents));

        } catch (IOException ex) {
            fail("Caught IOException in testReadNextLine()");
        }
    }

    private static String testsFileName;
}
