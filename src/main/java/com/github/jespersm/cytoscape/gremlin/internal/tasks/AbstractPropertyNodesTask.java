package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;
import org.codehaus.groovy.util.ListHashMap;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractPropertyNodesTask extends AbstractGremlinNetworkTask {
    private final ImportGraphStrategy importGraphStrategy;
    private final ImportGraphToCytoscape importer;

    public AbstractPropertyNodesTask(Services services, CyNetwork network) {
        super(services, network);
        this.importGraphStrategy = new DefaultImportStrategy();
        this.importer = new ImportGraphToCytoscape(this.network, this.importGraphStrategy, () -> this.cancelled);
    }

    abstract Stream<CyRow> getNodeRows();
        
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Getting node properties");

        List<Integer> ids = getNodeRows()
        		.map(row -> row.get(this.importGraphStrategy.getRefIDName(),String.class))
                .map(val -> Integer.parseInt(val) )
        		.collect(Collectors.toList());

        if (ids.isEmpty()) {
            taskMonitor.showMessage(Level.ERROR, "No nodes selected?");
            return;
        }

        String query = new StringBuilder("g.V(ids).as('v')")
                .append(".valueMap().with(WithOptions.tokens).as('p')")
                .append(".select('v','p').toList()")
                .toString();
        ScriptQuery scriptQuery = ScriptQuery.builder()
                .query(query)
                .params("ids", ids)
                .build();

        Graph graph = waitForGraph(taskMonitor, scriptQuery,
                "Error getting data from the Gremlin Server");

        taskMonitor.setStatusMessage("Importing from Gremlin server");
        this.importer.importGraph(graph);

        updateView();

//        reLayout();
    }

    protected void reLayout() {
		for (CyNetworkView cyNetworkView : this.services.getCyNetworkViewManager().getNetworkViews(network)) {
            services.getVisualMappingManager().getVisualStyle(cyNetworkView).apply(cyNetworkView);
            CyLayoutAlgorithm layout = services.getCyLayoutAlgorithmManager().getDefaultLayout();
            Set<View<CyNode>> nodes = new HashSet<>();
            insertTasksAfterCurrentTask(layout.createTaskIterator(cyNetworkView, layout.createLayoutContext(), nodes, null));
            cyNetworkView.updateView();
        }
	}

}
