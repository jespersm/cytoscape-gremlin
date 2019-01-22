package com.github.jespersm.cytoscape.gremlin.internal;

import static com.github.jespersm.cytoscape.gremlin.internal.test.model.fixtures.CyNetworkFixtures.emptyNetwork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.jespersm.cytoscape.gremlin.internal.client.ConnectionParameter;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClient;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.Command;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.CommandException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExpandNodeTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.TaskConstants;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportDifference;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportNew;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;
import com.github.jespersm.cytoscape.gremlin.internal.test.model.fixtures.CyNetworkFixtures;
import com.github.jespersm.cytoscape.gremlin.internal.test.model.fixtures.GremlinFixtures;

public class CyGremlinGraphIT {

    private String host = System.getenv("CYTOSCAPE_GREMLIN_HOST");
    private String user = System.getenv("CYTOSCAPE_GREMLIN_USER");
    private String passwd = System.getenv("CYTOSCAPE_GREMLIN_PASS");

    private GremlinClient gremlinClient = new GremlinClient();

    @Before
    public void setUp() throws GremlinClientException {
        assertNotNull("hostname not set", host);
        assertNotNull("user not set", user);
        assertNotNull("password not set", passwd);
        assertTrue("could not connect to Gremlin server", gremlinClient.connect(new ConnectionParameter(host, 8182, false, false, "g", user, passwd.toCharArray())));
    }

    @After
    public void tearDown() {
        gremlinClient.close();
    }

    @Test
    public void testImport() throws CommandException, GremlinClientException {

        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .whenImportGraph()
                .thenGraphHas("All nodes should be imported", 12, Steps.Types.NODES)
                .thenGraphHas("All edges should be imported", 17, Steps.Types.EDGES)
                .thenGraphEdgesAllMatch("All edges have a (lowercase) label", this::edgeHasLowercaseLabel)
                .thenGraphNodesAllMatch("All nodes must have a name", this::nodeHasNonEmptyName)
                .thenGraphNodesAllMatch("All nodes must have at exactly one label", this::nodeHasOneLabel)
                .thenNetworkHas(12, Steps.Types.NODES)
                .thenNetworkHas(17, Steps.Types.EDGES);
    }

    @Test
    public void testImportAndExpand() throws Exception {

        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .whenImportQuery("g.V().has(label, 'god')")
                .thenGraphHas("All nodes should be imported", 3, Steps.Types.NODES)
                .thenGraphHas("No edges should be imported", 0, Steps.Types.EDGES)
                .thenNetworkHas(3, Steps.Types.NODES)
                .thenNetworkHas(0, Steps.Types.EDGES)
                .whenExpandNode((cyNetwork, node) -> cyNetwork.getRow(node).get("name",String.class).equals("pluto"))
                .thenNetworkHas(5, Steps.Types.NODES)
                .thenNetworkHas(6, Steps.Types.EDGES);
    }
    
    /*
    
    @Test
    public void testImportQueryExport() throws CommandException, GremlinClientException, GraphImplementationException {

        Function query = (networkLabel) ->
                MessageFormat.format(
                        "MATCH (n) -[r]-> (m) WHERE n.{0} = ''{1}'' AND m.{0} = ''{1}''  RETURN n,r,m LIMIT 5",
                        TaskConstants.GREMLIN_PROPERTY_CYTOSCAPE_NETWORK,
                        networkLabel
                );

        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .givenGremlinFixture(GRAPH_5_STAR)
                .whenImportQuery(query)
                .whenExportDifferenceFromQuery(query)
                .whenImportGraph()
                .thenGraphHas("All nodes should be imported", 5, Steps.Types.NODES)
                .thenGraphHas("All nodes should have an edge", graphHasAtLeastEdges(5))
                .thenGraphNodesAllMatch("All nodes must have a network property",
                        nodeHasProperty(TaskConstants.GREMLIN_PROPERTY_CYTOSCAPE_NETWORK, String.class));
    }

    private Predicate<Graph> graphHasAtLeastEdges(int count) {
        return (graph) -> graph.edges().size() >= count;
    }


    @Test
    public void testExportDifference_AddNode_AddEdge() throws CommandException, GremlinClientException {
        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .whenExportGraph(NETWORK_WITH_3_NODES_1_EDGE)
                .whenImportGraph()
                .whenAddNode()
                .whenAddEdge(matchNodeProperty("name", "b"), matchNodeProperty("name", "c"))
                .whenExportDifference()
                .whenImportGraph()
                .thenNetworkHas(4, Steps.Types.NODES)
                .thenNetworkHas(2, Steps.Types.EDGES);
    }

    @Test
    public void testExportDifference_RemoveNode_RemoveEdge() throws CommandException, GremlinClientException, GraphImplementationException {
        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .givenGremlinFixture(GRAPH_5_STAR)
                .whenImportGraph()
                .whenDetachNode(matchNodeProperty("nodeId", 1l))
                .whenRemoveNode(matchNodeProperty("nodeId", 1l))
                .whenRemoveEdge(matchEdge(matchNodeProperty("nodeId", 2l), matchNodeProperty("nodeId", 3l)))
                .whenExportDifference()
                .whenImportGraph()
                .thenNetworkHas(4, Steps.Types.NODES)
                .thenNetworkHas(11, Steps.Types.EDGES);
    }

    @Test
    public void testImport5StarGraph() throws GremlinClientException, GraphImplementationException {
        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .givenGremlinFixture(GRAPH_5_STAR)
                .whenImportGraph()
                .thenNetworkHas(5, Steps.Types.NODES)
                .thenNetworkHas(20, Steps.Types.EDGES);
    }

    @Test
    public void testAddNodeProperty() throws GremlinClientException, GraphImplementationException, CommandException {
        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .givenCyNetwork(NETWORK_WITH_3_NODES_1_EDGE)
                .whenAddNodeColumn("my_int", Integer.class, 1)
                .whenExportDifference()
                .whenImportGraph()
                .thenGraphNodesAllMatch("Not all nodes have the expected property", nodeHasProperty("my_int", 1l));

    }


    @Test
    public void testUpdateNodeProperty() throws GremlinClientException, GraphImplementationException, CommandException {
        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .givenCyNetwork(NETWORK_WITH_3_NODES_1_EDGE)
                .whenAddNodeColumn("my_int", Integer.class, 1)
                .whenExportDifference()
                .whenImportGraph()
                .whenUpdateNodeProperty(matchNodeProperty("name", "a"), "my_int", 2l)
                .whenExportDifference()
                .whenImportGraph()
                .thenNodesExists("There should exists a node with the expected property", matchNodeProperty("my_int", 2l));
    }

    @Test
    public void testRemoveNodeProperty() throws GremlinClientException, GraphImplementationException, CommandException {
        Steps.newInstance(gremlinClient)
                .givenRandomNetwork()
                .givenCyNetwork(NETWORK_WITH_3_NODES_1_EDGE)
                .whenRemoveNodeColumn(CyNetworkFixtures.MY_PROPERTY)
                .whenExportDifference()
                .whenImportGraph()
                .thenGraphNodesAllMatch("There should not exists a node with this property [" + CyNetworkFixtures.MY_PROPERTY + "]",
                        nodeHasProperty(CyNetworkFixtures.MY_PROPERTY, Long.class).negate()
                );

    }
*/
    private <T> Predicate<GraphNode> nodeHasProperty(String key, Class<T> clz) {
        return node -> node
                .getProperty(key, clz)
                .isPresent();
    }


    private <T> Predicate<GraphNode> nodeHasProperty(String key, T value) {
        return node -> node
                .getProperty(key, value.getClass())
                .filter( p -> p.equals(value))
                .isPresent();
    }


    private <T> BiPredicate<CyNetwork, CyNode> matchNodeProperty(String key, T value) {
        return (network, node) ->
                network.getRow(node).isSet(key) &&
                        network.getRow(node).get(key, value.getClass()).equals(value);
    }

    private BiPredicate<CyNetwork, CyEdge> matchEdge(BiPredicate<CyNetwork, CyNode> selectSource, BiPredicate<CyNetwork, CyNode> selectTarget) {
        return (network, edge) -> {
            Predicate<CyNode> predicateSource = cyNode -> selectSource.test(network, cyNode);
            Predicate<CyNode> predicateTarget = cyNode -> selectTarget.test(network, cyNode);
            CyNode source = network.getNodeList().stream().filter(predicateSource).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            CyNode target = network.getNodeList().stream().filter(predicateTarget).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            return edge.getSource().equals(source) && edge.getTarget().equals(target);
        };
    }

    private boolean edgeHasLowercaseLabel(GraphEdge edge) {
		return edge.getLabel() != null && edge.getLabel().equals(edge.getLabel().toLowerCase());
    }

    private boolean nodeHasOneLabel(GraphNode node) {
        return node.getLabel() != null;
    }

    private boolean nodeHasNonEmptyName(GraphNode node) {
        return node.getProperty("name", String.class).filter(name -> !name.isEmpty()).isPresent();
    }

    private static final class Steps {

        private final class NullMonitor implements TaskMonitor {
			@Override
			public void showMessage(Level level, String message) {
			}

			@Override
			public void setTitle(String title) {
			}

			@Override
			public void setStatusMessage(String statusMessage) {
			}

			@Override
			public void setProgress(double progress) {
			}
		}

		public enum Types {
            NODES, EDGES;
        }
        private String networkLabel;
        private GremlinClient gremlinClient;

        private GremlinGraphImplementation graphImplementation;
        private Graph graph;
        private CyNetwork cyNetwork;
        public static Steps newInstance(GremlinClient gremlinClient) {
            return new Steps(gremlinClient);
        }
        private Steps(GremlinClient gremlinClient) {
            this.gremlinClient = gremlinClient;
        }

        public Steps givenCyNetwork(CyNetworkFixtures.CyFixture cyFixture) {
            cyNetwork = cyFixture.getNetwork();
            return this;
        }

        public Steps givenGremlinFixture(GremlinFixtures.GremlinFixture neo4jFixture) throws GraphImplementationException {
            neo4jFixture.create(gremlinClient, networkLabel);
            return this;
        }

        public Steps givenRandomNetwork() {
            this.networkLabel = randomLabel();
            graphImplementation = GremlinGraphImplementation.create(gremlinClient, TaskConstants.GREMLIN_PROPERTY_CYTOSCAPE_NETWORK, networkLabel);
            cyNetwork = emptyNetwork();
            return this;
        }

        public Steps whenRemoveNodeColumn(String columnName) {
            cyNetwork.getDefaultNodeTable().deleteColumn(columnName);
            return this;
        }

        public Steps whenImportQuery(Function<String, String> gremlinQuery) throws GremlinClientException {
            ScriptQuery query = ScriptQuery.builder().query(gremlinQuery.apply(networkLabel)).build();
            graph = gremlinClient.getGraph(query);
            importGraphToCytoscape();
            return this;
        }

        public <T> Steps whenAddNodeColumn(String columnName, Class<T> columnClass, T defaultValue) {
            if(cyNetwork.getDefaultNodeTable().getColumn(columnName) != null) {
                cyNetwork.getDefaultNodeTable().deleteColumn(columnName);
            }
            cyNetwork.getDefaultNodeTable().createColumn(columnName, columnClass, false, defaultValue);
            return this;
        }

        public <T> Steps whenUpdateNodeProperty(BiPredicate<CyNetwork, CyNode> selectNode, String columnName, T value) {
            Predicate<CyNode> nodePredicate = cyNode -> selectNode.test(cyNetwork, cyNode);
            CyNode cyNode = cyNetwork.getNodeList().stream().filter(nodePredicate).findFirst().orElseThrow(() -> new IllegalArgumentException("Node not found in network"));
            cyNetwork.getRow(cyNode).set(columnName, value);
            return this;
        }

        public Steps whenExportGraph(CyNetworkFixtures.CyFixture cyNetworkFixture) throws CommandException {
            ExportNew.create(cyNetworkFixture.getNetwork(), graphImplementation).compute().execute();
            return this;
        }

        public Steps whenImportGraph() throws GremlinClientException {
            this.graph = gremlinClient.getGraph(importAllNodesAndEdges(networkLabel));
            importGraphToCytoscape();
            return this;
        }

        public Steps whenImportQuery(String query) throws GremlinClientException {
            this.graph = gremlinClient.getGraph(importQuery(networkLabel, query));
            importGraphToCytoscape();
            return this;
        }
        
        public Steps whenExportDifferenceFromQuery(Function<String, String> query) throws CommandException, GremlinClientException {
            ScriptQuery gremlinQuery = ScriptQuery.builder().query(query.apply(networkLabel)).build();
            Graph neo4jGraph = gremlinClient.getGraph(gremlinQuery );
            ExportDifference exportDifference = ExportDifference.create(neo4jGraph, cyNetwork, graphImplementation);
            Command command = exportDifference.compute();
            command.execute();
            return this;
        }

        public Steps whenExportDifference() throws CommandException, GremlinClientException {
            Graph neo4jGraph = gremlinClient.getGraph(importAllNodesAndEdges(networkLabel));
            ExportDifference exportDifference = ExportDifference.create(neo4jGraph, cyNetwork, graphImplementation);
            Command command = exportDifference.compute();
            command.execute();
            return this;
        }

        public Steps whenAddNode() {
            CyNode cyNode = cyNetwork.addNode();
            return this;
        }

        public Steps whenDetachNode(BiPredicate<CyNetwork, CyNode> selectNode) {
            Predicate<CyNode> nodePredicate = cyNode -> selectNode.test(cyNetwork, cyNode);
            CyNode cyNode = cyNetwork.getNodeList().stream().filter(nodePredicate).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            List<CyEdge> edgestoRemove = cyNetwork.getEdgeList().stream().filter(cyEdge -> cyEdge.getSource().equals(cyNode) || cyEdge.getTarget().equals(cyNode)).collect(Collectors.toList());
            cyNetwork.removeEdges(edgestoRemove);
            return this;
        }

        public Steps whenRemoveNode(BiPredicate<CyNetwork, CyNode> selectNode) {
            Predicate<CyNode> nodePredicate = cyNode -> selectNode.test(cyNetwork, cyNode);
            CyNode cyNode = cyNetwork.getNodeList().stream().filter(nodePredicate).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            cyNetwork.removeNodes(Arrays.asList(cyNode));
            return this;
        }

        public Steps whenRemoveEdge(BiPredicate<CyNetwork, CyEdge> selectEdge) {
            Predicate<CyEdge> edgePredicate = cyEdge -> selectEdge.test(cyNetwork, cyEdge);
            CyEdge cyEdge = cyNetwork.getEdgeList().stream().filter(edgePredicate).findFirst().orElseThrow(() -> new IllegalArgumentException("CyEdge not found"));
            cyNetwork.removeEdges(Arrays.asList(cyEdge));
            return this;
        }


        public Steps whenAddEdge(BiPredicate<CyNetwork, CyNode> selectSource, BiPredicate<CyNetwork, CyNode> selectTarget) {
            Predicate<CyNode> sourcePredicate = cyNode -> selectSource.test(cyNetwork, cyNode);
            Predicate<CyNode> targetPredicate = cyNode -> selectTarget.test(cyNetwork, cyNode);
            CyNode source = cyNetwork.getNodeList().stream().filter(sourcePredicate).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            CyNode target = cyNetwork.getNodeList().stream().filter(targetPredicate).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            cyNetwork.addEdge(source, target, true);
            return this;
        }

        public Steps thenGraphEdgesAllMatch(String message, Predicate<GraphEdge> predicate) {
            assertTrue(message, graph.edges().stream().allMatch(predicate));
            return this;
        }

        public Steps thenGraphNodesAllMatch(String message, Predicate<GraphNode> predicate) {
            assertTrue(message, graph.nodes().stream().allMatch(predicate));
            return this;
        }

        public Steps thenGraphHas(int expected, Types types) {
            thenGraphHas("Unexpected number of " + types.name(), expected, types);
            return this;
        }

        public Steps thenGraphHas(String message, int expected, Types types) {
            switch (types) {
                case NODES:
                    assertEquals(message, expected, graph.nodes().size());
                    break;
                case EDGES:
                    assertEquals(message, expected, graph.edges().size());
                    break;
            }
            return this;
        }

        public Steps thenGraphHas(String message, Predicate<Graph> predicate) {
            assertTrue(message, predicate.test(graph));
            return this;
        }


        public Steps thenNetworkHas(int expected, Types types) {
            switch (types) {
                case NODES:
                    assertEquals("Unexpected number of nodes in graph.", expected, cyNetwork.getNodeList().size());
                    break;
                case EDGES:
                    assertEquals("Unexpected number of edges in graph.", expected, cyNetwork.getEdgeList().size());
                    break;
            }
            return this;
        }

        public Steps thenNodesExists(String message, BiPredicate<CyNetwork, CyNode> predicate) {
            Predicate<CyNode> nodePredicate = (cyNode) -> predicate.test(this.cyNetwork, cyNode);
            assertTrue(message, cyNetwork.getNodeList().stream().anyMatch(nodePredicate));
            return this;
        }

        private String randomLabel() {
            return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        }

        private ScriptQuery importAllNodesAndEdges(String networkLabel) {
            String query = TaskConstants.MATCH_ALL_NODES_AND_EDGES;
            return ScriptQuery.builder().query(query).build();
        }

        private ScriptQuery importQuery(String networkLabel, String gremlinQuery) {
            return ScriptQuery.builder().query(gremlinQuery).build();
        }

        private void importGraphToCytoscape() {
            this.cyNetwork = emptyNetwork();
            ImportGraphToCytoscape importGraphToCytoscape = new ImportGraphToCytoscape(cyNetwork, new DefaultImportStrategy(), () -> false);
            importGraphToCytoscape.importGraph(graph);
        }
        
        public Steps whenExpandNode(BiPredicate<CyNetwork, CyNode> isNode) throws Exception {
            CyNode source = cyNetwork.getNodeList().stream().filter(node -> isNode.test(cyNetwork,  node)).findFirst().orElseThrow(() -> new IllegalArgumentException("CyNode not found"));
            
            Services s = new Services();
            s.setGremlinClient(gremlinClient);
            
        	ExpandNodeTask expandNodeTask = new ExpandNodeTask(s, cyNetwork, source, AbstractExpandNodesTask.Direction.BIDIRECTIONAL) {
        		@Override
        		protected void reLayout() {
        			// Not in the test
        		}
        	};
        	expandNodeTask.run(new NullMonitor());
        	return this;
        }

    }
}
