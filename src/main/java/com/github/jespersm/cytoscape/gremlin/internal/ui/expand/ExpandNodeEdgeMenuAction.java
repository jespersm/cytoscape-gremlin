package com.github.jespersm.cytoscape.gremlin.internal.ui.expand;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExpandNodeViewTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.DefaultImportStrategy;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph.ImportGraphStrategy;


public class ExpandNodeEdgeMenuAction implements CyNodeViewContextMenuFactory {

    private final transient Services services;
    private ImportGraphStrategy importGraphStrategy;
    private JMenu menu;
    private CyNetworkView networkView;
    private View<CyNode> nodeView;
    private Direction direction;

    public ExpandNodeEdgeMenuAction(Services services) {
        super();
        this.importGraphStrategy = new DefaultImportStrategy();
        this.services = services;
    }

    public void addMenuItemsEdges(Edge result) {
        String menuTitle = this.direction == Direction.IN ? "<- " : " - ";
        menuTitle = menuTitle + result + (this.direction == Direction.OUT ? " ->" : " - ");
        JMenuItem menuItem = new JMenuItem(menuTitle);
        ExpandNodeViewTask expandNodeTask = new ExpandNodeViewTask(nodeView, networkView, this.services, true);
        expandNodeTask.setEdge("`" + result + "`");
        menuItem.addActionListener(expandNodeTask);
        this.menu.add(menuItem);
    }


    @Override
    public CyMenuItem createMenuItem(CyNetworkView networkView, View<CyNode> nodeView) {
        this.networkView = networkView;
        this.nodeView = nodeView;
        CyNode cyNode = (CyNode) nodeView.getModel();
        Long refid = networkView.getModel().getRow(cyNode).get(this.importGraphStrategy.getRefIDName(), Long.class);
        this.menu = new JMenu("Expand node on:");
/*
        this.direction = Direction.BIDIRECTIONAL;
        String query = "match (n)-[r]-() where ID(n) = " + refid + " return distinct type(r) as r";
        ScriptQuery scriptQuery = ScriptQuery.builder().query(query).build();

        this.services.getGremlinClient().executeQueryAsync(theQuery)
        
        ResultSet result = this.services.getGremlinClient().getResults(scriptQuery);
        result.forEach(this::addMenuItemsEdges);

        this.direction = Direction.IN;
        query = "match (n)<-[r]-() where ID(n) = " + refid + " return distinct type(r) as r";
        scriptQuery = ScriptQuery.builder().query(query).build();
        result = this.services.getGremlinClient().getResults(scriptQuery);
        result.forEach(this::addMenuItemsEdges);

        this.direction = Direction.OUT;
        query = "match (n)-[r]->() where ID(n) = " + refid + " return distinct type(r) as r";
        scriptQuery = ScriptQuery.builder().query(query).build();
        result = this.services.getGremlinClient().getResults(scriptQuery);
        result.forEach(this::addMenuItemsEdges);
*/
        CyMenuItem cyMenuItem = new CyMenuItem(this.menu, 0.5f);

        return cyMenuItem;
    }
}
