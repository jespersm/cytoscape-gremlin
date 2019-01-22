package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import java.util.Map;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

/**
 * This class implements the 'Add Edge' command that can be executed by a GremlinClient.
 */
public class AddEdge extends GraphCommand {

    private String relationship = "relationsip";
    private final Map<String, Object> properties;
    private final PropertyKey<Object> startId;
    private final PropertyKey<Object> endId;

    public static AddEdge create(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties, String relationship) {
        return new AddEdge(startId, endId, properties, relationship);
    }

    private AddEdge(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties, String relationship) {
        this.startId = startId;
        this.endId = endId;
        this.properties = properties;
        this.relationship = relationship;
    }

    @Override
    public void execute() throws CommandException {
        try {
            graphImplementation.addEdge(startId, endId, relationship, properties);
        } catch (GraphImplementationException e) {
            throw new CommandException(e);
        }
    }
}
