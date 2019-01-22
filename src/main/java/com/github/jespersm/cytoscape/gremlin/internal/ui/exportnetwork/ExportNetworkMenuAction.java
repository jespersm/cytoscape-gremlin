package com.github.jespersm.cytoscape.gremlin.internal.ui.exportnetwork;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyNetwork;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.Label;
import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExportNetworkToGremlinTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportNetworkConfiguration;
import com.github.jespersm.cytoscape.gremlin.internal.ui.DialogMethods;

import javax.swing.*;

import java.awt.event.ActionEvent;
import static com.github.jespersm.cytoscape.gremlin.internal.Constants.REF_ID;
import static com.github.jespersm.cytoscape.gremlin.internal.Constants.CYCOLUMN_GREMLIN_LABEL;

public class ExportNetworkMenuAction extends AbstractCyAction {

    /**
     *
     */
    private static final long serialVersionUID = -3105483618300742403L;
    private static final String MENU_TITLE = "Export Network to Gremlin";
    private static final String MENU_LOC = "Apps.Gremlin Queries";

    private final transient Services services;

    public static ExportNetworkMenuAction create(Services services) {
        return new ExportNetworkMenuAction(services);
    }

    private ExportNetworkMenuAction(Services services) {
        super(MENU_TITLE);
        this.services = services;
        setPreferredMenu(MENU_LOC);
        setEnabled(true);
        setMenuGravity(0.5f);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!DialogMethods.connect(services)) {
            return;
        }

        Label label = getNodeLabel();
        ExportNetworkConfiguration exportNetworkConfiguration = ExportNetworkConfiguration.create(
                label,
                "shared name",
                REF_ID,
                CYCOLUMN_GREMLIN_LABEL,
                "_gremlin_properties"
        );
        if (label != null) {
            ExportNetworkToGremlinTask task = services.getTaskFactory().createExportNetworkToGremlinTask(exportNetworkConfiguration);
            services.getTaskExecutor().execute(task);
        }
    }

    private Label getNodeLabel() {
        String message = "Enter the name for this network";
        CyNetwork currentNetwork = services.getCyApplicationManager().getCurrentNetwork();
        String initialValue = currentNetwork.getRow(currentNetwork).get(CyNetwork.NAME, String.class);

        while (true) {
            String label = JOptionPane.showInputDialog(services.getCySwingApplication().getJFrame(), message, initialValue);
            if (label != null) {
                try {
                    return NodeLabel.create(label);
                } catch (Exception e) {
                    message = "Error in network name ([A-Za-z0-9])";
                }
            } else {
                return null;
            }
        }
    }
}
