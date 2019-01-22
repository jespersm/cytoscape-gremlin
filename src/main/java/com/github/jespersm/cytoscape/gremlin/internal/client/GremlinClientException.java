package com.github.jespersm.cytoscape.gremlin.internal.client;

public class GremlinClientException extends Exception {
    public GremlinClientException(String message, Throwable t) {
        super(message, t);
    }

    public GremlinClientException() {
        super();
    }
}
