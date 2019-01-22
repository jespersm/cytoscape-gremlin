package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template;

public class ReaderException extends Exception {
    public ReaderException(String msg) {
        super(msg);
    }

    public ReaderException(String msg, Throwable e) {
        super(msg, e);
    }
}
