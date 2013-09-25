/*
 * Copyright 2012 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.statistics.distributions;

import java.util.Collection;
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

@Slf4j
public class IntegerDistributionTest {
    
    public IntegerDistributionTest() {
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
     * Test of reset method, of class IntegerDistribution.
     */
    @Test
    public void testReset_int() {
        IntegerDistribution h1 = new IntegerDistribution(5);
        assertEquals(5, h1.getNumBins());
        h1.setData(1, 1);
        h1.setData(2, 2);
        h1.setData(3, 3);
        h1.setData(4, 4);
        h1.setData(5, 5);

        assertEquals("1,2,3,4,5", h1.toCsv());

        h1.reset(5);
        assertEquals("5,5,5,5,5", h1.toCsv());
        h1.reset(0);
        assertEquals("0,0,0,0,0", h1.toCsv());
    }

    /**
     * Test of reset method, of class IntegerDistribution.
     */
    @Test
    public void testReset_0args() {
        IntegerDistribution h1 = new IntegerDistribution(5);
        assertEquals(5, h1.getNumBins());
        h1.setData(1, 1);
        h1.setData(2, 2);
        h1.setData(3, 3);
        h1.setData(4, 4);
        h1.setData(5, 5);

        assertEquals("1,2,3,4,5", h1.toCsv());
        h1.reset();
        assertEquals("0,0,0,0,0", h1.toCsv());
    }

    /**
     * Test of add method, of class IntegerDistribution.
     */
    @Test
    public void testAdd() {
        IntegerDistribution h1 = new IntegerDistribution(3);
        IntegerDistribution h2 = new IntegerDistribution(3);

        h2.setData(1);
        h2.setData(2, 4);

        assertEquals(3, h1.getNumBins());
        assertEquals(3, h2.getNumBins());
        assertEquals("0,0,0", h1.toCsv());
        assertEquals("1,4,0", h2.toCsv());

        h1.add(h2);
        assertEquals(3, h1.getNumBins());
        assertEquals(3, h2.getNumBins());
        assertEquals(h1.toCsv(), h2.toCsv());
    }

    /**
     * Test of getNumBins method, of class IntegerDistribution.
     */
    @Test
    public void testGetNumBins() {
        IntegerDistribution h1 = new IntegerDistribution();
        assertEquals(0, h1.getNumBins());
        h1.reset(5);
        assertEquals(0, h1.getNumBins());
        h1.reset();
        assertEquals(0, h1.getNumBins());

        h1 = new IntegerDistribution(10);
        assertEquals(10, h1.getNumBins());
    }

    /**
     * Test of setData method, of class IntegerDistribution.
     */
    @Test
    public void testSetData_Integer() {
        IntegerDistribution h = new IntegerDistribution(3);

        h.setData(1);
        assertEquals(3, h.getNumBins());
        assertEquals("1,0,0", h.toCsv());
        h.setData(1);
        assertEquals("2,0,0", h.toCsv());
        h.setData(1);
        h.setData(2);
        h.setData(3);
        assertEquals("3,1,1", h.toCsv());
    }

    /**
     * Test of setData method, of class IntegerDistribution.
     */
    @Test
    public void testSetData_Integer_Integer() {
        IntegerDistribution h = new IntegerDistribution(3);

        h.setData(1, 1);
        assertEquals(3, h.getNumBins());
        assertEquals("1,0,0", h.toCsv());
        h.setData(1, 0);
        assertEquals("0,0,0", h.toCsv());
        h.setData(1, 5);
        h.setData(2);
        h.setData(3, 3);
        assertEquals("5,1,3", h.toCsv());
        h.setData(2, -10);
        assertEquals("5,-10,3", h.toCsv());
    }

    /**
     * Test of getData method, of class IntegerDistribution.
     */
    @Test
    public void testGetData() {
        IntegerDistribution h = new IntegerDistribution(3);
        assertEquals(Integer.valueOf(0), h.getData(1));
        assertEquals(Integer.valueOf(0), h.getData(2));
        assertEquals(Integer.valueOf(0), h.getData(3));

        h.setData(1);
        assertEquals(Integer.valueOf(1), h.getData(1));
        assertEquals(Integer.valueOf(0), h.getData(2));
        assertEquals(Integer.valueOf(0), h.getData(3));

        h.setData(1);
        h.setData(2, 4);
        h.setData(3, 9);
        assertEquals(Integer.valueOf(2), h.getData(1));
        assertEquals(Integer.valueOf(4), h.getData(2));
        assertEquals(Integer.valueOf(9), h.getData(3));
    }

    /**
     * Test of getRandomBin method, of class IntegerDistribution.
     */
    @Test
    public void testGetRandomBin() {
        IntegerDistribution h1 = new IntegerDistribution(5);
        h1.setData(1, 11);
        h1.setData(2, 22);
        h1.setData(3, 33);
        h1.setData(4, 44);
        h1.setData(5, 55);
        
        Integer randomBin = h1.getRandomBin();
        assertTrue(h1.getBins().contains(randomBin));
    }

    /**
     * Test of getBinContents method, of class IntegerDistribution.
     */
    @Test
    public void testGetBinContents() {
        IntegerDistribution h1 = new IntegerDistribution(5);
        h1.setData(1, 1);
        h1.setData(2, 2);
        h1.setData(3, 3);
        h1.setData(4, 4);
        h1.setData(5, 5);
        
        Collection<Integer> binContents = h1.getBinContents();
        assertTrue(binContents.size() == 5);
        assertTrue(binContents.contains(1));
        assertTrue(binContents.contains(2));
        assertTrue(binContents.contains(3));
        assertTrue(binContents.contains(4));
        assertTrue(binContents.contains(5));
    }

    /**
     * Test of copy method, of class IntegerDistribution.
     */
    @Test
    public void testCopy() {
        IntegerDistribution h = new IntegerDistribution(5);
        h.setData(1, 0);
        h.setData(2, 4);
        h.setData(3);
        h.setData(4, 2);
        h.setData(5, 5);

        assertEquals("0,4,1,2,5", h.toCsv());
        assertEquals("0,4,1,2,5", h.copy().toCsv());
        assertEquals(h.toString(), h.copy().toString());
    }

    /**
     * Test of getSumCounts method, of class IntegerDistribution.
     */
    @Test
    public void testGetSumCounts() {
        IntegerDistribution h = new IntegerDistribution(5);
        h.setData(1, 0);
        h.setData(2, 4);
        h.setData(3);
        h.setData(4, 2);
        h.setData(5, 5);

        assertEquals(Integer.valueOf(12), h.getSumCounts());
        h.setData(3);
        assertEquals(Integer.valueOf(13), h.getSumCounts());
        h.setData(1, 10);
        assertEquals(Integer.valueOf(23), h.getSumCounts());
    }

    /**
     * Test of getBins method, of class IntegerDistribution.
     */
    @Test
    public void testGetBins() {
        IntegerDistribution h = new IntegerDistribution(5);
        assertEquals(5, h.getNumBins());
        assertEquals(5, h.getBins().size());

        assertTrue(h.getBins().contains(Integer.valueOf(1)));
        assertTrue(h.getBins().contains(Integer.valueOf(2)));
        assertTrue(h.getBins().contains(Integer.valueOf(3)));
        assertTrue(h.getBins().contains(Integer.valueOf(4)));
        assertTrue(h.getBins().contains(Integer.valueOf(5)));
    }

    /**
     * Test of size method, of class IntegerDistribution.
     */
    @Test
    public void testSize() {
        IntegerDistribution h = new IntegerDistribution(5);
        assertEquals(5, h.size());
        h = new IntegerDistribution(50);
        assertEquals(50, h.size());
        h = new IntegerDistribution(2);
        assertEquals(2, h.size());
        h = new IntegerDistribution(5472);
        assertEquals(5472, h.size());
    }

    /**
     * Test of toArray method, of class IntegerDistribution.
     */
    @Test
    public void testToArray() {
        IntegerDistribution h = new IntegerDistribution(5);
        h.setData(1, 0);
        h.setData(2, 4);
        h.setData(3);
        h.setData(4, 2);
        h.setData(5, 5);

        int[] arr = h.toArray();
        assertEquals(arr[0], 0);
        assertEquals(arr[1], 4);
        assertEquals(arr[2], 1);
        assertEquals(arr[3], 2);
        assertEquals(arr[4], 5);
    }

    /**
     * Test of toLongArray method, of class IntegerDistribution.
     */
    @Test
    public void testToLongArray() {
        IntegerDistribution h = new IntegerDistribution(5);
        h.setData(1, 0);
        h.setData(2, 4);
        h.setData(3);
        h.setData(4, 2);
        h.setData(5, 5);

        long[] arr = h.toLongArray();
        assertEquals(arr[0], 0);
        assertEquals(arr[1], 4);
        assertEquals(arr[2], 1);
        assertEquals(arr[3], 2);
        assertEquals(arr[4], 5);
    }

    /**
     * Test of scaleBins method, of class IntegerDistribution.
     */
    @Test
    public void testScaleBins() {
        IntegerDistribution h = new IntegerDistribution(5);
        h.setData(1, 0);
        h.setData(2, 4);
        h.setData(3);
        h.setData(4, 2);
        h.setData(5, 5);

        IntegerDistribution h2 = h.scaleBins(2.5);
        assertEquals(Integer.valueOf(0), h2.getData(1));
        assertEquals(Integer.valueOf(10), h2.getData(2));
        assertEquals(Integer.valueOf(2), h2.getData(3));
        assertEquals(Integer.valueOf(5), h2.getData(4));
        assertEquals(Integer.valueOf(12), h2.getData(5));

        h2 = h.scaleBins(1);
        assertEquals(Integer.valueOf(0), h2.getData(1));
        assertEquals(Integer.valueOf(4), h2.getData(2));
        assertEquals(Integer.valueOf(1), h2.getData(3));
        assertEquals(Integer.valueOf(2), h2.getData(4));
        assertEquals(Integer.valueOf(5), h2.getData(5));

        h2 = h2.scaleBins(2);
        assertEquals(Integer.valueOf(0), h2.getData(1));
        assertEquals(Integer.valueOf(8), h2.getData(2));
        assertEquals(Integer.valueOf(2), h2.getData(3));
        assertEquals(Integer.valueOf(4), h2.getData(4));
        assertEquals(Integer.valueOf(10), h2.getData(5));

        h = h2;
        assertEquals(Integer.valueOf(0), h.getData(1));
        assertEquals(Integer.valueOf(8), h.getData(2));
        assertEquals(Integer.valueOf(2), h.getData(3));
        assertEquals(Integer.valueOf(4), h.getData(4));
        assertEquals(Integer.valueOf(10), h.getData(5));
    }

    /**
     * Test of normaliseBins method, of class IntegerDistribution.
     */
    @Test
    public void testNormaliseBins() {
        IntegerDistribution h = new IntegerDistribution(10);
        h.setData(1, 9404);
        h.setData(2, 9369);
        h.setData(3, 4718);
        h.setData(4, 1957);
        h.setData(5, 897);
        h.setData(6, 509);
        h.setData(7, 270);
        h.setData(8, 149);
        h.setData(9, 103);
        h.setData(10, 111);

        assertEquals("9404,9369,4718,1957,897,509,270,149,103,111", h.toCsv());

        Integer sumCounts = 78;
        IntegerDistribution h2 = h.normaliseBins(sumCounts);
        assertEquals(h2.getSumCounts(), sumCounts);

        h.setData(1, 5951);
        h.setData(2, 3879);
        h.setData(3, 1325);
        h.setData(4, 604);
        h.setData(5, 317);
        h.setData(6, 182);
        h.setData(7, 99);
        h.setData(8, 62);
        h.setData(9, 35);
        h.setData(10, 59);
        assertEquals("5951,3879,1325,604,317,182,99,62,35,59", h.toCsv());
        h2 = h.normaliseBins(sumCounts);
        assertEquals(h2.getSumCounts(), sumCounts);
    }

    /**
     * Test of toString method, of class IntegerDistribution.
     */
    @Test
    public void testToString() {
        IntegerDistribution h = new IntegerDistribution(3);

        h.setData(1);
        h.setData(2, 4);
        assertEquals(3, h.getNumBins());

        assertEquals("1:1\n2:4\n3:0\n", h.toString());
    }

    /**
     * Test of toCsv method, of class IntegerDistribution.
     */
    @Test
    public void testToCsv() {
        IntegerDistribution h = new IntegerDistribution(3);

        h.setData(1);
        h.setData(2, 4);
        assertEquals(3, h.getNumBins());

        assertEquals("1,4,0", h.toCsv());
    }
    
}
