package com.github.jespersm.cytoscape.gremlin.internal.graph;

/**
 * This class represents a simple value in a graph.
 */
public class GraphSimple implements GraphObject {

    private final Object value;

    public GraphSimple(Object value) {
        this.value = value;
    }

    @Override
    public void accept(GraphVisitor graphVisitor) {
        graphVisitor.visit(this);
    }

    public Object getValue() {
        return value;
    }
}
