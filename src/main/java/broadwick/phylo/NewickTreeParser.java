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
import broadwick.io.FileInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Parser for a Newick tree. This has limited functionality and only works for Newick trees <ul> <li> with unique node
 * names (e.g. one node can be unnamed but the rest must be uniquely named)</li> <li> with defined branch lengths (e.g.
 * (A:0.1,B:0.2,(C:0.3,D:0.4)E:0.5)F:0.0;)</li> </ul>
 */
@Slf4j
public class NewickTreeParser {

    /**
     * Create the parser.
     * @param newickFile the name of the file containing the tree.
     */
    public NewickTreeParser(final String newickFile) {
        try (FileInput file = new FileInput(newickFile)) {
            newickString = file.read();
        } catch (IOException ex) {
            log.error("Could not read Newick file {}. {}", newickFile, ex.getLocalizedMessage());
        }
    }

    /**
     * Create the parser.
     */
    public NewickTreeParser() {
        newickString = null;
    }

    /**
     * Parse a given string that MUST be a valid Newick format.
     * @param newickStr the string to parse.
     * @return a Tree created from the parsed string.
     */
    public final Tree<PhyloNode, Edge<PhyloNode>> parse(final String newickStr) {
        branchLabel = 0;

        final Tree<PhyloNode, Edge<PhyloNode>> phyloTree = new Tree<>();
        // first remove the trailing ; if any
        String stringToParse = StringUtils.removeEnd(newickStr.trim(), ";").trim();

        if (stringToParse.charAt(0) == '(' && getClosingParenthesis(stringToParse) == stringToParse.length() - 1) {
            stringToParse = stringToParse.substring(1, stringToParse.length() - 1);
        }

        // extract the information for the root node, this will be after the last closing parenthesis iff there is
        // a colon (TODO is this right? e.g. (A,B,(C,D),E)F; )
        double distance = 0.0;
        String rootName = "";
        final int rparen = stringToParse.lastIndexOf(')');
        final int rcolon = stringToParse.lastIndexOf(':');
        if (rparen < rcolon) {
            distance = Double.parseDouble(stringToParse.substring(rcolon + 1));
            rootName = stringToParse.substring(rparen + 1, rcolon);
            stringToParse = stringToParse.substring(0, rcolon - rootName.length()).trim(); // adjust the string to exclude the distance
        }

        // create a new node for this root and add it to the tree.
        final PhyloNode root = new PhyloNode(rootName, distance);
        log.trace("Parsing Newick file: Adding root node {}", root);
        phyloTree.addVertex(root);

        // if we don't have a comma we are just have the root node
        if (stringToParse.indexOf(',') == -1) {
            return phyloTree;
        }

        if (stringToParse.charAt(0) == '(' && getClosingParenthesis(stringToParse) == stringToParse.length() - 1) {
            stringToParse = stringToParse.substring(1, stringToParse.length() - 1);
        }

        parseString(stringToParse, root, phyloTree);
        return phyloTree;
    }

    /**
     * Parse a string from a Newick file.
     * @return a generated Tree object.
     */
    public final Tree<PhyloNode, Edge<PhyloNode>> parse() {
        if (newickString == null) {
            throw new IllegalArgumentException("NewickParser created but no string to parse found. Did you create the parser without specifying a file?");
        }
        return parse(newickString);
    }

    /**
     * Parse a string in Newick format (where the root node has been removed) recursively.
     * @param stringToParse the string to be parsed.
     * @param parent        the parent node for the string (initially this is the root node but will be a brach node if
     *                      this method is called recursively).
     * @param tree          the tree to which the created nodes will be attached.
     */
    private void parseString(final String stringToParse, final PhyloNode parent,
                             final Tree<PhyloNode, Edge<PhyloNode>> tree) {
        for (String node : findNodes(stringToParse)) {
            parseNode(node, tree, parent);
        }
    }

    /**
     * Parse a tree splitting it into top level nodes and subtrees.
     * @param tree the tree tp be parsed.
     * @return a collection of nodes and subtrees contained in the parsed string
     */
    private Collection<String> findNodes(final String tree) {
        final Collection<String> nodes = new ArrayList<>();

        int start = 0;
        int depth = 0;
        boolean isOk = true;
        for (int i = 0; i < tree.length(); i++) {
            final char charAt = tree.charAt(i);
            if (charAt == '(') {
                depth++;
                isOk = false;
            }
            if (charAt == ')' && --depth == 0) {
                isOk = true;
            }

            if ((charAt == ',' && isOk)) {
                nodes.add(tree.substring(start, i).trim());
                start = i + 1;
            }
        }
        nodes.add(tree.substring(start).trim());

        return nodes;
    }

    /**
     * Get the index of the closing parenthesis in a string that starts with an opening parenthesis. If the string does
     * not start with an opening parenthesis then an IllegalArgumentException is thrown. The algorithm for finding the
     * closing parenthesis is simple - keep reading the string incrementing a count of the number of opening parentheses
     * we find and decrementing the count if we encounter a closing parenthesis. When we read a closing parenthesis AND
     * the count becomes zero we have our closing parenthesis.
     * @param strng the string to examine.
     * @return the index in the string of the closing parenthesis, -1 if none is found.
     */
    private int getClosingParenthesis(final String strng) {

        if (!strng.trim().startsWith("(")) {
            throw new IllegalArgumentException(String.format("Illegal Argument [%s] does not start with an opening parenthesis", strng));
        }

        int depth = 0;
        for (int i = 0; i < strng.length(); i++) {
            if (strng.charAt(i) == '(') {
                depth++;
            }
            if (strng.charAt(i) == ')' && (--depth == 0)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Parse a string containing information on a node in Newick format and attach it to a given tree.
     * @param node   the string containing the node information.
     * @param tree   the tree to which the created node will be attached.
     * @param parent the node on the tree to which the new node will be created.
     */
    private void parseNode(final String node, final Tree<PhyloNode, Edge<PhyloNode>> tree, final PhyloNode parent) {
        log.trace("parsing {} from parent node {} ", node, parent.getId());
        if (node.charAt(0) == '(') {
            // this is a branch so create a branch node and set the parent
            final int rparen = node.lastIndexOf(')');
            final PhyloNode branchNode = addNodeToTree(node.substring(rparen + 1), tree, parent, true);
            for (String nd : findNodes(node.substring(1, rparen))) {
                parseNode(nd, tree, branchNode);
            }
        } else if (node.indexOf(',') != -1) {
            for (String nd : findNodes(node)) {
                parseNode(nd, tree, parent);
            }
        } else {
            // this is a single node
            addNodeToTree(node, tree, parent, false);
        }
    }

    /**
     * Given a string that represents a node in the tree, split in into its constituent name and distance components and
     * add it to the tree. If the string is empty then a dummy node is added, this is useful fro creating (unnamed)
     * branches.
     * @param node             the string representing the node.
     * @param tree             the tree object on which the node will be placed.
     * @param parent           the parent node to which this node is attached.
     * @param createUniqueName if the name of the node is not unique then add create a unique one if this is true.
     * @return the node added to the tree.
     */
    private PhyloNode addNodeToTree(final String node, final Tree<PhyloNode, Edge<PhyloNode>> tree, final PhyloNode parent,
                                    final boolean createUniqueName) {
        log.trace("Adding {} to tree at {}.", node, parent);
        PhyloNode phyloNode;

        final int lcolon = node.indexOf(':');
        String nodeName;
        double distance = 0.0;

        if (node.isEmpty()) {
            // no details so use default values.
            nodeName = String.format("branch-%d", branchLabel++);
            distance = 0.0;
        } else if (lcolon == -1) {
            // we just have a node name and no distance.
            distance = 0.0;
            nodeName = node;
        } else {
            // we have a node name and a distance.
            distance = Double.parseDouble(node.substring(lcolon + 1));
            nodeName = node.substring(0, lcolon);
        }

        phyloNode = new PhyloNode(nodeName, distance);

        try {
            tree.addEdge(new Edge<>(parent, phyloNode, distance), parent, phyloNode);
        } catch (IllegalArgumentException e) {
            // we may have non-unique branch names in the data file, if we encounter one here and we want to rename
            // it we do it now. This is not very good as we are searching for an error message but it's the best as can
            // be done.
            if (e.getLocalizedMessage().contains("already exists in this graph") && createUniqueName) {
                nodeName = String.format("branch-%d", branchLabel++);
                phyloNode = new PhyloNode(nodeName, distance);
                tree.addEdge(new Edge<>(parent, phyloNode), parent, phyloNode);
            }
        }

        return phyloNode;
    }
    private String newickString;
    private int branchLabel = 0;
}
