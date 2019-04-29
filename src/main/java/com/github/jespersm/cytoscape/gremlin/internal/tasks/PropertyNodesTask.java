package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;

import java.util.stream.Stream;

public class PropertyNodesTask extends AbstractPropertyNodesTask {
    private Boolean onlySelected;

    public PropertyNodesTask(Services services, CyNetwork network, Boolean onlySelected) {
        super(services, network);
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
