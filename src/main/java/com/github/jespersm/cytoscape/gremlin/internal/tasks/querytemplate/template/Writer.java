package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.GremlinQueryTemplate;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.CopyAllMappingStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.EdgeColumnMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.GraphMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.MappingStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.MappingStrategyVisitor;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.NodeColumnMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.EdgeId;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.EdgePropertyValue;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.EdgeScriptExpression;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.LabelValue;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.NodeId;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.NodePropertyValue;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.NodeScriptExpression;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.ValueExpression;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.ValueExpressionVisitor;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.ColumnMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.CopyAll;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.EdgeColumn;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.EdgeMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.EdgeType;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Expression;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Id;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Label;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Mapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.NodeColumn;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.NodeMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Parameter;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Parameters;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.Property;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.QueryTemplate;

import java.io.OutputStream;
import java.util.Map;

public class Writer {

    private static final JAXBContext jaxbContext = createJaxbContext();

    private static JAXBContext createJaxbContext() {
        try {
            return JAXBContext.newInstance(QueryTemplate.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot queryTemplate jaxb context for reading cyGremlin query template files");
        }
    }

    public void write(GremlinQueryTemplate template, OutputStream outputStream) throws JAXBException {
        QueryTemplate queryTemplate = queryTemplate(template);
        jaxbContext.createMarshaller().marshal(queryTemplate, outputStream);
    }

    private QueryTemplate queryTemplate(GremlinQueryTemplate gremlinQueryTemplate) {
        QueryTemplate queryTemplate = new QueryTemplate();
        queryTemplate.setName(gremlinQueryTemplate.getName());
        queryTemplate.setQuery(gremlinQueryTemplate.getGremlinQuery());
        queryTemplate.setParameters(parameters(gremlinQueryTemplate));
        queryTemplate.setMapping(mapping(gremlinQueryTemplate.getMapping()));
        return queryTemplate;

    }

    private Parameters parameters(GremlinQueryTemplate template) {
        Parameters parameters = new Parameters();
        for (Map.Entry<String, Class<?>> entry : template.getParameterTypes().entrySet()) {
            Parameter parameter = new Parameter();
            parameter.setName(entry.getKey());
            parameter.setType(entry.getValue().getName());
            parameters.getParameterList().add(parameter);
        }
        return parameters;
    }


    private Mapping mapping(MappingStrategy mappingStrategy) {
        ToMappingVisitor visitor = new ToMappingVisitor();
        mappingStrategy.accept(visitor);
        return visitor.getMapping();
    }

    private static class ToMappingVisitor implements MappingStrategyVisitor {
        Mapping mapping;

        public Mapping getMapping() {
            return mapping;
        }

        @Override
        public void visit(GraphMapping graphMapping) {
            ColumnMapping columnMapping = new ColumnMapping();

            NodeMapping nodeMapping = new NodeMapping();
            nodeMapping.setReferenceIdColumn(graphMapping.getNodeReferenceIdColumn());
            for (NodeColumnMapping nodeColumnMapping : graphMapping.getNodeColumnMapping()) {
                NodeColumn nodeColumn = new NodeColumn();

                nodeColumn.setName(nodeColumnMapping.getColumnName());
                nodeColumn.setType(nodeColumnMapping.getColumnType().getName());

                ValueExpression<?, ?> valueExpression = nodeColumnMapping.getValueExpression();
                ToNodeVisitor toNodeVisitor = new ToNodeVisitor(nodeColumn);
                valueExpression.accept(toNodeVisitor);
            }
            columnMapping.setNodeMapping(nodeMapping);

            EdgeMapping edgeMapping = new EdgeMapping();
            edgeMapping.setReferenceIdColumn(graphMapping.getEdgeReferenceIdColumn());
            columnMapping.setEdgeMapping(edgeMapping);
            for (EdgeColumnMapping edgeColumnMapping : graphMapping.getEdgeColumnMapping()) {
                EdgeColumn edgeColumn = new EdgeColumn();

                edgeColumn.setName(edgeColumnMapping.getColumnName());
                edgeColumn.setType(edgeColumnMapping.getColumnType().getName());
                edgeColumn.setEdgeType(new EdgeType());
                ValueExpression<?, ?> valueExpression = edgeColumnMapping.getValueExpression();
                ToEdgeVisitor toEdgeVisitor = new ToEdgeVisitor(edgeColumn);
                valueExpression.accept(toEdgeVisitor);
            }
            mapping = columnMapping;
        }

        @Override
        public void visit(CopyAllMappingStrategy copyAllMappingStrategy) {
            CopyAll copyAll = new CopyAll();
            copyAll.setNetwork(copyAllMappingStrategy.getNetworkName());
            copyAll.setReferenceIdColumn(copyAllMappingStrategy.getReferenceColumn());
            mapping = copyAll;
        }
    }

    private static class ToNodeVisitor implements ValueExpressionVisitor {
        private NodeColumn nodeColumn;

        public ToNodeVisitor(NodeColumn nodeColumn) {
            this.nodeColumn = nodeColumn;
        }

        @Override
        public void visit(NodeId nodeId) {
            nodeColumn.setId(new Id());
        }

        @Override
        public void visit(EdgeId edgeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> void visit(EdgePropertyValue<T> edgePropertyValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(EdgeScriptExpression vEdgeScriptExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(LabelValue labelValue) {
            Label label = new Label();
            label.setMatch(labelValue.getPattern().pattern());
            nodeColumn.setLabel(label);
        }

        @Override
        public <T> void visit(NodePropertyValue<T> tNodePropertyValue) {
            Property property = new Property();
            property.setKey(tNodePropertyValue.getKey());
            nodeColumn.setProperty(property);
        }

        @Override
        public <T> void visit(NodeScriptExpression<T> tNodeScriptExpression) {
            Expression expression = new Expression();
            expression.setValue(tNodeScriptExpression.getScript());
            nodeColumn.setExpression(expression);
        }
    }

    private static class ToEdgeVisitor implements ValueExpressionVisitor {
        private EdgeColumn edgeColumn;

        public ToEdgeVisitor(EdgeColumn edgeColumn) {
            this.edgeColumn = edgeColumn;
        }

        @Override
        public void visit(NodeId nodeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(EdgeId edgeId) {
            edgeColumn.setId(new Id());
        }

        @Override
        public <T> void visit(EdgePropertyValue<T> edgePropertyValue) {
            Property property = new Property();
            property.setKey(edgePropertyValue.getKey());
            edgeColumn.setProperty(property);
        }

        @Override
        public void visit(EdgeScriptExpression vEdgeScriptExpression) {
            Expression expression = new Expression();
            expression.setValue(vEdgeScriptExpression.getScript());
            edgeColumn.setExpression(expression);

        }

        @Override
        public void visit(LabelValue labelValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> void visit(NodePropertyValue<T> tNodePropertyValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> void visit(NodeScriptExpression<T> tNodeScriptExpression) {
            throw new UnsupportedOperationException();
        }
    }
}
