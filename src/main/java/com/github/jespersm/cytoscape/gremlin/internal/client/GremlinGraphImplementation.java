package com.github.jespersm.cytoscape.gremlin.internal.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;

public final class GremlinGraphImplementation implements GraphImplementation {

    private static final String START_ID = "startId";
    private static final String END_ID = "endId";
    private static final String PROPS = "props";
    private static final String EDGE_ID = "edgeId";
    private static final String NODE_ID = "nodeId";
    //TODO: move shared global statics to a separate class
    private final String cytoscapeNetworkPorpertyName;
    private final GremlinClient gremlinClient;
    private final String networkLabel;

    private GremlinGraphImplementation(GremlinClient gremlinClient, String cytoscapeNetworkPorpertyName, String networkLabel) {
        this.gremlinClient = gremlinClient;
        this.cytoscapeNetworkPorpertyName = cytoscapeNetworkPorpertyName;
        this.networkLabel = networkLabel;
    }

    public static GremlinGraphImplementation create(GremlinClient gremlinClient, String cytoscapeNetworkPorpertyName, String networkLabel) {
        return new GremlinGraphImplementation(gremlinClient, cytoscapeNetworkPorpertyName, networkLabel);
    }

    @Override
    public void addEdge(PropertyKey<Object> start, PropertyKey<Object> end, String relationship, Map<String, Object> properties) throws GraphImplementationException {
        ScriptQuery query = ScriptQuery.builder()
                .query(
                        "MATCH (s {" + matchNetworkProperty() + "}), " +
                                "(e {" + matchNetworkProperty() + "}) " +
                                "WHERE " +
                                where("s", start, START_ID) + " AND " +
                                where("e", end, END_ID) + " " +
                                "CREATE (s) -[:`" + relationship + "` $" + PROPS + "]-> (e)"
                )
                .params(START_ID, start.getValue())
                .params(END_ID, end.getValue())
                .params(PROPS, properties)
                .build();
        executeQuery(query);
    }


    @Override
    public void updateDirectedEdge(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties) throws GraphImplementationException {
        ScriptQuery query = ScriptQuery.builder()
                .query(
                        "MATCH (s {" + matchNetworkProperty() + "}) " +
                                " -[r]-> " +
                                "(e {" + matchNetworkProperty() + "}) " +
                                "WHERE " +
                                where("s", startId, START_ID) + " AND " +
                                where("e", endId, END_ID) + " " +
                                "SET r = $" + PROPS
                )
                .params(START_ID, startId.getValue())
                .params(END_ID, endId.getValue())
                .params(PROPS, properties)
                .build();
        executeQuery(query);
    }

    @Override
    public void updateEdgeById(PropertyKey<Object> edgeId, Map<String, Object> properties) throws GraphImplementationException {
        ScriptQuery query = ScriptQuery.builder()
                .query(
                        "MATCH (s {" + matchNetworkProperty() + "}) " +
                                " -[r]-> " +
                                "(e {" + matchNetworkProperty() + "}) " +
                                "WHERE " +
                                where("r", edgeId, EDGE_ID) + " " +
                                "SET r = $" + PROPS
                )
                .params(EDGE_ID, edgeId.getValue())
                .params(PROPS, properties)
                .build();
        executeQuery(query);
    }

    private String where(String alias, PropertyKey<Object> nodeId, String param) {
        if ("id".equalsIgnoreCase(nodeId.getName())) {
            return "id(" + alias + ") = $" + param + " ";
        } else {
            return alias + "." + nodeId.getName() + " = $" + param + " ";
        }
    }

    @Override
    public void removeEdge(PropertyKey<Object> edgeId) throws GraphImplementationException {
        ScriptQuery query = ScriptQuery.builder()
                .query(
                        "MATCH (s) - [r] - (e) " +
                                removeEdgeWhere(edgeId) +
                                " AND  s." + cytoscapeNetworkPorpertyName + "='" + networkLabel + "'" +
                                " AND  e." + cytoscapeNetworkPorpertyName + "='" + networkLabel + "'" +
                                "DELETE r"
                )
                .params(EDGE_ID, edgeId.getValue())
                .build();
        executeQuery(query);
    }

    private String removeEdgeWhere(PropertyKey<Object> edgeId) {
        if ("id".equalsIgnoreCase(edgeId.getName())) {
            return "WHERE id(r) = $" + EDGE_ID + " ";
        } else {
            return "WHERE r." + edgeId.getName() + " = $" + EDGE_ID + " ";
        }
    }

    @Override
    public void removeAllEdgesFromNode(PropertyKey<Object> nodeId) throws GraphImplementationException {
        ScriptQuery removerRelationsQuery = ScriptQuery.builder().query(
                "MATCH(n {" + nodeId.getName() + ":$" + NODE_ID + ", " + matchNetworkProperty() + "}) - [r] - (e) DELETE r")
                .params(NODE_ID, nodeId.getValue())
                .build();
        executeQuery(removerRelationsQuery);
    }

    @Override
    public void removeNode(PropertyKey<Object> nodeId) throws GraphImplementationException {
        if ("id".equalsIgnoreCase(nodeId.getName())) {
            ScriptQuery removerQuery = ScriptQuery.builder().query(
                    "MATCH(n) WHERE id(n) = $" + NODE_ID +
                            " AND  n." + cytoscapeNetworkPorpertyName + "='" + networkLabel + "'" +
                            "  DELETE n")
                    .params(NODE_ID, nodeId.getValue())
                    .build();
            executeQuery(removerQuery);
        } else {
            ScriptQuery removerQuery = ScriptQuery.builder().query(
                    "MATCH(n {" + nodeId.getName() + ":$" + NODE_ID + ", " + matchNetworkProperty() + "}) DELETE n")
                    .params(NODE_ID, nodeId.getValue())
                    .build();
            executeQuery(removerQuery);
        }
    }

    @Override
    public void addNode(List<NodeLabel> labels, Map<String, Object> properties) throws GraphImplementationException {
        String nodeLabelClause = getNodeLabelsClause(labels);
        ScriptQuery insertQuery = ScriptQuery.builder().query(
                "CREATE(n $" + PROPS + ") SET n." + cytoscapeNetworkPorpertyName + "='" + networkLabel + "' "
                        + (nodeLabelClause.isEmpty() ? "" : ", n" + nodeLabelClause))
                .params(PROPS, properties)
                .build();
        executeQuery(insertQuery);
    }

    @Override
    public void updateNode(PropertyKey<Object> nodeId, List<NodeLabel> labels, Map<String, Object> properties) throws GraphImplementationException {
        String nodeLabelClause = getNodeLabelsClause(labels);
        ScriptQuery updateQuery = ScriptQuery.builder().query(
                "MATCH(n) " +
                        "WHERE " + nodeIdClause("n", nodeId) + " " +
                        "SET n = $" + PROPS + ", n." + cytoscapeNetworkPorpertyName + " = '" + networkLabel + "' " +
                        (nodeLabelClause.isEmpty() ? "" : ", n" + nodeLabelClause))
                .params(NODE_ID, nodeId.getValue())
                .params(PROPS, properties)
                .build();
        executeQuery(updateQuery);
    }

    private String getNodeLabelsClause(List<NodeLabel> labels) {
        return labels.stream()
                .reduce(
                        "",
                        (str, label) -> str + ":`" + label.getLabel() + "`",
                        (s1, s2) -> s1 + s2
                );
    }

    private String nodeIdClause(String nodeAlias, PropertyKey<Object> nodeId) {
        if("id".equals(nodeId.getName())) {
            return "id(" + nodeAlias + ")=$" + NODE_ID;
        } else {
            return nodeAlias + "." + nodeId.getName() + "=$" + NODE_ID + " AND " + whereNetworkProperty(nodeAlias);
        }
    }

    private void executeQuery(ScriptQuery theQuery) throws GraphImplementationException {
    	try {
            gremlinClient.executeQueryAsync(theQuery);
        } catch (Exception ex) {
            throw new GraphImplementationException(ex);
        }
    }

    private String matchNetworkProperty() {
        return cytoscapeNetworkPorpertyName + ":'" + networkLabel + "'";
    }

    private String whereNetworkProperty(String nodeALias) {
        return nodeALias + "." + cytoscapeNetworkPorpertyName + " = '" + networkLabel + "'";
    }

}
