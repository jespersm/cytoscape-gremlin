package com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph;

import org.cytoscape.model.CyNetwork;

import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;

/**
 * This interface specifies an import strategy for copying nodes and edges from Gremlin to Cytoscape.
 */
public interface ImportGraphStrategy {

    void createTables(CyNetwork network, Graph graph);

    default void copyGraph(CyNetwork network, Graph graph) {
        graph.nodes().forEach(node -> copyNode(network, node));
        graph.edges().forEach(edge -> copyEdge(network, edge));
    }

    void copyNode(CyNetwork network, GraphNode graphNode);

    void copyEdge(CyNetwork network, GraphEdge graphEdge);

    void postProcess(CyNetwork network);

    String getRefIDName();

}
