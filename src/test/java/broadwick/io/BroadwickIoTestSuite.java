package broadwick.io;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test Suite for broadwick.io package.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({broadwick.io.FileUtilsTest.class, broadwick.io.FileInputTest.class, broadwick.io.FileOutputTest.class})
public class BroadwickIoTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
