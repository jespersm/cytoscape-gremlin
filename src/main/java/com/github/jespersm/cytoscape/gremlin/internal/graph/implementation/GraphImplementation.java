package com.github.jespersm.cytoscape.gremlin.internal.graph.implementation;

import java.util.List;
import java.util.Map;

public interface GraphImplementation {

    void addEdge(PropertyKey<Object> start, PropertyKey<Object> end, String label, Map<String, Object> properties) throws GraphImplementationException;

    void addNode(List<NodeLabel> labels, Map<String, Object> properties) throws GraphImplementationException;

    void removeEdge(PropertyKey<Object> edgeId) throws GraphImplementationException;

    void updateDirectedEdge(PropertyKey<Object> startId, PropertyKey<Object> endId, Map<String, Object> properties) throws GraphImplementationException;

    void removeAllEdgesFromNode(PropertyKey<Object> nodeId) throws GraphImplementationException;

    void removeNode(PropertyKey<Object> node) throws GraphImplementationException;

    void updateNode(PropertyKey<Object> nodeId, List<NodeLabel> labels, Map<String, Object> properties) throws GraphImplementationException;

    void updateEdgeById(PropertyKey<Object> edgeId, Map<String, Object> properties) throws GraphImplementationException;
}
