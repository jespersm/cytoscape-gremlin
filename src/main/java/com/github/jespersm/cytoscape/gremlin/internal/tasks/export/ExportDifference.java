package com.github.jespersm.cytoscape.gremlin.internal.tasks.export;

import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.AddEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.AddNode;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.Command;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.CommandBuilder;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.RemoveEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.RemoveNode;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.UpdateDirectedEdgeByStartAndEndNodeId;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.UpdateNode;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import static com.github.jespersm.cytoscape.gremlin.internal.Constants.REF_ID;
import static com.github.jespersm.cytoscape.gremlin.internal.Constants.SUID;
import static com.github.jespersm.cytoscape.gremlin.internal.Constants.CYCOLUMN_GREMLIN_LABEL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportDifference {

    private final Graph graph;
    private final CyNetwork cyNetwork;
    private final CommandBuilder commandBuilder = new CommandBuilder();
    private final List<GraphNode> visitedNode = new ArrayList<>();
    private final List<GraphEdge> visitedEdges = new ArrayList<>();

    public static ExportDifference create(Graph graph, CyNetwork cyNetwork, GraphImplementation graphImplementation) {
        return new ExportDifference(graph, cyNetwork, graphImplementation);
    }

    private ExportDifference(Graph graph, CyNetwork cyNetwork, GraphImplementation graphImplementation) {
        this.graph = graph;
        this.cyNetwork = cyNetwork;
        this.commandBuilder.graphImplementation(graphImplementation);
    }

    public Command compute() {
        visitedNode.clear();
        for (CyNode cyNode : cyNetwork.getNodeList()) {
            if (nodeExistsInGraph(cyNode)) {
                visit(cyNode);
                commandBuilder.updateNode(refId(cyNode), labels(cyNode), properties(cyNode));
            } else {
                commandBuilder.addNode(labels(cyNode), properties(cyNode));
            }
        }

        unvisitedNodes().forEach(removeNode -> commandBuilder.removeNode(new PropertyKey<>("id", removeNode.getId())));

        visitedEdges.clear();
        for (CyEdge cyEdge : cyNetwork.getEdgeList()) {
            if (edgeExistsInGraph(cyEdge)) {
                visit(cyEdge);
                commandBuilder.updateEdgeById(refId(cyEdge), properties(cyEdge));
            } else {
                PropertyKey<Object> startId = nodeId(cyEdge.getSource());
                PropertyKey<Object> endId = nodeId(cyEdge.getTarget());
                commandBuilder.addEdge(startId, endId, properties(cyEdge), relationship(cyEdge));
            }
        }
        unvisitedEdges().forEach(removeEdge -> commandBuilder.removeEdge(new PropertyKey<>("id", removeEdge.getId())));
        return commandBuilder.sort(this::compareCommands).build();
    }

    private String relationship(CyEdge cyEdge) {
        return cyNetwork.getRow(cyEdge).get("name", String.class, "relationship");
    }

    private int compareCommands(Command command1, Command command2) {
        return Integer.compare(arity(command1), arity(command2));
    }

    private int arity(Command command) {
        if (command instanceof RemoveEdge) return 0;
        if (command instanceof RemoveNode) return 1;
        if (command instanceof AddNode) return 2;
        if (command instanceof AddEdge) return 3;
        if (command instanceof UpdateNode) return 4;
        if (command instanceof UpdateDirectedEdgeByStartAndEndNodeId) return 5;
        return 6;
    }

    private Stream<GraphEdge> unvisitedEdges() {
        return graph.edges().stream().filter(edge -> !visitedEdges.contains(edge));
    }

    private Stream<GraphNode> unvisitedNodes() {
        return graph.nodes().stream().filter(node -> !visitedNode.contains(node));
    }

    private void visit(CyNode cyNode) {
        GraphNode node = graph
                .getNodeById(cyNetwork.getRow(cyNode).get(REF_ID, Long.class))
                .orElseThrow(() -> new IllegalStateException("Could not find node for cyNode SUID: " + cyNode.getSUID()));
        visitedNode.add(node);
    }

    private void visit(CyEdge cyEdge) {
        GraphEdge edge = graph
                .getEdgeById(cyNetwork.getRow(cyEdge).get(REF_ID, Long.class))
                .orElseThrow(() -> new IllegalStateException("Could not find edge for cyEdge SUID: " + cyEdge.getSUID()));
        visitedEdges.add(edge);
    }

    private PropertyKey<Object> nodeId(CyNode node) {
        if (nodeExistsInGraph(node)) {
            return refId(node);
        } else {
            return suid(node);
        }
    }

    private PropertyKey<Object> refId(CyNode node) {
        return new PropertyKey<>("id", cyNetwork.getRow(node).get(REF_ID, Long.class));
    }

    private PropertyKey<Object> refId(CyEdge edge) {
        return new PropertyKey<>("id", cyNetwork.getRow(edge).get(REF_ID, Long.class));
    }

    private PropertyKey<Object> suid(CyNode node) {
        return new PropertyKey<>(SUID, node.getSUID());
    }

    private Map<String, Object> properties(CyIdentifiable cyIdentifiable) {
        CyRow cyRow = cyNetwork.getRow(cyIdentifiable);
        return cyRow.getAllValues();
    }

    private List<NodeLabel> labels(CyNode cyNode) {
        CyRow cyRow = cyNetwork.getRow(cyNode);
        if (cyRow.isSet(CYCOLUMN_GREMLIN_LABEL) ) {
            if (cyRow.getRaw(CYCOLUMN_GREMLIN_LABEL) instanceof List) {
                List<String> labels = cyRow.getList(CYCOLUMN_GREMLIN_LABEL, String.class);
                if (labels != null) {
                    return labels.stream()
                            .map(NodeLabel::create)
                            .collect(Collectors.toList());

                }
            } else {
                String labelsAsString = cyRow.get(CYCOLUMN_GREMLIN_LABEL, String.class, "[]");
                return parseLabels(labelsAsString).stream()
                        .map(NodeLabel::create)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private boolean nodeExistsInGraph(CyNode cyNode) {
        CyRow cyRow = cyNetwork.getRow(cyNode);
        return nodeExistsInGraph(cyRow);
    }

    private boolean edgeExistsInGraph(CyEdge cyEdge) {
        CyRow cyRow = cyNetwork.getRow(cyEdge);
        return edgeExistsInGraph(cyRow);
    }

    private boolean nodeExistsInGraph(CyRow row) {
        return refId(row).map(graph::containsNodeId).orElse(false);
    }

    private boolean edgeExistsInGraph(CyRow row) {
        return refId(row).map(graph::containsEdgeId).orElse(false);
    }

    private Optional<Long> refId(CyRow row) {
        if (row.isSet(REF_ID)) {
            return Optional.ofNullable(row.get(REF_ID, Long.class));
        } else {
            return Optional.empty();
        }
    }

    private List<String> parseLabels(String labels) {
        return Collections.emptyList(); // TODO what's this
    }
}
