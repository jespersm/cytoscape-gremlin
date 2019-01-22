package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.GraphMappingImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.CopyAllMappingStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.GraphMapping;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.MappingStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.mapping.MappingStrategyVisitor;

/**
 * This class implements a query template, it has a parameterized Gremlin query
 * and a mapping from nodes and edges to Cytoscape
 */
public class GremlinQueryTemplate {

    private String name;
    private String gremlinQuery;
    private Map<String, Class<?>> parameterTypes;
    private Map<String, Object> parameters;
    private MappingStrategy mapping;

    GremlinQueryTemplate(
            String name,
            String query,
            Map<String, Class<?>> parameterTypes,
            MappingStrategy mapping) {
        this.name = name;
        this.gremlinQuery = query;
        this.parameterTypes = new HashMap<>(parameterTypes);
        this.mapping = mapping;
        this.parameters = new HashMap<>();
        for (String key : parameterTypes.keySet()) {
            parameters.put(key, null);
        }
    }

    public ScriptQuery createQuery() {
        return ScriptQuery.builder()
                .query(gremlinQuery)
                .params(parameters)
                .build();
    }

    public MappingStrategy getMapping() {
        return mapping;
    }

    public String getName() {
        return name;
    }

    public String getGremlinQuery() {
        return gremlinQuery;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameterMap) {
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            if (this.parameters.containsKey(entry.getKey())) { //TODO: check type
                this.parameters.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public ImportGraphStrategy getImportGraphStrategy() {
        AtomicReference<ImportGraphStrategy> importGraphStrategy = new AtomicReference<>();
        mapping.accept(new MappingStrategyVisitor() {
            @Override
            public void visit(GraphMapping graphMapping) {
                importGraphStrategy.set(new GraphMappingImportStrategy(graphMapping));
            }

            @Override
            public void visit(CopyAllMappingStrategy copyAllMappingStrategy) {
                importGraphStrategy.set(new DefaultImportStrategy());
            }
        });
        return importGraphStrategy.get();
    }

    public static class Builder {

        private String query;
        private String name;
        private Map<String, Class<?>> parameterTypes = new HashMap<>();
        private MappingStrategy mappingStrategy;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setQueryTemplate(String query) {
            this.query = query;
            return this;
        }

        public Builder addParameter(String parameter, Class<?> type) {
            parameterTypes.put(parameter, type);
            return this;
        }

        public Builder addMappingStrategy(MappingStrategy mappingStrategy) {
            this.mappingStrategy = mappingStrategy;
            return this;
        }

        public GremlinQueryTemplate build() {
            return new GremlinQueryTemplate(
                    name,
                    query,
                    parameterTypes,
                    mappingStrategy
            );
        }
    }
}
