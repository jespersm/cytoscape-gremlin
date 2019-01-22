package com.github.jespersm.cytoscape.gremlin.internal.graph;

/**
 * This class specifies the interface for any object that is part of a graph.
 * A graph object is used as an intermediate data model to import data from Gremlin to Cytoscape.
 * A graph object can be processed by a visitor.
 */
public interface GraphObject {
    void accept(GraphVisitor graphVisitor);
}
