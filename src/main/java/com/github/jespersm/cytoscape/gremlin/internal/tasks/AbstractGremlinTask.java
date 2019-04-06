package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jespersm.cytoscape.gremlin.internal.client.AbstractGremlinGraphFactory;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;

public abstract class AbstractGremlinTask extends AbstractTask {
    protected final transient Services services;

    public AbstractGremlinTask(Services services) {
    	super();
        this.services = services;
    }

	protected Graph waitForGraph(TaskMonitor taskMonitor, ScriptQuery scriptQuery,
								 AbstractGremlinGraphFactory creator, String errorMessage)
			throws ExecutionException, InterruptedException
	{
	    CompletableFuture<Graph> result =
				CompletableFuture.supplyAsync(() -> getGraph(scriptQuery, creator));

		while (true) {
            if (this.cancelled) {
                result.cancel(true);
            }
            try {
				return result.get(100, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				// It's OK - loop!
			} catch (ExecutionException e) {
				// Oooo, not so good.
				if (taskMonitor != null) {
					taskMonitor.showMessage(Level.ERROR, errorMessage + ": " + e.getCause().getMessage());
				}
				throw e;
			} catch (InterruptedException e) {
				if (taskMonitor != null) {
					taskMonitor.showMessage(Level.ERROR, "Query interrupted");
				}
				throw e;
			}
		}
	}

	private Graph getGraph(ScriptQuery query) {
    	return getGraph(query, new GremlinGraphFactory());
	}

	private Graph getGraph(ScriptQuery query, AbstractGremlinGraphFactory creator) {
		try {
			return services
					.getGremlinClient()
					.getGraphAsync(query, creator)
					.get();
		} catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}

}
