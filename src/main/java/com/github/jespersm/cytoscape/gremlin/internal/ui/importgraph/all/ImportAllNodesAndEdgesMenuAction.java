package com.github.jespersm.cytoscape.gremlin.internal.ui.importgraph.all;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.vizmap.VisualStyle;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractImportTask;
import com.github.jespersm.cytoscape.gremlin.internal.ui.DialogMethods;

import java.awt.event.ActionEvent;
import java.util.stream.Collectors;

public class ImportAllNodesAndEdgesMenuAction extends AbstractCyAction {

    private static final String MENU_TITLE = "Import all nodes and edges from Gremlin";
    private static final String MENU_LOC = "Apps.Gremlin Queries";

    private final transient Services services;

    public static ImportAllNodesAndEdgesMenuAction create(Services services) {
        return new ImportAllNodesAndEdgesMenuAction(services);
    }

    private ImportAllNodesAndEdgesMenuAction(Services services) {
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

        ImportAllNodesAndEdgesDialog importAllNodesAndEdgesDialog = new ImportAllNodesAndEdgesDialog(services.getCySwingApplication().getJFrame(), getAllVisualStyleTitle());
        importAllNodesAndEdgesDialog.showDialog();
        if (importAllNodesAndEdgesDialog.isExecuteQuery()) {

            AbstractImportTask importAllNodesAndEdgesFromGremlinTask =
                    services.getTaskFactory().createImportAllNodesAndEdgesFromGremlinTask(
                            importAllNodesAndEdgesDialog.getNetwork(),
                            importAllNodesAndEdgesDialog.getVisualStyleTitle()
                    );
            services.getTaskExecutor().execute(importAllNodesAndEdgesFromGremlinTask);
        } else {
            return;
        }
    }


    private String[] getAllVisualStyleTitle() {
        return services.getVisualMappingManager()
                .getAllVisualStyles().stream()
                .map(VisualStyle::getTitle)
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
