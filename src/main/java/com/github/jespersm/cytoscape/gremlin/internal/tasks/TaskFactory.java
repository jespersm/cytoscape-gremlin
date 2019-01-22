package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportNetworkConfiguration;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.GremlinQueryTemplate;

/**
 * This class creates executable commands:
 * - Import all nodes and edges
 * - Export network to Gremlin
 * - Import query template
 * - Import a query
 */
public class TaskFactory {

    private final Services services;

    public static TaskFactory create(Services services) {
        return new TaskFactory(services);
    }

    private TaskFactory(Services services) {
        this.services = services;
    }

    public ImportAllNodesAndEdgesTask createImportAllNodesAndEdgesFromGremlinTask(String network, String visualStyle) {
        return new ImportAllNodesAndEdgesTask(
                services,
                network,
                visualStyle);
    }

    public ExportNetworkToGremlinTask createExportNetworkToGremlinTask(ExportNetworkConfiguration exportNetworkConfiguration) {
        return new ExportNetworkToGremlinTask(services, exportNetworkConfiguration);
    }

    public ImportQueryTemplateTask createImportQueryTemplateTask(String networkName, GremlinQueryTemplate queryTemplate, String visualStyle) {
        return new ImportQueryTemplateTask(services, networkName, visualStyle, queryTemplate);
    }

    public AbstractImportTask createImportQueryTask(String networkName, ScriptQuery query, String visualStyle) {
        return new ImportQueryTask(services, networkName, visualStyle, new DefaultImportStrategy(), query);
    }
}
