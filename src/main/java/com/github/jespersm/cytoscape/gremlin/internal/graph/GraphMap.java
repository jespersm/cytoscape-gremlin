package com.github.jespersm.cytoscape.gremlin.internal.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a dictionary with graph objects that are part of a graph.
 */
public class GraphMap implements GraphObject {

    private Map<String, GraphObject> results = new HashMap<>();

    public void add(String key, GraphObject graphObject) {
        results.put(key, graphObject);
    }

    @Override
    public void accept(GraphVisitor graphVisitor) {
        graphVisitor.visit(this);
    }

    public GraphObject get(String key) {
        return results.get(key);
    }

    public Collection<GraphObject> getAll() {
        return Collections.unmodifiableCollection(results.values());
    }
}
