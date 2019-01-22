package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.stream.Stream;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

import com.github.jespersm.cytoscape.gremlin.internal.Services;

public class ExpandNodesTask extends AbstractExpandNodesTask {
    private Boolean onlySelected;

    public ExpandNodesTask(Services services, CyNetwork network, Boolean onlySelected, AbstractExpandNodesTask.Direction direction) {
        super(services, network, direction);
        this.onlySelected = onlySelected;
    }

    @Override
    Stream<CyRow> getNodeRows() {
    	if (this.onlySelected) {
    		return getSelectedNodes();
    	} else {
    		return this.network.getDefaultNodeTable().getAllRows().stream();
    	}
	}
}
