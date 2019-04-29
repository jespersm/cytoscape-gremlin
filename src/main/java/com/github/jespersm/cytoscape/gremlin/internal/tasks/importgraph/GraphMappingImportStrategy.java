package com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph;

import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.EdgeColumnMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.GraphMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.NodeColumnMapping;

import java.util.Collection;
import java.util.Optional;

/**
 * This class implements an import strategy that uses a graph mapping (used by query templates)
 * for copying nodes and edges into cytoscape.
 */
public class GraphMappingImportStrategy implements ImportGraphStrategy {

    private final GraphMapping graphMapping;

    public GraphMappingImportStrategy(GraphMapping graphMapping) {
        this.graphMapping = graphMapping;
    }

    @Override
    public void createTables(CyNetwork network, Graph graph) {
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable edgeTable = network.getDefaultEdgeTable();

        for (NodeColumnMapping<?> nodeColumnMapping : graphMapping.getNodeColumnMapping()) {
            if (!columnExists(nodeTable, nodeColumnMapping.getColumnName())) {
                nodeTable.createColumn(nodeColumnMapping.getColumnName(), nodeColumnMapping.getColumnType(), true);
            }
        }

        for (EdgeColumnMapping<?> edgeColumnMapping : graphMapping.getEdgeColumnMapping()) {
            if (!columnExists(edgeTable, edgeColumnMapping.getColumnName())) {
                edgeTable.createColumn(edgeColumnMapping.getColumnName(), edgeColumnMapping.getColumnType(), true);
            }
        }
    }

    private boolean columnExists(CyTable cyTable, String columnName) {
        return cyTable.getColumns().stream().anyMatch(cyColumn -> cyColumn.getName().equals(columnName));
    }

    @Override
    public void copyNode(CyNetwork network, GraphNode graphNode) {
        CyNode cyNode = getNodeByIdOrElseCreate(network, graphNode.getId());
        CyRow cyRow = network.getRow(cyNode);
        for (NodeColumnMapping<?> nodeColumnMapping : graphMapping.getNodeColumnMapping()) {
            cyRow.set(nodeColumnMapping.getColumnName(), nodeColumnMapping.getValueExpression().eval(graphNode));
        }
    }

    @Override
    public void copyEdge(CyNetwork network, GraphEdge graphEdge) {

        if (edgeExists(network, graphEdge.getId())) {
            return;
        }
        Object start = graphEdge.getStart();
        Object end = graphEdge.getEnd();

        CyNode startNode = getNodeByIdOrElseCreate(network, start);
        CyNode endNode = getNodeByIdOrElseCreate(network, end);
        CyEdge cyEdge = network.addEdge(startNode, endNode, true);
        CyRow cyRow = network.getRow(cyEdge);

        for (EdgeColumnMapping<?> edgeColumnMapping : graphMapping.getEdgeColumnMapping()) {
            cyRow.set(edgeColumnMapping.getColumnName(), edgeColumnMapping.getValueExpression().eval(graphEdge));
        }
    }

    @Override
    public void postProcess(CyNetwork network) {

    }

    private boolean edgeExists(CyNetwork network, Object id) {
        String edgeRefereceIdColumn = graphMapping.getEdgeReferenceIdColumn();
        return !network
                .getDefaultEdgeTable()
                .getMatchingRows(edgeRefereceIdColumn, id)
                .isEmpty();
    }

    private CyNode getNodeByIdOrElseCreate(CyNetwork currNet, Object id) {
        return getNodeById(currNet, id).orElseGet(() -> createNewNode(currNet, id));
    }

    private Optional<CyNode> getNodeById(CyNetwork network, Object id) {
        String primaryKeyColumnName = network.getDefaultNodeTable().getPrimaryKey().getName();
        String nodeReferenceIdColumn = graphMapping.getNodeReferenceIdColumn();
        return network
                .getDefaultNodeTable()
                .getMatchingRows(nodeReferenceIdColumn, id)
                .stream()
                .findFirst()
                .map(row -> network.getNode(row.get(primaryKeyColumnName, Long.class)));
    }

    private CyNode createNewNode(CyNetwork currNet, Object id) {
        String nodeReferenceIdColumn = graphMapping.getNodeReferenceIdColumn();
        CyNode newNode = currNet.addNode();
        currNet.getRow(newNode).set(nodeReferenceIdColumn, id);
        return newNode;
    }

    @Override
    public String getRefIDName() {
        return null;
    }

}
