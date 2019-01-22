package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.GremlinQueryTemplate;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.CopyAllMappingStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.GraphMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.EdgeId;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.EdgePropertyValue;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.EdgeScriptExpression;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.LabelValue;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.NodeId;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.NodePropertyValue;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.values.NodeScriptExpression;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.ColumnMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.CopyAll;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.MappingVisitor;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml.QueryTemplate;

import java.io.InputStream;

public class Reader {

	private static final JAXBContext jaxbContext = createJaxbContext();

	private static JAXBContext createJaxbContext() {
		try {
			return JAXBContext.newInstance(QueryTemplate.class);
		} catch (JAXBException e) {
			throw new IllegalStateException("Cannot create jaxb context for reading cyGremlin query template files");
		}
	}

	public GremlinQueryTemplate read(InputStream inputStream) throws ReaderException {
		try {
			StreamSource source = new StreamSource(inputStream);
			JAXBElement<QueryTemplate> jaxbElement = jaxbContext.createUnmarshaller().unmarshal(source,
					QueryTemplate.class);
			return create(jaxbElement.getValue());
		} catch (JAXBException e) {
			throw new ReaderException("Failed processing template", e);
		} catch (InternalReaderException e) {
			throw new ReaderException(e.getMessage());
		}
	}

	private GremlinQueryTemplate create(QueryTemplate xml) {

		GremlinQueryTemplate.Builder builder = GremlinQueryTemplate.builder();

		if (xml.getName() == null) {
			throw new InternalReaderException("Name is a required attribute");
		}
		builder.setName(xml.getName());
		builder.setQueryTemplate(xml.getQuery());

		xml.getParameters().getParameterList().forEach(cyQueryParameter -> {
			Class<?> type = getType(cyQueryParameter.getType());
			builder.addParameter(cyQueryParameter.getName(), type);
		});

		GraphMapping.Builder graphMappingBuilder = GraphMapping.builder();

		xml.getMapping().accept(new MyMappingVisitor(graphMappingBuilder, builder));
		return builder.build();
	}

	private Class<?> getType(String type) {
		try {
			return ClassLoader.getSystemClassLoader().loadClass(type);
		} catch (ClassNotFoundException e) {
			throw new InternalReaderException("Cannot load type: " + type);
		}
	}

	private class MyMappingVisitor implements MappingVisitor {

		private final GraphMapping.Builder graphMappingBuilder;
		private final GremlinQueryTemplate.Builder builder;

		private MyMappingVisitor(GraphMapping.Builder graphMappingBuilder, GremlinQueryTemplate.Builder builder) {
			this.graphMappingBuilder = graphMappingBuilder;
			this.builder = builder;
		}

		@Override
		public void visit(ColumnMapping columnMapping) {

			graphMappingBuilder.setNodeReferenceIdColumn(columnMapping.getNodeMapping().getReferenceIdColumn());

			columnMapping.getNodeMapping().getColumnList().forEach(column -> {
				Class<?> columnType = getType(column.getType());
				if (column.getId() != null) {
					graphMappingBuilder.addNodeColumnMapping(column.getName(), Object.class, new NodeId());
				}
				if (column.getProperty() != null) {
					graphMappingBuilder.addNodeColumnMapping(column.getName(), columnType,
							new NodePropertyValue(column.getProperty().getKey(), columnType));
				}
				if (column.getLabel() != null) {
					graphMappingBuilder.addNodeColumnMapping(column.getName(), String.class,
							new LabelValue(column.getLabel().getMatch()));
				}
				if (column.getExpression() != null) {
					graphMappingBuilder.addNodeColumnMapping(column.getName(), columnType,
							new NodeScriptExpression(column.getExpression().getValue(), columnType));
				}
			});

			graphMappingBuilder.setEdgeReferenceIdColumn(columnMapping.getEdgeMapping().getReferenceIdColumn());

			columnMapping.getEdgeMapping().getColumnList().forEach(column -> {
				Class<?> columnType = getType(column.getType());
				if (column.getId() != null) {
					graphMappingBuilder.addEdgeColumnMapping(column.getName(), Object.class, new EdgeId());
				}
				if (column.getProperty() != null) {
					graphMappingBuilder.addEdgeColumnMapping(column.getName(), columnType,
							new EdgePropertyValue(column.getProperty().getKey(), columnType));
				}
				if (column.getExpression() != null) {
					graphMappingBuilder.addEdgeColumnMapping(column.getName(), columnType,
							new EdgeScriptExpression(column.getExpression().getValue(), columnType));
				}
			});
			builder.addMappingStrategy(graphMappingBuilder.build());
		}

		@Override
		public void visit(CopyAll copyAll) {
			builder.addMappingStrategy(
					new CopyAllMappingStrategy(copyAll.getReferenceIdColumn(), copyAll.getNetwork()));
		}
	}
}
