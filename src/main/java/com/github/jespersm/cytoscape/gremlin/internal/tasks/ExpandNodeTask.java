package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.stream.Stream;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import com.github.jespersm.cytoscape.gremlin.internal.Services;

public class ExpandNodeTask extends AbstractExpandNodesTask {

    private CyNode node;

	public ExpandNodeTask(Services services, CyNetwork network, CyNode node, Direction direction) {
        super(services, network, direction);
        this.node = node;
    }

	@Override
	Stream<CyRow> getNodeRows() {
		return Stream.of(network.getRow(node));
	}

}
