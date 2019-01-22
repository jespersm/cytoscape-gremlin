package com.github.jespersm.cytoscape.gremlin.internal.ui.importgraph.query;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.vizmap.VisualStyle;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractImportTask;
import com.github.jespersm.cytoscape.gremlin.internal.ui.DialogMethods;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.stream.Collectors;

public class ImportGremlinQueryMenuAction extends AbstractCyAction {

    private static final String MENU_TITLE = "Import Gremlin Query";
    private static final String MENU_LOC = "Apps.Gremlin Queries";

    private final transient Services services;

    public static ImportGremlinQueryMenuAction create(Services services) {
        return new ImportGremlinQueryMenuAction(services);
    }

    private ImportGremlinQueryMenuAction(Services services) {
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
        boolean finished = false;
        GremlinQueryDialog gremlinQueryDialog = new GremlinQueryDialog(services.getCySwingApplication().getJFrame(), getAllVisualStyleTitle());
        do {
            gremlinQueryDialog.showDialog();
            if (!gremlinQueryDialog.isExecuteQuery()) {
                finished = true;
                break;
            }
            String query = gremlinQueryDialog.getGremlinQuery();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(), "Query is empty");
                break;
            }
            ScriptQuery scriptQuery = ScriptQuery.builder().query(query).build();
            try {
                services.getGremlinClient().explainQuery(scriptQuery);
                AbstractImportTask executeImportTask =
                        services.getTaskFactory().createImportQueryTask(
                                gremlinQueryDialog.getNetwork(),
                                scriptQuery,
                                gremlinQueryDialog.getVisualStyleTitle()
                        );
                services.getTaskExecutor().execute(executeImportTask);
                finished = true;
            } catch (GremlinClientException e1) {
                JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(), e1.getMessage());
            }
            if (!finished) {
                gremlinQueryDialog = new GremlinQueryDialog(
                        services.getCySwingApplication().getJFrame(),
                        getAllVisualStyleTitle(),
                        gremlinQueryDialog.getGremlinQuery(),
                        gremlinQueryDialog.getNetwork()
                );
            }
        } while (!finished);
    }

    private String[] getAllVisualStyleTitle() {
        return services.getVisualMappingManager()
                .getAllVisualStyles().stream()
                .map(VisualStyle::getTitle)
                .sorted()
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
