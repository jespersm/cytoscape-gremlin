package com.github.jespersm.cytoscape.gremlin.internal.graph;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;

public class GraphTest {

    @Test
    public void addNode() {
        Graph graph = new Graph();
        graph.add(new GraphNode("1"));
        graph.add(new GraphNode("2"));
        assertEquals(2, graph.size());

        GraphNode node1 = new GraphNode("1");
        node1.getProperties().put("test", 123);
        graph.add(node1);
        assert (graph.size() == 2);

    }

    @Test
    public void add1() {
    }
}