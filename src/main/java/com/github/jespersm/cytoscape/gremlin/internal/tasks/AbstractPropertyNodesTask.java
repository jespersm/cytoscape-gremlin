package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.AbstractGremlinGraphFactory;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphFactory;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphObject;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphSimple;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.codehaus.groovy.util.ListHashMap;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import java.util.*;
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

    /**
     * The queries make use of "parameterized scripts"
     * see http://tinkerpop.apache.org/docs/current/reference/#parameterized-scripts
     * This also makes in unnecessary to quote the vertex ids (for injection into a query string).
     *
     * The problem being resolved here is the case where a node does not have its properties.
     * This query gets a list of vertices and their properties and updates the graph with them.
     *
     *
     * @param taskMonitor
     * @throws Exception
     */
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


        Graph graph = waitForGraph(taskMonitor, scriptQuery,  new PropertyGraphFactory(),
                "Error getting data from the Gremlin Server");

        taskMonitor.setStatusMessage("Importing from Gremlin server");
        this.importer.importGraph(graph);

        updateView();

//        reLayout();
    }


    /**
     * When properties are retrieved via Gremlin they may be multi-valued.
     * Cytoscape does not support multi-valued objects (I think) so only
     * the first element is retained.
     */
    static class PropertyGraphFactory implements  AbstractGremlinGraphFactory {

        @Override
        public GraphObject create(Result result) {
            Object ro = result.getObject();
            if (!(ro instanceof Map)) { return new GraphSimple(""); }
            Map<String,Object> r = (Map<String,Object>) ro;

            Object vo = r.get("v");
            if (!(vo instanceof Vertex)) { return new GraphSimple("");  }

            Object po = r.get("p");
            if (!(po instanceof Map)) { return new GraphSimple(""); }

            Vertex v = (Vertex) vo;
            Map<String,Object> p = (Map<String,Object>) po;

            Map<String,Object> q = new HashMap<>(p.size());
            for (Map.Entry ent : p.entrySet()) {
                if (ent.getKey().toString().equals("id")) continue;
                if (ent.getKey().toString().equals("label")) continue;
                Object value = ((List)ent.getValue()).get(0);
                q.put(ent.getKey().toString(), value);
            }
            return  AbstractGremlinGraphFactory.create(v, q);
        }
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
