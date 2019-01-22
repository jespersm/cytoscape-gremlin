package com.github.jespersm.cytoscape.gremlin.internal.graph;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This class represents an edge in a graph.
 */
public class GraphEdge implements GraphObject {
    private String start;
    private String end;
    private Map<String, Object> properties = new TreeMap<>();
    private String label;
    private String id;

    public GraphEdge() {
    }

    public GraphEdge(String id, String start, String end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    @Override
    public void accept(GraphVisitor graphVisitor) {
        graphVisitor.visit(this);
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStart() {
        return start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getEnd() {
        return end;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void setType(String type) {
        this.label = type;
    }

    public String getLabel() {
        return label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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


    GraphEdge merge(GraphEdge that) {
        this.properties.putAll(that.properties.keySet()
                .stream()
                .filter(key -> !this.properties.containsKey(key))
                .collect(Collectors.toMap(key -> key, key -> that.properties.get(key)))
        );
        if (that.label != null) {
            this.label = that.label;
        }
        return this;
    }
}
