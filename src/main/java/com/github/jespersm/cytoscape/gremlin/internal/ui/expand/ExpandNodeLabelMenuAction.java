package com.github.jespersm.cytoscape.gremlin.internal.ui.expand;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClientException;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExpandNodeViewTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;

public class ExpandNodeLabelMenuAction implements CyNodeViewContextMenuFactory {

    private DefaultImportStrategy importGraphStrategy;
    private Services services;
    private CyNetworkView networkView;
    private View<CyNode> nodeView;
    private JMenu menu;
    private Direction direction;

    public ExpandNodeLabelMenuAction(Services services) {
        super();
        this.importGraphStrategy = new DefaultImportStrategy();
        this.services = services;
    }

    public void addMenuItemsNodes(Result record) {
        Vertex result = record.getVertex();
        ArrayList<String> nodeLabels = new ArrayList<String>();
//        result.asList().forEach(v -> nodeLabels.add("`" + (String) v + "`"));
        String nodeLabel = String.join(":", nodeLabels);
        String menuTitle = this.direction == Direction.IN ? "<- " : " - ";
        menuTitle = menuTitle + nodeLabel.replace("`", "") + (this.direction == Direction.OUT ? " ->" : " - ");

        JMenuItem menuItem = new JMenuItem(menuTitle);

        ExpandNodeViewTask expandNodeTask = new ExpandNodeViewTask(nodeView, networkView, this.services, true);
        expandNodeTask.setNode(nodeLabel);
        menuItem.addActionListener(expandNodeTask);
        this.menu.add(menuItem);

    }


    @Override
    public CyMenuItem createMenuItem(CyNetworkView networkView, View<CyNode> nodeView) {
        this.networkView = networkView;
        this.nodeView = nodeView;
        CyNode cyNode = (CyNode) nodeView.getModel();
        try {
            this.menu = new JMenu("Expand node to:");

            String refid = networkView.getModel().getRow(cyNode).get(this.importGraphStrategy.getRefIDName(), String.class);

            this.direction = Direction.BIDIRECTIONAL;
            String query = "match (n)-[]-(r) where ID(n) = " + refid + " return distinct labels(r) as r";
            ScriptQuery scriptQuery = ScriptQuery.builder().query(query).build();
            List<Result> result = this.services.getGremlinClient().executeQuery(scriptQuery);
            result.forEach(this::addMenuItemsNodes);

            direction = Direction.IN;
            query = "match (n)<-[]-(r) where ID(n) = " + refid + " return distinct labels(r) as r";
            scriptQuery = ScriptQuery.builder().query(query).build();
            result = this.services.getGremlinClient().executeQuery(scriptQuery);
            result.forEach(this::addMenuItemsNodes);

            this.direction = Direction.OUT;
            query = "match (n)-[]->(r) where ID(n) = " + refid + " return distinct labels(r) as r";
            scriptQuery = ScriptQuery.builder().query(query).build();
            result = this.services.getGremlinClient().executeQuery(scriptQuery);
            result.forEach(this::addMenuItemsNodes);


            CyMenuItem cyMenuItem = new CyMenuItem(this.menu, 0.5f);

            return cyMenuItem;

        } catch (GremlinClientException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return null;
    }

}
