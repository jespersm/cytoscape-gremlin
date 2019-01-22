package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values;

/**
 * This interface specifies a value expression that can be evaluated. It is used to map Gremlin nodes and edges to Cytoscape.
 *
 * @param <T>
 * @param <V>
 */
public interface ValueExpression<T, V> {
    V eval(T val);

    void accept(ValueExpressionVisitor visitor);
}
