package broadwick.graph;

import broadwick.markovchain.Step;
import broadwick.rng.RNG;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
 *
 * @author anthony
 */
@Slf4j
public class DepthFirstIteratorTest {

    public DepthFirstIteratorTest() {
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
     * Test of hasNext method, of class DepthFirstIterator.
     */
    @Test
    public void testHasNext() {
        Tree<Vertex, Edge> tree = createTree(0);
        DepthFirstIterator<Vertex, Edge> it = new DepthFirstIterator<>(tree);
        assertFalse(it.hasNext());

        int treeSize = 1;
        tree = createTree(treeSize);
        it = new DepthFirstIterator<>(tree);
        for (int i = 0; i < treeSize; i++) {
            assertTrue(it.hasNext());
            it.next();
        }
        assertFalse(it.hasNext());


        treeSize = 2;
        tree = createTree(treeSize);
        it = new DepthFirstIterator<>(tree);
        for (int i = 0; i < treeSize; i++) {
            assertTrue(it.hasNext());
            it.next();
        }
        assertFalse(it.hasNext());



        treeSize = 12;
        tree = createTree(treeSize);
        it = new DepthFirstIterator<>(tree);
        for (int i = 0; i < treeSize; i++) {
            assertTrue(it.hasNext());
            it.next();
        }
        assertFalse(it.hasNext());
    }

    /**
     * Test of next method, of class DepthFirstIterator.
     */
    @Test
    public void testNext() {

        final List<String> nodeIds = Arrays.asList("root", "1", "2", "3", "4");

        Tree<Vertex, Edge> tree = createTree(5);
        DepthFirstIterator<Vertex, Edge> it = new DepthFirstIterator<>(tree);
        int numNodes = 0;
        while (it.hasNext()) {
            final Vertex next = it.next();
            numNodes++;
            assertTrue(nodeIds.contains(next.getId()));
        }

        assertEquals(numNodes, 5);
        assertFalse(it.hasNext());
    }

    /**
     * Test of remove method, of class DepthFirstIterator.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        Tree<Vertex, Edge> tree = createTree(1);
        DepthFirstIterator<Vertex, Edge> it = new DepthFirstIterator<>(tree);
        System.out.println("testRemove " + it.hasNext());
        assertTrue(it.hasNext());

        it.remove();
    }

    private Tree<Vertex, Edge> createTree(final int numVertices) {

        System.out.println("Creating tree for " + numVertices + " nodes");
        Tree<Vertex, Edge> tree = new Tree<>();

        // let's create a simple tree where all the even numbered nodes are on one [linear] branch and the odd ones are 
        // on the other.

        if (numVertices > 0) {
            Vertex root = new Vertex("root");
            tree.addVertex(root);

            Vertex evenSource = root;
            Vertex oddSource = root;
            for (int i = 1; i < numVertices; i++) {
                final Vertex v = new Vertex(String.valueOf(i));
                Edge edge;

                if (i % 2 == 0) {
                    // even branch
                    if (i > 2) {
                        edge = new Edge(evenSource, v, 1.0);
                    } else {
                        edge = new Edge(root, v, 1.0);
                    }
                    evenSource = v;
                } else {
                    // odd branch 
                    if (i > 2) {
                        edge = new Edge(oddSource, v, 1.0);
                    } else {
                        edge = new Edge(root, v, 1.0);
                    }
                    oddSource = v;
                }

                tree.addEdge(edge, edge.getSource(), v);
            }
        }

        return tree;
    }
}