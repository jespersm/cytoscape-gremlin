package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping;

public interface MappingStrategyVisitor {
    void visit(GraphMapping graphMapping);

    void visit(CopyAllMappingStrategy copyAllMappingStrategy);
}
