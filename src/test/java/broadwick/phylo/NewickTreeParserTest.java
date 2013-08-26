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
package broadwick.phylo;

import broadwick.graph.Edge;
import broadwick.graph.Tree;
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
        Tree<PhyloNode, Edge<PhyloNode>> tree = newickTreeParser.parse();
        checkTree(tree);
    }

    @Test
    public void testParseWithArg() {
        NewickTreeParser newickTreeParser = new NewickTreeParser();

        // Test the trees we've already tested
        Tree<PhyloNode, Edge<PhyloNode>> tree = newickTreeParser.parse("A:0.1,B:0.2,(C:0.3,D:0.4):0.5");
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
        tree = newickTreeParser.parse("(A,B,(C,D));");
        checkTreeWithNoBranchLengths(tree);

        // all nodes are named
//        tree = newickTreeParser.parse("(A,B,(C,D)E)F;");
//        checkTreeWithWithBranchNamesWithoutBranchLengths(tree);

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
        checkTreeWithOnlyRootNode(tree);

        tree = newickTreeParser.parse("(A:0.1,((C:0.3,D:0.4)E:0.5,B:0.2),Q:1.8,R:0.6,(T:0.9,(U:0.1,V:0.15,Y:1.0))W:0.4)F:0.0;");
        checkTreeWithNestedBranches(tree);
    }

    /**
     * Several of the methods here generate the same tree (using different optional braces etc in the Newick format),
     * this method tests the outcomes.
     * @param tree
     */
    private void checkTree(final Tree<PhyloNode, Edge<PhyloNode>> tree) {
        assertEquals(6, tree.getVertexCount());
        assertEquals(2, tree.getHeight());

        Collection<PhyloNode> vertices = tree.getVertices();

        PhyloNode rootNode = new PhyloNode("", 0.5);
        PhyloNode aNode = new PhyloNode("A", 0.1);
        PhyloNode bNode = new PhyloNode("B", 0.2);
        PhyloNode cNode = new PhyloNode("C", 0.3);
        PhyloNode dNode = new PhyloNode("D", 0.4);
        PhyloNode branchNode = new PhyloNode("branch-0", 0.0);

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

    private void checkTreeWithBranchName(final Tree<PhyloNode, Edge<PhyloNode>> tree) {
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

    private void checkTreeWithOnlyRootNode(final Tree<PhyloNode, Edge<PhyloNode>> tree) {
        assertEquals(1, tree.getVertexCount());
        assertEquals(0, tree.getHeight());
        PhyloNode rootNode = new PhyloNode("A", 0.1);
        assertTrue(tree.getVertices().contains(rootNode));
        assertEquals(0, tree.getDepth(rootNode));
        assertEquals(0, tree.inDegree(rootNode));
        assertEquals(0, tree.outDegree(rootNode));
    }
    
    private void checkTreeWithNoBranchLengths(Tree<PhyloNode, Edge<PhyloNode>> tree) {
        
        assertEquals(6, tree.getVertexCount());
        assertEquals(2, tree.getHeight());

        Collection<PhyloNode> vertices = tree.getVertices();

        PhyloNode rootNode = new PhyloNode("", 0.0);
        PhyloNode aNode = new PhyloNode("A", 0.0);
        PhyloNode bNode = new PhyloNode("B", 0.0);
        PhyloNode cNode = new PhyloNode("C", 0.0);
        PhyloNode dNode = new PhyloNode("D", 0.0);
        PhyloNode branchNode = new PhyloNode("branch-0", 0.0);

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
    
    private void checkTreeWithWithBranchNamesWithoutBranchLengths(Tree<PhyloNode, Edge<PhyloNode>> tree) {
        assertEquals(6, tree.getVertexCount());
        assertEquals(2, tree.getHeight());

        Collection<PhyloNode> vertices = tree.getVertices();

        PhyloNode rootNode = new PhyloNode("F", 0.0);
        PhyloNode aNode = new PhyloNode("A", 0.0);
        PhyloNode bNode = new PhyloNode("B", 0.0);
        PhyloNode cNode = new PhyloNode("C", 0.0);
        PhyloNode dNode = new PhyloNode("D", 0.0);
        PhyloNode branchNode = new PhyloNode("E", 0.0);

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
    
    private void checkTreeWithNestedBranches(final Tree<PhyloNode, Edge<PhyloNode>> tree) {
        assertEquals(15, tree.getVertexCount());
        assertEquals(3, tree.getHeight());
        
        PhyloNode rootNode = new PhyloNode("F", 0.0);
        PhyloNode aNode = new PhyloNode("A", 0.1);
        PhyloNode bNode = new PhyloNode("B", 0.2);
        PhyloNode cNode = new PhyloNode("C", 0.3);
        PhyloNode dNode = new PhyloNode("D", 0.4);
        PhyloNode eNode = new PhyloNode("E", 0.5);
        PhyloNode qNode = new PhyloNode("Q", 1.8);
        PhyloNode rNode = new PhyloNode("R", 0.6);
        PhyloNode tNode = new PhyloNode("T", 0.9);
        PhyloNode uNode = new PhyloNode("U", 0.1);
        PhyloNode vNode = new PhyloNode("V", 0.15);
        PhyloNode wNode = new PhyloNode("W", 0.4);
        PhyloNode yNode = new PhyloNode("Y", 1.0);
        PhyloNode branch0 = new PhyloNode("branch-0", 0.0);
        PhyloNode branch1 = new PhyloNode("branch-1", 0.0);
        
        Collection<PhyloNode> vertices = tree.getVertices();
        assertTrue(vertices.contains(rootNode));
        assertTrue(vertices.contains(aNode));
        assertTrue(vertices.contains(bNode));
        assertTrue(vertices.contains(cNode));
        assertTrue(vertices.contains(dNode));
        assertTrue(vertices.contains(eNode));
        assertTrue(vertices.contains(qNode));
        assertTrue(vertices.contains(rNode));
        assertTrue(vertices.contains(tNode));
        assertTrue(vertices.contains(uNode));
        assertTrue(vertices.contains(vNode));
        assertTrue(vertices.contains(wNode));
        assertTrue(vertices.contains(yNode));
        assertTrue(vertices.contains(branch0));
        assertTrue(vertices.contains(branch1));

        assertEquals(0, tree.getDepth(rootNode));
        assertEquals(0, tree.inDegree(rootNode));
        assertEquals(5, tree.outDegree(rootNode));

        assertEquals(1, tree.getDepth(aNode));
        assertEquals(1, tree.inDegree(aNode));
        assertEquals(0, tree.outDegree(aNode));

        assertEquals(2, tree.getDepth(bNode));
        assertEquals(1, tree.inDegree(bNode));
        assertEquals(0, tree.outDegree(bNode));

        assertEquals(3, tree.getDepth(cNode));
        assertEquals(1, tree.inDegree(cNode));
        assertEquals(0, tree.outDegree(cNode));

        assertEquals(3, tree.getDepth(dNode));
        assertEquals(1, tree.inDegree(dNode));
        assertEquals(0, tree.outDegree(dNode));

        assertEquals(2, tree.getDepth(eNode));
        assertEquals(1, tree.inDegree(eNode));
        assertEquals(2, tree.outDegree(eNode));

        assertEquals(1, tree.getDepth(qNode));
        assertEquals(1, tree.inDegree(qNode));
        assertEquals(0, tree.outDegree(qNode));

        assertEquals(1, tree.getDepth(rNode));
        assertEquals(1, tree.inDegree(rNode));
        assertEquals(0, tree.outDegree(rNode));

        assertEquals(2, tree.getDepth(tNode));
        assertEquals(1, tree.inDegree(tNode));
        assertEquals(0, tree.outDegree(tNode));

        assertEquals(3, tree.getDepth(uNode));
        assertEquals(1, tree.inDegree(uNode));
        assertEquals(0, tree.outDegree(uNode));

        assertEquals(3, tree.getDepth(vNode));
        assertEquals(1, tree.inDegree(vNode));
        assertEquals(0, tree.outDegree(vNode));

        assertEquals(1, tree.getDepth(wNode));
        assertEquals(1, tree.inDegree(wNode));
        assertEquals(2, tree.outDegree(wNode));

        assertEquals(3, tree.getDepth(yNode));
        assertEquals(1, tree.inDegree(yNode));
        assertEquals(0, tree.outDegree(yNode));

        assertEquals(1, tree.getDepth(branch0));
        assertEquals(1, tree.inDegree(branch0));
        assertEquals(2, tree.outDegree(branch0));

        assertEquals(2, tree.getDepth(branch1));
        assertEquals(1, tree.inDegree(branch1));
        assertEquals(3, tree.outDegree(branch1));
    }

    private static String testFileName;
}
