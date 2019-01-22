package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

public class CommandBuilder {

    private GraphImplementation graphImplementation;
    private List<Command> commandList = new ArrayList<>();

    public CommandBuilder graphImplementation(GraphImplementation graphImplementation) {
        this.graphImplementation = graphImplementation;
        return this;
    }

    public CommandBuilder addEdge(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties, String relationship) {
        AddEdge addEdge = AddEdge.create(startId, endId, properties, relationship);
        addEdge.setGraphImplementation(graphImplementation);
        commandList.add(addEdge);
        return this;
    }


    public CommandBuilder updateNode(PropertyKey<Object> nodeId, List<NodeLabel> labels, Map<String, Object> properties) {
        UpdateNode updateNode = UpdateNode.create(nodeId, labels, properties);
        updateNode.setGraphImplementation(graphImplementation);
        commandList.add(updateNode);
        return this;
    }

    public CommandBuilder addNode(List<NodeLabel> labels, Map<String, Object> properties) {
        AddNode addNode = AddNode.create(labels, properties);
        addNode.setGraphImplementation(graphImplementation);
        commandList.add(addNode);
        return this;
    }

    public CommandBuilder removeNode(PropertyKey<Object> nodeId) {
        RemoveNode removeNode = RemoveNode.create(nodeId);
        removeNode.setGraphImplementation(graphImplementation);
        commandList.add(removeNode);
        return this;
    }

    public CommandBuilder updateDirectedEdge(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties) {
        UpdateDirectedEdgeByStartAndEndNodeId updateDirectedEdgeByStartAndEndNodeId = UpdateDirectedEdgeByStartAndEndNodeId.create(startId, endId, properties);
        updateDirectedEdgeByStartAndEndNodeId.setGraphImplementation(graphImplementation);
        commandList.add(updateDirectedEdgeByStartAndEndNodeId);
        return this;
    }

    public CommandBuilder updateEdgeById(PropertyKey<Object> edgeId, Map<String, Object> properties) {
        UpdateEdgeById updateEdgeById = UpdateEdgeById.create(edgeId, properties);
        updateEdgeById.setGraphImplementation(graphImplementation);
        commandList.add(updateEdgeById);
        return this;
    }

    public CommandBuilder removeEdge(PropertyKey<Object> edgeId) {
        RemoveEdge removeEdge = RemoveEdge.create(edgeId);
        removeEdge.setGraphImplementation(graphImplementation);
        commandList.add(removeEdge);
        return this;
    }

    public CommandBuilder sort(Comparator<Command> comparator) {
        commandList.sort(comparator);
        return this;
    }

    public Command build() {
        return ComposedCommand.create(commandList);
    }
}
