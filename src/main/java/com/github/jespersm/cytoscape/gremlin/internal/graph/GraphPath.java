package com.github.jespersm.cytoscape.gremlin.internal.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a path in a graph.
 */
public class GraphPath implements GraphObject {
    private List<GraphNode> nodes;
    private List<GraphEdge> edges;

    public GraphPath() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void add(GraphNode graphNode) {
        nodes.add(graphNode);
    }

    public void add(GraphEdge graphEdge) {
        edges.add(graphEdge);
    }

    @Override
    public void accept(GraphVisitor graphVisitor) {
        graphVisitor.visit(this);
    }

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }
}
