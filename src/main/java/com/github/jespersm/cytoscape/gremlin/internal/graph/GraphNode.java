package com.github.jespersm.cytoscape.gremlin.internal.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents a node in a graph.
 */
public class GraphNode implements GraphObject {

    private Map<String, Object> properties = new HashMap<>();
    private String label = null;
    private String id;

    public GraphNode(String id) {
        this.id = id;
    }

    @Override
    public void accept(GraphVisitor graphVisitor) {
        graphVisitor.visit(this);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }


    public <T> Optional<T> getProperty(String key, Class<T> clz) {
        if (key != null && clz != null) {
            Object value = properties.get(key);
            if (value != null && clz.isInstance(value)) {
                return Optional.of(clz.cast(value));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return id == graphNode.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    GraphNode merge(GraphNode that) {
    	if (this.label == null) {
    		this.label = that.label;
    	}
        this.properties.putAll(that.properties.keySet()
                .stream()
                .filter(key -> !this.properties.containsKey(key))
                .collect(Collectors.toMap(key -> key, key -> that.properties.get(key)))
        );
        return this;
    }
}
