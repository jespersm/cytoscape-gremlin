package com.github.jespersm.cytoscape.gremlin.internal.graph;

/**
 * This interface specifies a graph visitor that can be used for processing graph objects.
 */
public interface GraphVisitor {
    void visit(GraphNode graphNode);

    void visit(GraphEdge graphEdge);

    void visit(GraphMap gremlinResult);

    void visit(GraphSimple value);

    void visit(GraphPath graphPath);

    void visit(GraphObjectList graphObjectList);

    void visit(Graph graph);
}
