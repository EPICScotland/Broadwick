package broadwick.phylo;

import broadwick.graph.Tree;
import broadwick.io.FileInput;
import java.io.IOException;
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
    public final Tree<PhyloNode, String> parse(final String newickStr) {

        final Tree<PhyloNode, String> phyloTree = new Tree<>();
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
    public final Tree<PhyloNode, String> parse() {
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
    private void parseString(String stringToParse, PhyloNode parent, final Tree tree) {

        stringToParse = removeLeadingComma(stringToParse);

        if (stringToParse.charAt(0) == '(') {
            // We have a new branch, create one and process the subtree
            final int lparen = stringToParse.indexOf('(');
            final int rparen = getClosingParenthesis(stringToParse);

            String branchNodeName = String.format("branch-%s", parent.getId());
            double branchnodeDistance = 0.0;
            if (rparen != stringToParse.length() - 1) {
                char nextChar = stringToParse.charAt(rparen + 1);
                if (!(nextChar == ',' || nextChar == ')')) {
                    // the branch is named so find the name
                    String branchDetails = ":";
                    for (int i = rparen + 1; i < stringToParse.length(); i++) {
                        nextChar = stringToParse.charAt(i);
                        if (nextChar == ',' || nextChar == ')' || i == stringToParse.length() - 1) {
                            branchDetails = stringToParse.substring(rparen + 1, i + 1);
                            break;
                        }
                    }
                    final int lcolon = branchDetails.indexOf(':');
                    branchnodeDistance = Double.parseDouble(branchDetails.substring(lcolon + 1));
                    branchNodeName = branchDetails.substring(0, lcolon);
                    stringToParse = stringToParse.substring(0, rparen + 1);

                }
            }

            // Create a new branching point (a new node) on which to hang this sub tree.
            final PhyloNode phyloNode = new PhyloNode(branchNodeName, branchnodeDistance);
            tree.addEdge(String.format("[%s]-[%s]", parent.getId(), phyloNode.getId()), parent, phyloNode);
            parent = phyloNode;

            // Create the string for the subtree
            final String subtree = stringToParse.substring(lparen + 1, rparen);

            // now remove the subtree from the rest of the string that needs to be parsed.
            stringToParse = stringToParse.substring(rparen + 1);

            parseString(subtree, parent, tree);
        }

        final int rcomma = stringToParse.indexOf(',');
        if (rcomma != -1) {
            final String node = stringToParse.substring(0, rcomma);
            parseNode(node, tree, parent);

            stringToParse = stringToParse.substring(node.length());
            parseString(stringToParse, parent, tree);

        } else if (stringToParse.length() > 0) {
            // all we have left is a node
            parseNode(stringToParse, tree, parent);
        }
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
            if (strng.charAt(i) == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Trim a given string and remove any leading commas.
     * @param stringToTrim the string to be modified.
     * @return a copy of the supplied string with leading/trailing whitespaces removed and leading commas removed.
     */
    private String removeLeadingComma(String stringToTrim) {
        // firstly trim any whitespaces
        stringToTrim = stringToTrim.trim();

        // ... and strip any leading commas
        if (stringToTrim.charAt(0) == ',') {
            return stringToTrim.substring(1);
        }

        return stringToTrim;
    }

    /**
     * Parse a string containing information on a node in Newick format and attach it to a given tree.
     * @param nodeStr the string containing the node information.
     * @param tree    the tree to which the created node will be attached.
     * @param parent  the node on the tree to which the new node will be created.
     */
    private void parseNode(final String nodeStr, final Tree tree, final PhyloNode parent) {
        final int lcolon = nodeStr.indexOf(':');
        final double distance = Double.parseDouble(nodeStr.substring(lcolon + 1));
        final String nodeName = nodeStr.substring(0, lcolon);

        final PhyloNode phyloNode = new PhyloNode(nodeName, distance);
        tree.addEdge(String.format("[%s]-[%s]", parent.getId(), phyloNode.getId()), parent, phyloNode);
    }

    private String newickString;
}
