package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping;

public interface MappingStrategy {
    void accept(MappingStrategyVisitor visitor);
}
