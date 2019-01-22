package com.github.jespersm.cytoscape.gremlin.internal.ui;

import javax.swing.*;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.ui.connect.ConnectToGremlinServer;

import java.awt.*;

public class DialogMethods {

    private DialogMethods() {
    }

    public static void centerAndShow(JDialog jDialog) {
        center(jDialog);
        jDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jDialog.setModal(true);
        jDialog.setResizable(true);
        jDialog.pack();
        jDialog.setVisible(true);
    }

    public static void center(JDialog jDialog) {
        Point cyLocation = jDialog.getParent().getLocation();
        int height = jDialog.getParent().getHeight();
        int width = jDialog.getParent().getWidth();
        jDialog.setLocation(new Point(cyLocation.x + (width / 4), cyLocation.y + (height / 4)));
    }

    public static boolean connect(Services services) {
        ConnectToGremlinServer connectToGremlin = ConnectToGremlinServer.create(services);
        if (!connectToGremlin.openConnectDialogIfNotConnected()) {
            JOptionPane.showMessageDialog(services.getCySwingApplication().getJFrame(), "Not connected");
            return false;
        }
        return true;

    }
}
