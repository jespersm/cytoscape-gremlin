package com.github.jespersm.cytoscape.gremlin.internal.test.model.fixtures;

import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClient;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementationException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.PropertyKey;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.TaskConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GremlinFixtures {

    private final GraphImplementation gi;
    private final GremlinClient gremlinClient;


    public GremlinFixtures(GremlinClient gremlinClient, String networkLabel) {
        this.gremlinClient = gremlinClient;
        this.gi = GremlinGraphImplementation.create(gremlinClient, TaskConstants.GREMLIN_PROPERTY_CYTOSCAPE_NETWORK, networkLabel);
    }

    interface CreateGraph {
        void create(GremlinClient gremlinClient, String networkLabel) throws GraphImplementationException;
    }

    public enum GremlinFixture {
        GRAPH_5_STAR(GremlinFixtures::createFiveStarGraph);

        private final CreateGraph createGraph;

        GremlinFixture(CreateGraph createGraph) {
            this.createGraph = createGraph;
        }

        public void create(GremlinClient gremlinClient, String networkLabel) throws GraphImplementationException {
            createGraph.create(gremlinClient, networkLabel);
        }

    }

    private static void createFiveStarGraph(GremlinClient gremlinClient, String networkLabel) throws GraphImplementationException {
        new GremlinFixtures(gremlinClient, networkLabel).createStarGraph(5);
    }

    private void createStarGraph(int n) throws GraphImplementationException {
        for (int i = 0; i < n; i++) {
            createNode(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    createEdge(i, j);
                }
            }
        }
    }

    private void createNode(int i) throws GraphImplementationException {
        Map<String, Object> props = new HashMap<>();
        props.put("nodeId", i);
        gi.addNode(Collections.emptyList(), props);
    }

    private void createEdge(int start, int end) throws GraphImplementationException {
        gi.addEdge(new PropertyKey("nodeId", start), new PropertyKey("nodeId", end), "REL_" + start + "_" + end, Collections.emptyMap());
    }

}
