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
package broadwick.graph;

import java.util.Arrays;
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
        Tree<Vertex, Edge<Vertex>> tree = createTree(0);
        DepthFirstIterator<Vertex, Edge<Vertex>> it = new DepthFirstIterator<>(tree);
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

        Tree<Vertex, Edge<Vertex>> tree = createTree(5);
        DepthFirstIterator<Vertex, Edge<Vertex>> it = new DepthFirstIterator<>(tree);
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
        Tree<Vertex, Edge<Vertex>> tree = createTree(1);
        DepthFirstIterator<Vertex, Edge<Vertex>> it = new DepthFirstIterator<>(tree);
        System.out.println("testRemove " + it.hasNext());
        assertTrue(it.hasNext());

        it.remove();
    }

    private Tree<Vertex, Edge<Vertex>> createTree(final int numVertices) {

        Tree<Vertex, Edge<Vertex>> tree = new Tree<>();

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