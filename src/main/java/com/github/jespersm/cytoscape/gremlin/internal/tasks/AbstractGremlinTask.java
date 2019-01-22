package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	protected Graph waitForRespose(ScriptQuery scriptQuery, TaskMonitor taskMonitor)
			throws InterruptedException, ExecutionException {
		return waitForRespose(scriptQuery, taskMonitor, "Error getting data from the Gremlin Server");
	}

	protected Graph waitForRespose(ScriptQuery scriptQuery, TaskMonitor taskMonitor, String errorMessage)
			throws InterruptedException, ExecutionException {
		
		CompletableFuture<Graph> result = CompletableFuture.supplyAsync(() -> getGraph(scriptQuery));
		return waitForGraph(taskMonitor, errorMessage, result);
	}

	private Graph waitForGraph(TaskMonitor taskMonitor, String errorMessage, CompletableFuture<Graph> result)
			throws ExecutionException, InterruptedException {
		while (true) {
            if (this.cancelled) {
                result.cancel(true);
            }
            try {
				return result.get(100, TimeUnit.MILLISECONDS);
				// If cancelled or failed, it will throw
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
        try {
            return services.getGremlinClient().getGraph(query);
        } catch (GremlinClientException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
	protected static String quote(String s) {
		return "\"" + s.replace("\"", "\\\"") + "\"";
	}

}
