package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.util.Optional;

import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinGraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.graph.commands.Command;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.GraphImplementation;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportDifference;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportNetworkConfiguration;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportNew;

/**
 * This class exports a cytoscape graph to Gremlin.
 */
public class ExportNetworkToGremlinTask extends AbstractTask {

    private final Services services;
    private final ExportNetworkConfiguration exportNetworkConfiguration;

    public ExportNetworkToGremlinTask(Services services, ExportNetworkConfiguration exportNetworkConfiguration) {
        this.services = services;
        this.exportNetworkConfiguration = exportNetworkConfiguration;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        try {
            taskMonitor.setTitle("Export network to Gremlin");
            taskMonitor.setProgress(0);
            taskMonitor.setStatusMessage("Exporting network to Gremlin");
            CyNetwork cyNetwork = services.getCyApplicationManager().getCurrentNetwork();
            if (cyNetwork == null) {
                taskMonitor.showMessage(TaskMonitor.Level.WARN, "No network selected");
            } else {
                GraphImplementation graphImplementation = GremlinGraphImplementation.create(
                        services.getGremlinClient(),
                        TaskConstants.GREMLIN_PROPERTY_CYTOSCAPE_NETWORK,
                        exportNetworkConfiguration.getNodeLabel().getLabel()
                );
                Command command = getScriptQuery(cyNetwork).map(scriptQuery -> {
                    try {
                        Graph grapInDb = services.getGremlinClient()
                                .getGraphAsync(scriptQuery, new GremlinGraphFactory())
                                .get();
                        return ExportDifference.create(grapInDb, cyNetwork, graphImplementation).compute();
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                }).orElseGet(() -> ExportNew.create(cyNetwork, graphImplementation).compute());

                taskMonitor.setStatusMessage("Updating graph");
                // @TODO proper export: Label names from shared names and correct edge names and properties
                command.execute();
                taskMonitor.showMessage(TaskMonitor.Level.INFO, "Export completed");
            }

        } catch (Exception e) {
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
        }
    }

    private Optional<ScriptQuery> getScriptQuery(CyNetwork cyNetwork) {
        if (cyNetwork.getRow(cyNetwork).isSet("gremlin_query")) {
            String query = cyNetwork.getRow(cyNetwork).get("gremlin_query", String.class);
            return Optional.ofNullable(ScriptQuery.builder().query(query).build());
        } else {
            return Optional.empty();
        }
    }
}
