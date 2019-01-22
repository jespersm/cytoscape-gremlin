package com.github.jespersm.cytoscape.gremlin.internal.tasks;

public class CancelTaskException extends RuntimeException {
    public CancelTaskException(String msg) {
        super(msg);
    }
}
