package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;

public class ImportQueryTask extends AbstractImportTask {
    public ImportQueryTask(Services services, String networkName, String visualStyle, DefaultImportStrategy defaultImportStrategy, ScriptQuery query) {
        super(services, networkName, visualStyle, defaultImportStrategy, query);
    }
}
