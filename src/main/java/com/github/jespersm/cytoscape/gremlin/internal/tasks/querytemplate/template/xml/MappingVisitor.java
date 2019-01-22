package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml;

public interface MappingVisitor {
    void visit(ColumnMapping columnMapping);

    void visit(CopyAll copyAll);
}
