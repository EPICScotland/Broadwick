package broadwick.utils;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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

@Slf4j
public class ArrayUtilsTest {

    public ArrayUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
     * Test of max method, of class ArrayUtils.
     */
    @Test
    public void testMax() {
        long[] longs = {1, 2, 3, 4, 5};
        long expResult = 5L;
        long result = ArrayUtils.max(longs);
        assertEquals(expResult, result);
    }

    /**
     * Test of scale method, of class ArrayUtils.
     */
    @Test
    public void testScale() {
        long[] longs = {1, 2, 3, 4, 5};
        long[] results = {2, 4, 6, 8, 10};
        double scale = 2.0;
        ArrayUtils.scale(longs, scale);
        assertArrayEquals(longs, results);
    }

    /**
     * Test of scale method, of class ArrayUtils.
     */
    @Test
    public void testScale2() {
        long[] longs = {1, 2, 3, 4, 5};
        long[] results = {0, 0, 0, 0, 0};
        double scale = 0.0;
        ArrayUtils.scale(longs, scale);
        assertArrayEquals(longs, results);
    }

    /**
     * Test of sum method, of class ArrayUtils.
     */
    @Test
    public void testSum() {
        Double[] dbls = new Double[5];
        dbls[0] = 1.0;
        dbls[1] = 2.0;
        dbls[2] = 3.0;
        dbls[3] = 4.0;
        dbls[4] = 5.0;
        Double expResult = 15.0;
        assertEquals(ArrayUtils.sum(dbls), expResult, 0.1);
    }

    /**
     * Test of toStringArray method, of class ArrayUtils.
     */
    @Test
    public void testToStringArrayWithToken() {
        String[] arr = ArrayUtils.toStringArray("a;b;c", ';');
        assertEquals(arr.length, 3);

        arr = ArrayUtils.toStringArray("a", ';');
        assertEquals(arr.length, 1);
    }

    /**
     * Test of toStringArray method, of class ArrayUtils.
     */
    @Test
    public void testToStringArray() {
        String[] arr = ArrayUtils.toStringArray("a,b,c");
        assertEquals(arr.length, 3);
    }

    /**
     * Test of toStringArray method, of class ArrayUtils.
     */
    @Test
    public void testToCollectionStrings() {
        Collection<String> coll = ArrayUtils.toCollectionStrings("a,b,c");
        assertEquals(coll.size(), 3);
    }
}
