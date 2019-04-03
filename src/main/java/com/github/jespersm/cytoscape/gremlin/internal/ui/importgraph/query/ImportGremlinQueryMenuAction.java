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

        GremlinQueryDialog gremlinQueryDialog =
                new GremlinQueryDialog(services.getCySwingApplication().getJFrame(), getAllVisualStyleTitle());

        handler_loop:
        do {
            gremlinQueryDialog.showDialog();
            try {
                switch (gremlinQueryDialog.whichQueryType()) {
                    case CANCEL:
                        break handler_loop;

                    case EXECUTE: {
                        String query = gremlinQueryDialog.getGremlinQuery();
                        if (query.isEmpty()) {
                            JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(),
                                    "Query is empty");
                            break;
                        }
                        ScriptQuery scriptQuery = ScriptQuery.builder().query(query).build();
                        AbstractImportTask executeImportTask =
                                services.getTaskFactory().createImportQueryTask(
                                        gremlinQueryDialog.getNetwork(),
                                        scriptQuery,
                                        gremlinQueryDialog.getVisualStyleTitle()
                                );
                        services.getTaskExecutor().execute(executeImportTask);
                    } break handler_loop;

                    case EXPLAIN: {
                        String query = gremlinQueryDialog.getGremlinQuery();
                        if (query.isEmpty()) {
                            JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(),
                                    "Query is empty");
                            break;
                        }
                        ScriptQuery scriptQuery = ScriptQuery.builder().query(query).build();
                        String res = services.getGremlinClient().explainQuery(scriptQuery);
                        JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(),
                                res);
                    } break handler_loop;

                    default:
                        JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(),
                                String.format("Unknown query type %s", gremlinQueryDialog.whichQueryType()));

                }
            } catch (GremlinClientException e1) {
                JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(), e1.getMessage());
            }
            gremlinQueryDialog = new GremlinQueryDialog(
                    services.getCySwingApplication().getJFrame(),
                    getAllVisualStyleTitle(),
                    gremlinQueryDialog.getGremlinQuery(),
                    gremlinQueryDialog.getNetwork()
            );
        } while (true);
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
