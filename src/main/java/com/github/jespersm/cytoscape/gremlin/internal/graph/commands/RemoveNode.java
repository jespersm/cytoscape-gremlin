package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

/**
 * Remove a node from a graph.
 */
public class RemoveNode extends GraphCommand {

    private final PropertyKey<Object> nodeId;

    private RemoveNode(PropertyKey<Object> nodeId) {
        this.nodeId = nodeId;
    }

    public static RemoveNode create(PropertyKey<Object> nodeId) {
        return new RemoveNode(nodeId);
    }

    @Override
    public void execute() throws CommandException {
        try {
            graphImplementation.removeNode(nodeId);
        } catch (GraphImplementationException e) {
            throw new CommandException(e);
        }
    }
}
