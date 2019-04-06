package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.groovy.util.ListHashMap;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;

public abstract class AbstractExpandNodesTask extends AbstractGremlinNetworkTask {
    private final ImportGraphStrategy importGraphStrategy;
    private final Direction direction;

	public enum Direction {
		IN(".inE()"),
		OUT(".outE()"),
        BIDIRECTIONAL(".bothE()");

		public String step;
        Direction(String step) { this.step = step; }
    }

    public AbstractExpandNodesTask(Services services, CyNetwork network, Direction direction) {
        super(services, network);
        this.importGraphStrategy = new DefaultImportStrategy();
        this.direction = direction;
    }

    abstract Stream<CyRow> getNodeRows();
        
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Expanding multiple nodes");

        List<Integer> ids = getNodeRows()
        		.map(row -> row.get(this.importGraphStrategy.getRefIDName(),String.class))
                .map(val -> Integer.parseInt(val) )
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            taskMonitor.showMessage(Level.ERROR, "No nodes selected?");
            return;
        }
        String query = new StringBuilder()
                .append("g.V(ids).as('v')")
                .append(this.direction.step).append(".as('e')")
                .append(".otherV().as('o')")
                .append(".select('v','e','o')")
                .toString();

        ScriptQuery scriptQuery = ScriptQuery.builder().query(query).params("ids",ids).build();
        Graph graph = waitForGraph(taskMonitor, scriptQuery,
                "problem connecting to server");

        taskMonitor.setStatusMessage("Importing from Gremlin server");
        ImportGraphToCytoscape importer = new ImportGraphToCytoscape(this.network, importGraphStrategy, () -> this.cancelled);

        importer.importGraph(graph);

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
