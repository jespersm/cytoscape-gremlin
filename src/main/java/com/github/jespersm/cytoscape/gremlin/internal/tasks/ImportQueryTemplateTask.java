package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.GremlinQueryTemplate;

/**
 * This class imports the results of a query template from Gremlin into cytoscape.
 */
public class ImportQueryTemplateTask extends AbstractImportTask {

    public ImportQueryTemplateTask(Services services, String networkName, String visualStyleTitle, GremlinQueryTemplate queryTemplate) {
        super(services, networkName, visualStyleTitle, queryTemplate.getImportGraphStrategy(), queryTemplate.createQuery());
    }
}
