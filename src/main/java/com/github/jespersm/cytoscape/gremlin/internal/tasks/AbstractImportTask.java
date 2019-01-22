package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphToCytoscape;

/**
 * This class imports the results of a cyher query into cytoscape.
 */
public abstract class AbstractImportTask extends AbstractGremlinTask {

    private final String networkName;
    private final String visualStyleTitle;
    private final ImportGraphStrategy importGraphStrategy;
    private final ScriptQuery scriptQuery;

    public AbstractImportTask(Services services, String networkName, String visualStyleTitle, ImportGraphStrategy importGraphStrategy, ScriptQuery scriptQuery) {
        super(services);
        this.networkName = networkName;
        this.visualStyleTitle = visualStyleTitle;
        this.importGraphStrategy = importGraphStrategy;
        this.scriptQuery = scriptQuery;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        try {

            taskMonitor.setStatusMessage("Execute query");
            explainQuery(scriptQuery);

            Graph graph = waitForRespose(scriptQuery, taskMonitor);

            taskMonitor.setTitle("Importing the Gremlin Graph " + networkName);

            // setup network
            CyNetwork network = services.getCyNetworkFactory().createNetwork();
            network.getRow(network).set(CyNetwork.NAME, networkName);
            setGremlinQuery(network);

            services.getCyNetworkManager().addNetwork(network);

            ImportGraphToCytoscape importer = new ImportGraphToCytoscape(network, importGraphStrategy, () -> this.cancelled);

            taskMonitor.setStatusMessage("Importing graph");
            importer.importGraph(graph);

            CyEventHelper cyEventHelper = services.getCyEventHelper();
            cyEventHelper.flushPayloadEvents();

            taskMonitor.setStatusMessage("Creating View");
            CyNetworkView networkView = services.getCyNetworkViewFactory().createNetworkView(network);
            services.getCyNetworkViewManager().addNetworkView(networkView);

            taskMonitor.setStatusMessage("Applying Layout");
            Set<View<CyNode>> nodes = new HashSet<>();
            CyLayoutAlgorithm layout = services.getCyLayoutAlgorithmManager().getLayout("force-directed");
            insertTasksAfterCurrentTask(layout.createTaskIterator(networkView, layout.createLayoutContext(), nodes, null));

            VisualStyle visualStyle = services.getVisualMappingManager().getAllVisualStyles().stream()
                    .filter(vs -> vs.getTitle().equals(visualStyleTitle))
                    .findFirst().orElseGet(() -> services.getVisualMappingManager().getDefaultVisualStyle());
            visualStyle.apply(networkView);
            networkView.updateView();

        } catch (Exception e) {
            taskMonitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
        }
    }

    private void setGremlinQuery(CyNetwork network) throws IOException {
        CyTable cyTable = network.getDefaultNetworkTable();
        if (cyTable.getColumn("gremlin_query") == null) {
            network.getDefaultNetworkTable().createColumn("gremlin_query", String.class, true);
        }
        network.getRow(network).set("gremlin_query", scriptQuery.getQuery());
    }

    private void explainQuery(ScriptQuery scriptQuery) throws GremlinClientException {
        services.getGremlinClient().explainQuery(scriptQuery);
    }

}
