package broadwick.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.MarkerFactory;

/**
 * Test cases for broadwick.config.ConfigValidator class.
 */
@Slf4j
public class ConfigValidatorTest {

    public ConfigValidatorTest() {
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
//    @Test
//    public void testValidate() {
//        System.out.println("validate");
//        ConfigValidator instance = null;
//        ConfigValidationErrors expResult = null;
//        ConfigValidationErrors result = instance.validate();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testGetValidatedProject() {
//        System.out.println("getValidatedProject");
//        ConfigValidator instance = null;
//        Project expResult = null;
//        Project result = instance.getValidatedProject();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
