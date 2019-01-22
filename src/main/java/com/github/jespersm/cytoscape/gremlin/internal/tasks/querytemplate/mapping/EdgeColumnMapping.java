package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping;

import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.ValueExpression;

/**
 * This class implements a mapping from a value expression to a Cytoscape column.
 *
 * @param <T>
 */
public class EdgeColumnMapping<T> {
    private final String columnName;
    private final Class<T> columnType;
    private final ValueExpression<GraphEdge, T> valueExpression;

    public EdgeColumnMapping(String columnName, Class<T> columnType, ValueExpression<GraphEdge, T> valueExpression) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.valueExpression = valueExpression;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<T> getColumnType() {
        return columnType;
    }

    public ValueExpression<GraphEdge, T> getValueExpression() {
        return valueExpression;
    }
}
