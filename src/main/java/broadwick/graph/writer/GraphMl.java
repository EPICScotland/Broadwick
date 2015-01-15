/*
 * Copyright 2014 University of Glasgow.
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
package broadwick.graph.writer;

import broadwick.graph.Edge;
import broadwick.graph.EdgeAttribute;
import broadwick.graph.Graph;
import broadwick.graph.Vertex;
import broadwick.graph.VertexAttribute;
import broadwick.io.FileOutput;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Simple class to write the nodes/edges of a network in graphML format.
 */
public final class GraphMl {

    /**
     * hidden constructor for static class.
     */
    private GraphMl() {
        // no constructor
    }

    /**
     * Get the string representation of the network.
     * @param network  the network object to be written.
     * @param directed a boolean flag, true if the network is directed.
     * @return a string representing a document.
     */
    public static String toString(final Graph<? extends Vertex, ? extends Edge<?>> network, final boolean directed) {
        // graphml document header
        final Element graphml = new Element("graphml", "http://graphml.graphdrawing.org/xmlns");
        final Document document = new Document(graphml);
        final Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        final Namespace schemLocation = Namespace.getNamespace("schemLocation", "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0rc/graphml.xsd");

        // add Namespace
        graphml.addNamespaceDeclaration(xsi);
        graphml.addNamespaceDeclaration(schemLocation);

        // keys for graphic representation
        for (VertexAttribute attr : network.getVertexAttributes()) {
            final Element element = new Element("key");
            element.setAttribute("id", attr.getName());
            element.setAttribute("for", "node");
            element.setAttribute("attr.name", attr.getName());
            element.setAttribute("attr.type", attr.getType().getName());
            if (attr.getDefaultValue() != null) {
                final Element defaultValueElement = new Element("default");
                defaultValueElement.addContent(attr.getDefaultValue().toString());
                element.addContent(defaultValueElement);
            }
            graphml.addContent(element);
        }

        for (EdgeAttribute attr : network.getEdgeAttributes()) {
            final Element element = new Element("key");
            element.setAttribute("id", attr.getName());
            element.setAttribute("for", "edge");
            element.setAttribute("attr.name", attr.getName());
            element.setAttribute("attr.type", attr.getType().getName());
            if (attr.getDefaultValue() != null) {
                final Element defaultValueElement = new Element("default");
                defaultValueElement.addContent(attr.getDefaultValue());
                element.addContent(defaultValueElement);
            }
            graphml.addContent(element);
        }

        final Element graph = new Element("graph");
        graph.setAttribute("id", "G");
        if (directed) {
            graph.setAttribute("edgedefault", "directed");
        } else {
            graph.setAttribute("edgedefault", "undirected");
        }
        graphml.addContent(graph);

        final ImmutableList<Vertex> vertices = ImmutableList.copyOf(network.getVertices());
        final Iterator<Vertex> vertexIterator = vertices.iterator();
        while (vertexIterator.hasNext()) {
            final Vertex vertex = vertexIterator.next();
            addNode(vertex, graph);
        }

        final ImmutableList<Edge<? extends Vertex>> edges;
        edges = (ImmutableList<Edge<? extends Vertex>>) ImmutableList.copyOf(network.getEdges());
        final UnmodifiableIterator<Edge<? extends Vertex>> edgeIterator = edges.iterator();
        while (edgeIterator.hasNext()) {
            addEdge(edgeIterator.next(), graph);
        }

        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(document).replaceAll("xmlns=\"\" ", "");
    }

    /**
     * Get the string representation of the network.
     * @param network the network object to be written.
     * @return a string representing a document.
     */
    public static String toString(final Graph<? extends Vertex, ? extends Edge<?>> network) {
        return GraphMl.toString(network, false);
    }

    /**
     * Save the graph as in graphML format in the given file.
     * @param network the network object to be saved.
     * @param file    the name of the file in which the graph will be saved.
     */
    public static void save(final String file, final Graph<? extends Vertex, ? extends Edge<Vertex>> network) {
        try (FileOutput fo = new FileOutput(file)) {
            fo.write(GraphMl.toString(network));
        }
    }

    /**
     * Add an edge to the graphML document.
     * @param edge    the edge to be added to the graph element.
     * @param element the graph element of the graphML document
     */
    private static void addEdge(final Edge edge, final Element element) {

        final String id = edge.getId();

        final String source = edge.getSource().getId();
        final String target = edge.getDestination().getId();

        final Element elem = new Element("edge");
        elem.setAttribute("id", "e" + id);
        elem.setAttribute("source", source);
        elem.setAttribute("target", target);

        for (final Iterator it = edge.getAttributes().iterator(); it.hasNext();) {
            final EdgeAttribute attr = (EdgeAttribute) it.next();
            final Element data = new Element("data");
            data.setAttribute("key", attr.getName());
            data.setText(attr.getValue());
            elem.addContent(data);
        }

        element.addContent(elem);
    }

    /**
     * Add a node to the graphML document.
     * @param vertex  the id of the node.
     * @param element the graph element of the graphML document.
     */
    private static void addNode(final Vertex vertex, final Element element) {
        final Element node = new Element("node");
        node.setAttribute("id", vertex.getId());

        for (VertexAttribute attr : vertex.getAttributes()) {
            final Element data = new Element("data");
            data.setAttribute("key", attr.getName());
            data.setText(attr.getValue().toString());
            node.addContent(data);
        }

        element.addContent(node);
    }

}
