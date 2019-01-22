package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values;

import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;

/**
 * This class implements the value expresion for the id of an edge.
 */
public class EdgeId implements ValueExpression<GraphEdge, Object> {
    @Override
    public Object eval(GraphEdge val) {
        return val.getId();
    }

    @Override
    public void accept(ValueExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
