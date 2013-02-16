package broadwick.data;

import com.google.common.collect.Table;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.MarkerFactory;

/**
 * Test cases for broadwick.data.DataReader class.
 */
@Slf4j
public class DataReaderTest {
    
    public DataReaderTest() {
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
     * Test of readDataSection method, of class DataReader.
     */
    @Test
    public void testReadDataSection() {
        System.out.println("readDataSection");
        DataReader instance = null;
        instance.readDataSection();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class DataReader.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        DataReader instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateSectionDefiniton method, of class DataReader.
     */
    @Test
    public void testAddElement() {
        System.out.println("addElement");
        String elementName = "";
        int elementIndex = 0;
        Map<String, Integer> elements = null;
        StringBuilder errors = null;
        boolean recordErrors = false;
        String sectionName = "";
        DataReader instance = null;
        instance.updateSectionDefiniton(elementName, elementIndex, elements, errors, recordErrors, sectionName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLookup method, of class DataReader.
     */
    @Test
    public void testGetLookup() {
        System.out.println("getLookup");
        DataReader instance = null;
        Lookup expResult = null;
        Lookup result = instance.getLookup();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
