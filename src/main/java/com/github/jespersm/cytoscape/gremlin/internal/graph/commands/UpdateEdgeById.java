package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import java.util.Map;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

/**
 * Update an edge
 */
public class UpdateEdgeById extends GraphCommand {

    private PropertyKey<Object> edgeId;
    private Map<String, Object> properties;

    public UpdateEdgeById(PropertyKey<Object> edgeId, Map<String, Object> properties) {
        this.edgeId = edgeId;
        this.properties = properties;
    }

    public static UpdateEdgeById create(PropertyKey<Object> edgeId, Map<String, Object> properties) {
        return new UpdateEdgeById(edgeId, properties);
    }

    @Override
    public void execute() throws CommandException {
        try {
            graphImplementation.updateEdgeById(edgeId, properties);
        } catch (GraphImplementationException e) {
            throw new CommandException(e);
        }
    }
}
