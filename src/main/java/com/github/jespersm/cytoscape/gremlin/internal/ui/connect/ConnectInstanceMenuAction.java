package com.github.jespersm.cytoscape.gremlin.internal.ui.connect;

import org.cytoscape.application.swing.AbstractCyAction;

import com.github.jespersm.cytoscape.gremlin.internal.Services;

import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ConnectInstanceMenuAction extends AbstractCyAction {

    private static final String MENU_TITLE = "Connect to Gremlin Server";
    private static final String MENU_LOC = "Apps.Gremlin Queries";
    private final transient ConnectToGremlinServer connectToGremlin;

    public static ConnectInstanceMenuAction create(Services services) {
        return new ConnectInstanceMenuAction(ConnectToGremlinServer.create(services));
    }

    private ConnectInstanceMenuAction(ConnectToGremlinServer connectToGremlin) {
        super(MENU_TITLE);
        this.connectToGremlin = connectToGremlin;
        setPreferredMenu(MENU_LOC);
        setMenuGravity(0.0f);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        connectToGremlin.connect();
    }

}
