package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;

public class ExpandNodeViewTask extends AbstractGremlinTask implements ActionListener {

	protected final View<CyNode> nodeView;
	protected final CyNetworkView netView;
	
    private final ImportGraphStrategy importGraphStrategy;
    private Boolean redoLayout;
    private String edge;
    private String node;
	private AbstractExpandNodesTask.Direction direction; 
    
	public ExpandNodeViewTask(View<CyNode> nodeView, CyNetworkView networkView, Services services, Boolean redoLayout) {
		super(services);
		this.nodeView = nodeView;
		this.netView = networkView;
		this.importGraphStrategy = new DefaultImportStrategy();
		this.redoLayout = redoLayout;
		this.edge = null;
		this.node = null;
		this.direction = AbstractExpandNodesTask.Direction.BIDIRECTIONAL;
	}
	
	public void setNode(String node) {
		this.node = node;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	private void expand() throws InterruptedException, ExecutionException {
        CyNode cyNode = this.nodeView.getModel();
		
		Long refid = this.netView.getModel().getRow(cyNode).get(this.importGraphStrategy.getRefIDName(), Long.class);
		String directionLeft = this.direction == Direction.IN ? "<" : "";
		String directionRight = this.direction == Direction.OUT ? ">" : "";
		
		String query;
		if (this.edge == null && this.node == null) {
			query = "match p=(n)" + directionLeft +  "-[r]-" + directionRight + "() where ID(n) = " + refid +" return p"; 
		}
		else if (this.node == null){
			query = "match p=(n)" + directionLeft +  "-[:"+this.edge+"]-" + directionRight + "() where ID(n) = " + refid +" return p";
		}
		else {
			query = "match p=(n)" + directionLeft +  "-[r]-" + directionRight + "(:" + this.node + ") where ID(n) = " + refid +" return p"; 
		}
		ScriptQuery scriptQuery = ScriptQuery.builder().query(query).build();
		
        Graph graph = waitForGraph(null, scriptQuery, new GremlinGraphFactory(),
				"problem connecting to server");

        ImportGraphToCytoscape importer = new ImportGraphToCytoscape(this.netView.getModel(), importGraphStrategy, () -> this.cancelled);

        importer.importGraph(graph);
        if (this.redoLayout) {
            CyLayoutAlgorithm layout = services.getCyLayoutAlgorithmManager().getDefaultLayout();
            Set<View<CyNode>> nodes = new HashSet<>();
            insertTasksAfterCurrentTask(layout.createTaskIterator(this.netView, layout.createLayoutContext(), nodes, null));
        }
        services.getVisualMappingManager().getVisualStyle(this.netView).apply(this.netView);
        this.netView.updateView();

    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

        taskMonitor.setTitle("Expanding a single node");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            this.expand();
        } catch (Exception exception) {

        }
    }

}
