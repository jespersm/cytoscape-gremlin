package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

/**
 * Remove an edge from a graph
 */
public class RemoveEdge extends GraphCommand {

    private final PropertyKey<Object> edgeId;

    private RemoveEdge(PropertyKey<Object> edgeId) {
        this.edgeId = edgeId;
    }

    public static RemoveEdge create(PropertyKey<Object> edgeId) {
        return new RemoveEdge(edgeId);
    }

    @Override
    public void execute() throws CommandException {
        try {
            graphImplementation.removeEdge(edgeId);
        } catch (GraphImplementationException e) {
            throw new CommandException(e);
        }
    }
}
