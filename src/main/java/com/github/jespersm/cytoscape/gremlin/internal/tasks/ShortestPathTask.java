package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShortestPathTask extends AbstractGremlinNetworkTask implements Task {

    private DefaultImportStrategy importGraphStrategy;

    public ShortestPathTask(Services services, CyNetwork network) {
        super(services, network);
        this.importGraphStrategy = new DefaultImportStrategy();
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Finding shortest path");

        // get all ids
        List<CyRow> rows = this.network.getDefaultNodeTable().getAllRows();
        ArrayList<String> ids = new ArrayList<String>();
        for (CyRow row : rows) {
            if (row.get(CyNetwork.SELECTED, Boolean.class)) {
                ids.add(row.get(this.importGraphStrategy.getRefIDName(), Long.class).toString());
            }
        }

        if (ids.size() <= 1) {
            return;
        }

        HashMap<String, String> queryPaths = new HashMap<String, String>();
        int count = 0;

        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String path = "p_" + i + "_" + j;
                String in = String.format("[%s, %s]", ids.get(i), ids.get(j));
                String match = String.format("match %s=shortestpath((n%d)-[*]-(m%d)) where ID(n%d) in %s and ID(m%d) in %s and ID(n%d) <> ID(m%d)",
                        path, count, count, count, in, count, in, count, count);
                queryPaths.put(path, match);
                count++;
            }
        }
        String query = String.join("\n", queryPaths.values()) + "\n";
        query += "return " + String.join(",", queryPaths.keySet());

        ScriptQuery script = ScriptQuery.builder().query(query).build();

        Graph graph = waitForRespose(script, taskMonitor, "Could not find shortest path(s). Are you still connected to the database?");

        taskMonitor.setStatusMessage("Importing the Gremlin Graph");
        ImportGraphToCytoscape importer = new ImportGraphToCytoscape(this.network, importGraphStrategy, () -> this.cancelled);

        importer.importGraph(graph);
        for (CyNetworkView cyNetworkView : this.services.getCyNetworkViewManager().getNetworkViews(network)) {
            cyNetworkView.updateView();
        }

    }

    private Graph getGraph(ScriptQuery query) {
        try {
            return services.getGremlinClient().getGraph(query);
        } catch (GremlinClientException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
