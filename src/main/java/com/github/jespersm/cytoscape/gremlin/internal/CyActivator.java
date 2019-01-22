package com.github.jespersm.cytoscape.gremlin.internal;

import static org.cytoscape.work.ServiceProperties.APPS_MENU;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClient;
import com.github.jespersm.cytoscape.gremlin.internal.configuration.AppConfiguration;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.TaskExecutor;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.TaskFactory;
import com.github.jespersm.cytoscape.gremlin.internal.ui.connect.ConnectInstanceMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.expand.ConnectNodesMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.expand.ExpandNodeEdgeMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.expand.ExpandNodeLabelMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.expand.ExpandNodeMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.expand.ExpandNodesMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.exportnetwork.ExportNetworkMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.importgraph.all.ImportAllNodesAndEdgesMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.importgraph.query.ImportGremlinQueryMenuAction;
import com.github.jespersm.cytoscape.gremlin.internal.ui.shortestpath.ShortestPathMenuAction;

/**
 * This class is the entrypoint of the application,
 * it loads the configuration,
 * creates an object that holds references to cytoscape classes,
 * adds menu items.
 */
public class CyActivator extends AbstractCyActivator {

    private AppConfiguration appConfiguration = new AppConfiguration();

    @Override
    public void start(BundleContext context) throws Exception {
        appConfiguration.load();
        Services services = createServices(context);

        ConnectInstanceMenuAction connectAction = ConnectInstanceMenuAction.create(services);
        ImportGremlinQueryMenuAction importGremlinQueryMenuAction = ImportGremlinQueryMenuAction.create(services);
        ImportAllNodesAndEdgesMenuAction importAllNodesAndEdgesMenuAction = ImportAllNodesAndEdgesMenuAction.create(services);
        registerAllServices(context, connectAction, new Properties());
        
//        ExportNetworkMenuAction exportNetworkToGremlinMenuAction = ExportNetworkMenuAction.create(services);
//        registerAllServices(context, exportNetworkToGremlinMenuAction, new Properties());

        registerAllServices(context, importGremlinQueryMenuAction, new Properties());
        registerAllServices(context, importAllNodesAndEdgesMenuAction, new Properties());

        Properties expandProperties = new Properties();
        expandProperties.setProperty(PREFERRED_MENU, "Apps.Gremlin Queries");
        expandProperties.setProperty(TITLE, "Connect all nodes");
        ConnectNodesMenuAction connectNodesMenuAction = ConnectNodesMenuAction.create(services, false);
        registerAllServices(context, connectNodesMenuAction, expandProperties);

        expandProperties = new Properties();
        expandProperties.setProperty(PREFERRED_MENU, "Apps.Gremlin Queries");
        expandProperties.setProperty(TITLE, "Connect all selected nodes");
        connectNodesMenuAction = ConnectNodesMenuAction.create(services, true);
        registerAllServices(context, connectNodesMenuAction, expandProperties);

        expandProperties = new Properties();
        expandProperties.setProperty(PREFERRED_MENU, "Apps.Gremlin Queries");
        expandProperties.setProperty(TITLE, "Expand all nodes, bidirectional");
        ExpandNodesMenuAction expandNodesMenuAction = ExpandNodesMenuAction.create(services, false, Direction.BIDIRECTIONAL);
        registerAllServices(context, expandNodesMenuAction, expandProperties);
        expandProperties.setProperty(TITLE, "Expand all nodes, incoming only");
        expandNodesMenuAction = ExpandNodesMenuAction.create(services, false, Direction.IN);
        registerAllServices(context, expandNodesMenuAction, expandProperties);
        expandProperties.setProperty(TITLE, "Expand all nodes, outgoing only");
        expandNodesMenuAction = ExpandNodesMenuAction.create(services, false, Direction.OUT);
        registerAllServices(context, expandNodesMenuAction, expandProperties);

        expandProperties = new Properties();
        expandProperties.setProperty(PREFERRED_MENU, "Apps.Gremlin Queries");
        expandProperties.setProperty(TITLE, "Expand all selected nodes, bidirectional");
        expandNodesMenuAction = ExpandNodesMenuAction.create(services, true, Direction.BIDIRECTIONAL);
        registerAllServices(context, expandNodesMenuAction, expandProperties);
        expandProperties.setProperty(TITLE, "Expand all selected nodes, incoming only");
        expandNodesMenuAction = ExpandNodesMenuAction.create(services, true, Direction.IN);
        registerAllServices(context, expandNodesMenuAction, expandProperties);
        expandProperties.setProperty(TITLE, "Expand all selected nodes, outgoing only");
        expandNodesMenuAction = ExpandNodesMenuAction.create(services, true, Direction.OUT);
        registerAllServices(context, expandNodesMenuAction, expandProperties);

        Properties shortestPathProperties = new Properties();
        shortestPathProperties.setProperty(PREFERRED_MENU, "Apps.Gremlin Queries");
        shortestPathProperties.setProperty(TITLE, "Get shortest paths between selected nodes");
        ShortestPathMenuAction shortestPathMenuAction = ShortestPathMenuAction.create(services);
//        registerAllServices(context, shortestPathMenuAction, shortestPathProperties);

        /*
         *  Context menus
         */
        Properties contextProperties = new Properties();
        //expandProperties.setProperty("preferredTaskManager", "menu");
        contextProperties.setProperty(PREFERRED_MENU, "Gremlin");
        contextProperties.setProperty(APPS_MENU, "Apps");
        contextProperties.setProperty(IN_CONTEXT_MENU, "true");

        contextProperties.setProperty(TITLE, "Expand node");
        ExpandNodeMenuAction expandNodeMenuAction = ExpandNodeMenuAction.create(services, false, Direction.BIDIRECTIONAL);
        registerAllServices(context, expandNodeMenuAction, contextProperties);

        contextProperties.setProperty(TITLE, "Expand node, bidirectional");
        expandNodeMenuAction = ExpandNodeMenuAction.create(services, true, Direction.BIDIRECTIONAL);
        registerAllServices(context, expandNodeMenuAction, contextProperties);

        contextProperties.setProperty(TITLE, "Expand node, incoming edges");
        expandNodeMenuAction = ExpandNodeMenuAction.create(services, true, Direction.IN);
        registerAllServices(context, expandNodeMenuAction, contextProperties);

        contextProperties.setProperty(TITLE, "Expand node, outgoing edges");
        expandNodeMenuAction = ExpandNodeMenuAction.create(services, true, Direction.OUT);
        registerAllServices(context, expandNodeMenuAction, contextProperties);

        contextProperties = new Properties();
        contextProperties.setProperty(PREFERRED_MENU, "Gremlin");
        contextProperties.setProperty(IN_CONTEXT_MENU, "true");
        ExpandNodeEdgeMenuAction expandNodeEdgeMenuAction = new ExpandNodeEdgeMenuAction(services);
//        registerAllServices(context, expandNodeEdgeMenuAction, contextProperties);
        ExpandNodeLabelMenuAction expandNodeLabelMenuAction = new ExpandNodeLabelMenuAction(services);
//        registerAllServices(context, expandNodeLabelMenuAction, contextProperties);

    }

    private Services createServices(BundleContext context) {
        Services services = new Services();
        services.setAppConfiguration(appConfiguration);
        services.setCySwingApplication(getService(context, CySwingApplication.class));
        services.setCyApplicationManager(getService(context, CyApplicationManager.class));
        services.setCyNetworkFactory(getService(context, CyNetworkFactory.class));
        services.setCyNetworkManager(getService(context, CyNetworkManager.class));
        services.setCyNetworkViewManager(getService(context, CyNetworkViewManager.class));
        services.setDialogTaskManager(getService(context, DialogTaskManager.class));
        services.setCyNetworkViewFactory(getService(context, CyNetworkViewFactory.class));
        services.setCyLayoutAlgorithmManager(getService(context, CyLayoutAlgorithmManager.class));
        services.setVisualMappingManager(getService(context, VisualMappingManager.class));
        services.setCyEventHelper(getService(context, CyEventHelper.class));
        services.setVisualStyleFactory(getService(context, VisualStyleFactory.class));
        services.setGremlinClient(new GremlinClient());
        services.setTaskFactory(TaskFactory.create(services));
        services.setTaskExecutor(TaskExecutor.create(services));
        return services;
    }

}
