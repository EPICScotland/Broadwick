package broadwick.stochastic;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashSet;
import lombok.Getter;
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
 * Test cases for broadwick.stochastic.StochasticSimulator class.
 */
@Slf4j
public class StochasticSimulatorTest {

//    private TransitionKernel transitionKernel = null;
//    private AmountManager amountManager = null;
    private StochasticSimulator simulatorImpl;
    private ObserverImpl observerImpl;
    public StochasticSimulatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        simulatorImpl = new StochasticSimulatorImpl();
        observerImpl = new ObserverImpl(simulatorImpl);
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
    public void testGetName() {
        assertEquals("StochasticSimulatorImpl", this.simulatorImpl.getName());
    }

//    @Test
//    public void testStart_double() {
//        System.out.println("start");
//        double time = 0.0;
//        StochasticSimulator instance = null;
//        instance.start(time);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testStart_0args() {
//        System.out.println("start");
//        StochasticSimulator instance = null;
//        instance.start();
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testInit() {
//        System.out.println("init");
//        StochasticSimulator instance = null;
//        instance.init();
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testDoEvent_SimulationEvent_double() {
//        System.out.println("doEvent");
//        SimulationEvent mu = null;
//        double t = 0.0;
//        StochasticSimulator instance = null;
//        instance.doEvent(mu, t);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testDoEvent_3args() {
//        System.out.println("doEvent");
//        SimulationEvent mu = null;
//        double t = 0.0;
//        int n = 0;
//        StochasticSimulator instance = null;
//        instance.doEvent(mu, t, n);
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testAddObserver() {
        assertEquals(0, simulatorImpl.getObservers().size());
        simulatorImpl.addObserver(observerImpl);
        assertEquals(1, simulatorImpl.getObservers().size());
        simulatorImpl.getObservers().clear();
        assertEquals(0, simulatorImpl.getObservers().size());
    }

//    @Test
//    public void testGetDefaultObserver() {
//        System.out.println("getDefaultObserver");
//        StochasticSimulator instance = null;
//        Observer expResult = null;
//        Observer result = instance.getDefaultObserver();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testReinitialize() {
//        System.out.println("reinitialize");
//        StochasticSimulator instance = null;
//        instance.reinitialize();
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testPerformStep() {
//        System.out.println("performStep");
//        StochasticSimulator instance = null;
//        instance.performStep();
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testSetRngSeed() {
//        System.out.println("setRngSeed");
//        int seed = 0;
//        StochasticSimulator instance = null;
//        instance.setRngSeed(seed);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testSetTransitionKernel() {
//        System.out.println("setTransitionKernel");
//        TransitionKernel transitionKernel = null;
//        StochasticSimulator instance = null;
//        instance.setTransitionKernel(transitionKernel);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testGetTransitionKernel() {
//        System.out.println("getTransitionKernel");
//        StochasticSimulator instance = null;
//        TransitionKernel expResult = null;
//        TransitionKernel result = instance.getTransitionKernel();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testGetAmountManager() {
//        System.out.println("getAmountManager");
//        StochasticSimulator instance = null;
//        AmountManager expResult = null;
//        AmountManager result = instance.getAmountManager();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testSetCurrentTime() {
//        System.out.println("setCurrentTime");
//        double currentTime = 0.0;
//        StochasticSimulator instance = null;
//        instance.setCurrentTime(currentTime);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testGetCurrentTime() {
//        System.out.println("getCurrentTime");
//        StochasticSimulator instance = null;
//        double expResult = 0.0;
//        double result = instance.getCurrentTime();
//        assertEquals(expResult, result, 0.0);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testGetController() {
//        System.out.println("getController");
//        StochasticSimulator instance = null;
//        SimulationController expResult = null;
//        SimulationController result = instance.getController();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testSetController() {
//        System.out.println("setController");
//        SimulationController controller = null;
//        StochasticSimulator instance = null;
//        instance.setController(controller);
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testGetObservers() {
//        System.out.println("getObservers");
//        StochasticSimulator instance = null;
//        Set expResult = null;
//        Set result = instance.getObservers();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testDoThetaEvent() {
        simulatorImpl.addObserver(observerImpl);
        simulatorImpl.registerNewTheta(observerImpl, 1.0, "Event 1.0");
        simulatorImpl.registerNewTheta(observerImpl, 2.0, "Event 2.0");
        simulatorImpl.registerNewTheta(observerImpl, 2.0, "Event 2.0b");

        // once the simulator does a theta event our obseverImpl saves it. No
        // event was processed so out observer should have none.
        Collection<Object> thetas = observerImpl.getThetas();
        assertEquals(0, thetas.size());

        simulatorImpl.doThetaEvent();
        thetas = observerImpl.getThetas();
        assertEquals(1, thetas.size());
        assertTrue("Event 1.0".equals(Iterables.getFirst(thetas, this)));

        simulatorImpl.doThetaEvent();
        thetas = observerImpl.getThetas();
        assertEquals(2, thetas.size());
        assertTrue(thetas.contains("Event 2.0"));
        assertTrue(thetas.contains("Event 2.0b"));
    }

    @Test
    public void testGetNextThetaEventTime() {
        simulatorImpl.addObserver(observerImpl);
        simulatorImpl.registerNewTheta(observerImpl, 1.0, "Event 1.0");
        simulatorImpl.registerNewTheta(observerImpl, 1.0, "Event 1.0.1");
        simulatorImpl.registerNewTheta(observerImpl, 2.5, "Event 2.5");

        // simply calling getNextThetaEventTime() does not advance the time.
        assertEquals(1.0, simulatorImpl.getNextThetaEventTime(), 0.1);
        assertEquals(1.0, simulatorImpl.getNextThetaEventTime(), 0.1);

        //  calling doThetaEvent() processes the theta event and advances the time.
        simulatorImpl.doThetaEvent();
        assertEquals(2.5, simulatorImpl.getNextThetaEventTime(), 0.1);

        simulatorImpl.doThetaEvent();
        assertEquals(Double.POSITIVE_INFINITY, simulatorImpl.getNextThetaEventTime(), 0.1);
    }

//    @Test
//    public void testRegisterNewTheta() {
//        simulatorImpl.addObserver(observerImpl);
//        simulatorImpl.registerNewTheta(observerImpl, 1.0, "Event 1.0");
//        simulatorImpl.registerNewTheta(observerImpl, 2.0, "Event 2.0");
//        simulatorImpl.registerNewTheta(observerImpl, 2.5, "Event 2.5");
//
//        fail("The test case is a prototype.");
//    }
    public class StochasticSimulatorImpl extends StochasticSimulator {

        public StochasticSimulatorImpl() {
            super(null, null);
        }

        @Override
        public void reinitialize() {
        }

        @Override
        public void performStep() {
        }

        @Override
        public String getName() {
            return "StochasticSimulatorImpl";
        }

        @Override
        public void setRngSeed(int seed) {
        }

    }

    /**
     * Simple observer class that does the following:
     * <p/>
     * Saves the theta events it receives.
     * <p/>
     */
    public class ObserverImpl extends Observer {

        public ObserverImpl(final StochasticSimulator sim) {
            super(sim);
            thetas = new HashSet<>();
        }

        @Override
        public void started() {
        }

        @Override
        public void step() {
        }

        @Override
        public void finished() {
        }

        @Override
        public void theta(double thetaTime, Collection<Object> events) {
            thetas = new HashSet<>();
            thetas.addAll(events);
        }

        @Override
        public void performEvent(SimulationEvent event, double tau, int times) {
        }

        @Getter
        Collection<Object> thetas;
    }
}
