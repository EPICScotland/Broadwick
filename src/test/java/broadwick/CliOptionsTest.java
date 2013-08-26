/*
 * Copyright 2013 University of Glasgow.
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
package broadwick;

import static org.junit.Assert.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.MarkerFactory;

/**
 * Test cases for broadwick.CliOptions class.
 */
@Slf4j
public class CliOptionsTest {

    private String configFile = "input.cfg";
    private String configFileOpts[] = {"-c", configFile};
    public CliOptionsTest() {
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
    @Rule
    public ExpectedException exception = ExpectedException.none();
    /**
     * Test of getConfigurationFileName method with the generate-file option, of class CliOptions.
     */
    @Test
    public void testValidCommandLineArguments() {
    }

    /**
     * Test of getConfigurationFileName method, of class CliOptions.
     */
    @Test
    public void testGetConfigurationFileName() {
        CliOptions cli = new CliOptions(configFileOpts);
        assertEquals(configFile, cli.getConfigurationFileName());
    }

}
