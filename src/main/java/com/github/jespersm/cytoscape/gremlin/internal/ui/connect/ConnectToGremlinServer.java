package com.github.jespersm.cytoscape.gremlin.internal.ui.connect;

import org.cytoscape.application.swing.CySwingApplication;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClient;
import com.github.jespersm.cytoscape.gremlin.internal.configuration.AppConfiguration;

import javax.swing.*;

public class ConnectToGremlinServer {

    private final GremlinClient gremlinClient;
    private final CySwingApplication cySwingApplication;
    private final AppConfiguration appConfiguration;

    private ConnectToGremlinServer(GremlinClient gremlinClient, CySwingApplication cySwingApplication, AppConfiguration appConfiguration) {
        this.gremlinClient = gremlinClient;
        this.cySwingApplication = cySwingApplication;
        this.appConfiguration = appConfiguration;
    }

    public static ConnectToGremlinServer create(Services services) {
        return new ConnectToGremlinServer(
                services.getGremlinClient(),
                services.getCySwingApplication(),
                services.getAppConfiguration());
    }

    public boolean openConnectDialogIfNotConnected() {
        if (gremlinClient.isConnected()) {
            return true;
        }
        return connect();
    }

    public boolean connect() {
        ConnectDialog connectDialog = new ConnectDialog(cySwingApplication.getJFrame(), gremlinClient::connect,
                appConfiguration.getGremlinHost(),
                appConfiguration.getGremlinUsername(),
                appConfiguration.getPort()
        );
        connectDialog.showConnectDialog();
        if (connectDialog.isOk()) {
            appConfiguration.setConnectionParameters(connectDialog.getHostname(), connectDialog.getUsername(), connectDialog.getPort());
            appConfiguration.save();
        } else {
            JOptionPane.showMessageDialog(this.cySwingApplication.getJFrame(), "Not Connected");
        }
        return gremlinClient.isConnected();
    }
}
