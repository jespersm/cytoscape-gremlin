package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import java.util.Map;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

/**
 * Update an edge
 */
public class UpdateDirectedEdgeByStartAndEndNodeId extends GraphCommand {

    private PropertyKey<Object> startId;
    private PropertyKey<Object> endId;
    private Map<String, Object> properties;

    public UpdateDirectedEdgeByStartAndEndNodeId(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties) {
        this.startId = startId;
        this.endId = endId;
        this.properties = properties;
    }

    public static UpdateDirectedEdgeByStartAndEndNodeId create(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties) {
        return new UpdateDirectedEdgeByStartAndEndNodeId(startId, endId, properties);
    }

    @Override
    public void execute() throws CommandException {
        try {
            graphImplementation.updateDirectedEdge(startId, endId, properties);
        } catch (GraphImplementationException e) {
            throw new CommandException(e);
        }
    }
}
