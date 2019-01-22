package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;

/**
 * This class imports all nodes and edges from Gremlin into cytoscape.
 */
public class ImportAllNodesAndEdgesTask extends AbstractImportTask {

    public ImportAllNodesAndEdgesTask(Services services, String networkName, String visualStyleTitle) {
        super(
                services,
                networkName,
                visualStyleTitle,
                new DefaultImportStrategy(),
                ScriptQuery.builder().query(TaskConstants.MATCH_ALL_NODES_AND_EDGES).build());
    }

}
