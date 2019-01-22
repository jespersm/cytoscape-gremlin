package com.github.jespersm.cytoscape.gremlin.internal.tasks.export;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.Command;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.CommandBuilder;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportNew {

    private static final String SUID = "SUID";
    private static final String GREMLIN_LABEL = "_label";
    private final CyNetwork cyNetwork;
    private final CommandBuilder commandBuilder = new CommandBuilder();

    public static ExportNew create(CyNetwork cyNetwork, GraphImplementation graphImplementation) {
        return new ExportNew(cyNetwork, graphImplementation);
    }

    private ExportNew(CyNetwork cyNetwork, GraphImplementation graphImplementation) {
        this.cyNetwork = cyNetwork;
        this.commandBuilder.graphImplementation(graphImplementation);
    }

    public Command compute() {
        for (CyNode cyNode : cyNetwork.getNodeList()) {
            commandBuilder.addNode(getNodeLabel(cyNode), getProperties(cyNode));
        }
        for (CyEdge cyEdge : cyNetwork.getEdgeList()) {
            commandBuilder.addEdge(suid(cyEdge.getSource()), suid(cyEdge.getTarget()), getProperties(cyEdge), relationship(cyEdge));
        }
        return commandBuilder.build();
    }

    private String relationship(CyEdge cyEdge) {
        return cyNetwork.getRow(cyEdge).get("name", String.class, "relationship");
    }

    private PropertyKey<Object> suid(CyIdentifiable cyIdentifiable) {
        return new PropertyKey<>(SUID, cyIdentifiable.getSUID());
    }

    private Map<String, Object> getProperties(CyIdentifiable cyIdentifiable) {
        CyRow cyRow = cyNetwork.getRow(cyIdentifiable);
        Map<String, Object> properties = new HashMap<>(cyRow.getAllValues());
        properties.put(SUID, cyIdentifiable.getSUID());
        return properties;
    }

    private List<NodeLabel> getNodeLabel(CyIdentifiable cyIdentifiable) {
        CyRow cyRow = cyNetwork.getRow(cyIdentifiable);
        List<NodeLabel> labels = new ArrayList<>();
        String name = cyRow.get("name", String.class);
        if (name != null) {
            labels.add(NodeLabel.create(name));
        }
        if (cyRow.isSet(GREMLIN_LABEL)) {
            List<NodeLabel> moreLabels = cyRow.getList(GREMLIN_LABEL, String.class).stream()
                    .map(NodeLabel::create)
                    .collect(Collectors.toList());
            labels.addAll(moreLabels);
        }
        return labels;
    }
}
