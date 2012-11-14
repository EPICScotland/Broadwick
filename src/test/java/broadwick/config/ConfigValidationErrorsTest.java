package broadwick.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
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
 * Test cases for broadwick.config.ConfigValidatorErrors class.
 */
@Slf4j
public class ConfigValidationErrorsTest {

    public ConfigValidationErrorsTest() {
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
    /**
     * Test of addError method, of class ConfigValidationErrors.
     */
    @Test
    public void testAddError_String() {
//        System.out.println("addError");
//        String error = "";
//        ConfigValidationErrors instance = new ConfigValidationErrors();
//        instance.addError(error);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of addError method, of class ConfigValidationErrors.
     */
//    @Test
//    public void testAddError_ConfigValidationErrorsStatus_String() {
//        System.out.println("addError");
//        Status status = null;
//        String error = "";
//        ConfigValidationErrors instance = new ConfigValidationErrors();
//        instance.addError(status, error);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getValidationErrors method, of class ConfigValidationErrors.
     */
//    @Test
//    public void testGetValidationErrors() {
//        System.out.println("getValidationErrors");
//        ConfigValidationErrors instance = new ConfigValidationErrors();
//        String expResult = "";
//        String result = instance.getValidationErrors();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of isValid method, of class ConfigValidationErrors.
     */
//    @Test
//    public void testIsValid() {
//        System.out.println("isValid");
//        ConfigValidationErrors instance = new ConfigValidationErrors();
//        boolean expResult = false;
//        boolean result = instance.isValid();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getNumErrors method, of class ConfigValidationErrors.
     */
//    @Test
//    public void testGetNumErrors() {
//        System.out.println("getNumErrors");
//        ConfigValidationErrors instance = new ConfigValidationErrors();
//        int expResult = 0;
//        int result = instance.getNumErrors();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
