/*
 * Copyright 2013 .
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
package broadwick.phylo;

import broadwick.graph.Tree;
import java.util.Collection;
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
 * Test cases for broadwick.phylo.NewickTreeParserTest class.
 */
@Slf4j
public class NewickTreeParserTest {

    public NewickTreeParserTest() {
        testFileName = NewickTreeParserTest.class.getClass().getResource("/TestNewickFile.txt").getFile();

        // TODO BUG there is a bug in the windows implementation of java that prepends a 
        // "/" to the file name. We need to strip it here
        if (System.getProperty("os.name").startsWith("Windows") && testFileName.startsWith("/")) {
            testFileName = testFileName.substring(1);
        }
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
    @Test
    public void testParse() {
        NewickTreeParser newickTreeParser = new NewickTreeParser(testFileName);
        Tree<PhyloNode, String> tree = newickTreeParser.parse();
        checkTree(tree);
    }

    @Test
    public void testParseWithArg() {
        NewickTreeParser newickTreeParser = new NewickTreeParser();

        // Test the trees we've already tested
        Tree<PhyloNode, String> tree = newickTreeParser.parse("A:0.1,B:0.2,(C:0.3,D:0.4):0.5");
        checkTree(tree);

        // now test the same tree with optional braces, semicolon
        tree = newickTreeParser.parse("A:0.1,B:0.2,(C:0.3,D:0.4):0.5;");
        checkTree(tree);

        tree = newickTreeParser.parse("(A:0.1,B:0.2,(C:0.3,D:0.4):0.5);");
        checkTree(tree);

        // Now test it with more complicated tests.
        // no nodes are named
        //tree = newickTreeParser.parse("(,,(,));");

        // leaf nodes are named (but no branch length
        //tree = newickTreeParser.parse("(A,B,(C,D));");

        // all nodes are named
        //tree = newickTreeParser.parse("(A,B,(C,D)E)F;");

        // all but root node have a distance to parent
        //tree = newickTreeParser.parse("(:0.1,:0.2,(:0.3,:0.4):0.5);");

        // all have a distance to parent
        //tree = newickTreeParser.parse("(:0.1,:0.2,(:0.3,:0.4):0.5):0.0;");

        // distances and all names
        //tree = newickTreeParser.parse("(A:0.1,B:0.2,(C:0.3,D:0.4)E:0.5)F;");

        // distances and all names
        tree = newickTreeParser.parse("(A:0.1,B:0.2,(C:0.3,D:0.4)E:0.5)F:0.0;");
        checkTreeWithBranchName(tree);

        // a tree rooted on a leaf node (rare)
        //tree = newickTreeParser.parse("((B:0.2,(C:0.3,D:0.4)E:0.5)F:0.1)A;");
        
        tree = newickTreeParser.parse("A:0.1;");
        assertEquals(1, tree.getVertexCount());
        assertEquals(0, tree.getHeight());
        PhyloNode rootNode = new PhyloNode("A", 0.1);
        assertTrue(tree.getVertices().contains(rootNode));
        assertEquals(0, tree.getDepth(rootNode));
        assertEquals(0, tree.inDegree(rootNode));
        assertEquals(0, tree.outDegree(rootNode));
    }

    /**
     * Several of the methods here generate the same tree (using different optional braces etc in the Newick format),
     * this method tests the outcomes.
     * @param tree
     */
    private void checkTree(final Tree<PhyloNode, String> tree) {
        assertEquals(6, tree.getVertexCount());
        assertEquals(2, tree.getHeight());

        Collection<PhyloNode> vertices = tree.getVertices();

        PhyloNode rootNode = new PhyloNode("", 0.5);
        PhyloNode aNode = new PhyloNode("A", 0.1);
        PhyloNode bNode = new PhyloNode("B", 0.2);
        PhyloNode cNode = new PhyloNode("C", 0.3);
        PhyloNode dNode = new PhyloNode("D", 0.4);
        PhyloNode branchNode = new PhyloNode("branch-", 0.0);

        assertTrue(vertices.contains(rootNode));
        assertTrue(vertices.contains(aNode));
        assertTrue(vertices.contains(bNode));
        assertTrue(vertices.contains(cNode));
        assertTrue(vertices.contains(dNode));
        assertTrue(vertices.contains(branchNode));

        assertEquals(1, tree.getDepth(aNode));
        assertEquals(1, tree.inDegree(aNode));
        assertEquals(0, tree.outDegree(aNode));

        assertEquals(1, tree.getDepth(bNode));
        assertEquals(1, tree.inDegree(bNode));
        assertEquals(0, tree.outDegree(bNode));

        assertEquals(2, tree.getDepth(cNode));
        assertEquals(1, tree.inDegree(cNode));
        assertEquals(0, tree.outDegree(cNode));

        assertEquals(2, tree.getDepth(dNode));
        assertEquals(1, tree.inDegree(dNode));
        assertEquals(0, tree.outDegree(dNode));

        assertEquals(0, tree.getDepth(rootNode));
        assertEquals(0, tree.inDegree(rootNode));
        assertEquals(3, tree.outDegree(rootNode));

        assertEquals(1, tree.getDepth(branchNode));
        assertEquals(1, tree.inDegree(branchNode));
        assertEquals(2, tree.outDegree(branchNode));
    }

    private void checkTreeWithBranchName(final Tree<PhyloNode, String> tree) {
        // This is the newwick string we're checking 
        // (A:0.1,B:0.2,(C:0.3,D:0.4)E:0.5)F:0.0;
        // i.e. it is the same as before but the branch name and root are specified.


        assertEquals(6, tree.getVertexCount());
        assertEquals(2, tree.getHeight());

        Collection<PhyloNode> vertices = tree.getVertices();

        PhyloNode rootNode = new PhyloNode("F", 0.0);
        PhyloNode aNode = new PhyloNode("A", 0.1);
        PhyloNode bNode = new PhyloNode("B", 0.2);
        PhyloNode cNode = new PhyloNode("C", 0.3);
        PhyloNode dNode = new PhyloNode("D", 0.4);
        PhyloNode branchNode = new PhyloNode("E", 0.5);

        assertTrue(vertices.contains(rootNode));
        assertTrue(vertices.contains(aNode));
        assertTrue(vertices.contains(bNode));
        assertTrue(vertices.contains(cNode));
        assertTrue(vertices.contains(dNode));
        assertTrue(vertices.contains(branchNode));

        assertEquals(1, tree.getDepth(aNode));
        assertEquals(1, tree.inDegree(aNode));
        assertEquals(0, tree.outDegree(aNode));

        assertEquals(1, tree.getDepth(bNode));
        assertEquals(1, tree.inDegree(bNode));
        assertEquals(0, tree.outDegree(bNode));

        assertEquals(2, tree.getDepth(cNode));
        assertEquals(1, tree.inDegree(cNode));
        assertEquals(0, tree.outDegree(cNode));

        assertEquals(2, tree.getDepth(dNode));
        assertEquals(1, tree.inDegree(dNode));
        assertEquals(0, tree.outDegree(dNode));

        assertEquals(0, tree.getDepth(rootNode));
        assertEquals(0, tree.inDegree(rootNode));
        assertEquals(3, tree.outDegree(rootNode));

        assertEquals(1, tree.getDepth(branchNode));
        assertEquals(1, tree.inDegree(branchNode));
        assertEquals(2, tree.outDegree(branchNode));
    }

    private static String testFileName;
}
