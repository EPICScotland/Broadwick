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
package broadwick.statistics;

import java.text.DecimalFormat;
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
 *
 */
@Slf4j
public class SamplesTest {

    private Samples acc = null;
    public SamplesTest() {
        acc = new Samples();
        acc.add(1.0);
        acc.add(2.0);
        acc.add(3.0);
        acc.add(4.0);
        acc.add(5.0);
        acc.add(6.0);
        acc.add(7.0);
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
     * Test of add method, of class Samples.
     */
    @Test
    public void testAdd_double() {

        Samples samples = new Samples();
        samples.add(1.0);
        samples.add(2.0);
        samples.add(3.0);

        assertEquals(samples.getSize(), 3);
        assertEquals(samples.getSum(), 6.0, 0.001);

        samples.add(4.0);

        assertEquals(samples.getSize(), 4);
        assertEquals(samples.getSum(), 10.0, 0.001);
    }

    /**
     * Test of add method, of class Samples.
     */
    @Test
    public void testAdd_Samples() {

        Samples samples1 = new Samples();
        samples1.add(1.0);
        samples1.add(2.0);
        samples1.add(3.0);

        Samples samples2 = new Samples();
        samples2.add(4.0);
        samples2.add(5.0);
        samples2.add(6.0);

        assertEquals(samples1.getSize(), 3);
        assertEquals(samples1.getSum(), 6.0, 0.001);

        assertEquals(samples2.getSize(), 3);
        assertEquals(samples2.getSum(), 15.0, 0.001);

        samples1.add(samples2);

        assertEquals(samples1.getSize(), 6);
        assertEquals(samples1.getSum(), 21.0, 0.001);
    }

    /**
     * Test of clear method, of class Samples.
     */
    @Test
    public void testClear() {

        Samples samples = new Samples();
        samples.add(5.2);
        samples.add(4.3);
        samples.add(5.5);

        assertEquals(samples.getSize(), 3);
        assertEquals(samples.getSum(), 15.0, 0.001);

        samples.clear();

        assertEquals(samples.getSize(), 0);
        assertEquals(samples.getSum(), 0.0, 0.001);
    }

    /**
     * Test of getSummary method, of class Samples.
     */
    @Test
    public void testGetSummary() {
        
        final StringBuilder sb = new StringBuilder(10);
        sb.append("\t").append(new DecimalFormat("0.###E0").format(acc.getMean()));
        sb.append("\t").append(new DecimalFormat("0.###E0").format(acc.getStdDev()));

        assertEquals(acc.getSummary(), sb.toString());
    }

    /**
     * Test of getMean method, of class Accumulator.
     */
    @Test
    public void testGetMean() {
        assertEquals(acc.getMean(), 4.0, 0.01);
    }

    /**
     * Test of getVariance method, of class Accumulator.
     */
    @Test
    public void testGetVariance() {
        assertEquals(acc.getVariance(), 0.571, 0.001);
    }

    /**
     * Test of getStdDev method, of class Accumulator.
     */
    @Test
    public void testGetStdDev() {
        assertEquals(acc.getStdDev(), 0.756, 0.001);
    }

    /**
     * Test of getRootMeanSquare method, of class Accumulator.
     */
    @Test
    public void testGetRootMeanSquare() {
        assertEquals(acc.getRootMeanSquare(), 4.47, 0.01);
    }

    /**
     * Test of getGeometricMean method, of class Accumulator.
     */
    @Test
    public void testGetGeometricMean() {
        assertEquals(acc.getGeometricMean(), 3.38, 0.01);
    }

    /**
     * Test of getHarmonicMean method, of class Accumulator.
     */
    @Test
    public void testGetHarmonicMean() {
        assertEquals(acc.getHarmonicMean(), 2.69, 0.01);
    }

    /**
     * Test of getSum method, of class Accumulator.
     */
    @Test
    public void testGetSum() {
        assertEquals(acc.getSum(), 28.0, 0.001);
    }

    /**
     * Test of getSize method, of class Accumulator.
     */
    @Test
    public void testGetSize() {
        assertEquals(acc.getSize(), 7.0, 0.001);
    }

}
