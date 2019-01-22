package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values;

import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;

/**
 * This class implements the value expression for a node id.
 */
public class NodeId implements ValueExpression<GraphNode, Object> {
    @Override
    public Object eval(GraphNode val) {
        return val.getId();
    }

    @Override
    public void accept(ValueExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
