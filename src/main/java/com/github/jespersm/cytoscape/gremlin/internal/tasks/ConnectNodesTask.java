/**
 *
 */
package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;


/**
 * @author sven
 */
public class ConnectNodesTask extends AbstractGremlinNetworkTask {
    private final transient Services services;
    private final ImportGraphStrategy importGraphStrategy;
    private Boolean onlySelected;

    public ConnectNodesTask(Services services, CyNetwork network, Boolean onlySelected) {
        super(services, network);
        this.services = services;
        this.importGraphStrategy = new DefaultImportStrategy();
        this.onlySelected = onlySelected;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Connecting nodes");

        List<CyRow> rows = this.network.getDefaultNodeTable().getAllRows();
        Stream<CyRow> rowStream = onlySelected ? getSelectedNodes() : rows.stream();
        
        List<Integer> idsQuery = rowStream
        		.map(row -> row.get(this.importGraphStrategy.getRefIDName(),String.class))
        		.map(val -> Integer.parseInt(val))
        		.collect(Collectors.toList());

        if (idsQuery.isEmpty()) {
            taskMonitor.showMessage(Level.ERROR, "No nodes selected?");
            return;
        }
        String query = "g.V(idsQuery).bothE().where(__.otherV().hasId(idsQuery)).dedup()";
        ScriptQuery scriptQuery = ScriptQuery.builder().query(query).params("idsQuery", idsQuery).build();

        Graph graph = waitForRespose(scriptQuery, taskMonitor);

        taskMonitor.setStatusMessage("Importing the Gremlin Graph");
        ImportGraphToCytoscape importer = new ImportGraphToCytoscape(this.network, importGraphStrategy, () -> this.cancelled);

        importer.importGraph(graph);

        updateView();
    }

    private Graph getGraph(ScriptQuery query) {
        try {
            return services.getGremlinClient().getGraph(query);
        } catch (GremlinClientException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
