package com.github.jespersm.cytoscape.gremlin.internal.client;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a Gremlin script query.
 */
public class ScriptQuery {

    private final String query;
    private final Map<String, Object> params;

    ScriptQuery(String query, Map<String, Object> params) {
        this.query = query;
        this.params = params;
    }

    Map<String, Object> getParams() {
        return params;
    }

    public String getQuery() {
        return query;
    }

    String getExplainQuery() {
        return query + ".explain()";
    }

    public static final class Builder {

        private String query;
        private Map<String, Object> params;

        private Builder() {
            params = new HashMap<>();
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder params(String param, Object value) {
            this.params.put(param, value);
            return this;
        }

        public Builder params(Map<String, Object> map) {
            this.params.putAll(map);
            return this;
        }

        public ScriptQuery build() {
            return new ScriptQuery(query, params);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
