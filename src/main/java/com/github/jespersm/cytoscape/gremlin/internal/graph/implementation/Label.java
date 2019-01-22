package com.github.jespersm.cytoscape.gremlin.internal.graph.implementation;

public abstract class Label {
    private final String label;

    public Label(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
