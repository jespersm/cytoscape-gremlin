package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.stream.Stream;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;

import com.github.jespersm.cytoscape.gremlin.internal.Services;

public abstract class AbstractGremlinNetworkTask extends AbstractGremlinTask {
	protected final CyNetwork network;

    public AbstractGremlinNetworkTask(Services services, CyNetwork cyNetwork) {
    	super(services);
    	this.network = cyNetwork;
    }
    
	protected Stream<CyRow> getSelectedNodes() {
		return this.network.getDefaultNodeTable().getAllRows().stream().filter(
        		row -> row.get(CyNetwork.SELECTED, Boolean.class));
	}

	protected void updateView() {
		for (CyNetworkView cyNetworkView : this.services.getCyNetworkViewManager().getNetworkViews(network)) {
            cyNetworkView.updateView();
        }
	}

}
