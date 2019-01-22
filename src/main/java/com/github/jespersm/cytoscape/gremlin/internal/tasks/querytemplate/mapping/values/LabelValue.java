package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values;

import java.util.regex.Pattern;

import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;

/**
 * This class implements the value expression for a node label.
 */
public class LabelValue implements ValueExpression<GraphNode, String> {

    Pattern pattern;

    public LabelValue(String matches) {
        pattern = Pattern.compile(matches);
    }

    @Override
    public String eval(GraphNode val) {
        return pattern.matcher(val.getLabel()).find() ? val.getLabel() : "";
    }

    @Override
    public void accept(ValueExpressionVisitor visitor) {
        visitor.visit(this);
    }

    public Pattern getPattern() {
        return pattern;
    }
}
