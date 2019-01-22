package com.github.jespersm.cytoscape.gremlin.internal.graph.commands;

public abstract class Command {
    public abstract void execute() throws CommandException;
}
