package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import java.util.List;
import java.util.Map;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

/**
 * Update a node
 */
public class UpdateNode extends GraphCommand {

    private PropertyKey<Object> nodeId;
    private List<NodeLabel> labels;
    private Map<String, Object> properties;

    public static UpdateNode create(PropertyKey<Object> nodeId, List<NodeLabel> labels, Map<String, Object> properties) {
        return new UpdateNode(nodeId, labels, properties);
    }

    public UpdateNode(PropertyKey<Object> nodeId, List<NodeLabel> labels, Map<String, Object> properties) {
        this.nodeId = nodeId;
        this.labels = labels;
        this.properties = properties;
    }

    @Override
    public void execute() throws CommandException {
        try {
            graphImplementation.updateNode(nodeId, labels, properties);
        } catch (GraphImplementationException e) {
            throw new CommandException(e);
        }
    }
}
