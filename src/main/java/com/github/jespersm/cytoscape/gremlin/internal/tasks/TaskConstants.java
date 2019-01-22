package com.github.jespersm.cytoscape.gremlin.internal.tasks;

public final class TaskConstants {

    public static final String GREMLIN_PROPERTY_CYTOSCAPE_NETWORK = "_cytoscape_network";
    public static final String MATCH_ALL_NODES_AND_EDGES = "g.V().union(__.identity(),__.bothE()).dedup()";

    private TaskConstants() {

    }
}
