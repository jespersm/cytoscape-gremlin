package com.github.jespersm.cytoscape.gremlin.internal.ui.connect;

import javax.swing.*;

import com.github.jespersm.cytoscape.gremlin.internal.client.ConnectionParameter;
import com.github.jespersm.cytoscape.gremlin.internal.client.GremlinClient;
import com.github.jespersm.cytoscape.gremlin.internal.ui.DialogMethods;

import java.awt.*;
import java.util.function.Predicate;

class ConnectDialog extends JDialog { //NOSONAR , hierarchy level > 5

    private static final String CANCEL_CMD = "cancel";
    private static final String OK_CMD = "ok";
    
    private JTextField usernameField = new JTextField("");
    private JTextField graphField = new JTextField("g");
    private JPasswordField password = new JPasswordField();
    private JTextField hostnameField = new JTextField("localhost");
    private JTextField portField = new JTextField("8182");
    private JCheckBox tlsCheck = new JCheckBox("Use TLS");
    private boolean ok = false;
    private final transient Predicate<ConnectionParameter> connectionCheck;

    ConnectDialog(JFrame jFrame, Predicate<ConnectionParameter> connectionCheck, String hostname, String username, Integer port) {
        super(jFrame);
        this.connectionCheck = connectionCheck;
        usernameField.setText(username);
        hostnameField.setText(hostname);
        portField.setText(port == null ? "" : port.toString());
    }

    void showConnectDialog() {
        init();
        DialogMethods.center(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(true);
        pack();
        setVisible(true);
    }

    boolean isOk() {
        return ok;
    }

    void setHostname(String hostname) {
        hostnameField.setText(hostname);
    }

    void setUsernameField(String username) {
        usernameField.setText(username);
    }

    private ConnectionParameter getParameters() {
        return new ConnectionParameter(
        		hostnameField.getText(), parsePort(), false, false, null, usernameField.getText(), password.getPassword());
    }

    private int parsePort() {
		try {
			// Lazy
			return Integer.parseInt(portField.getText());
		} catch (Exception e) {
			return 0;
		}
	}

	private void init() {

        setTitle("Connect to Gremlin Server");

        JPanel topPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));


        JButton okButton = new JButton("Connect");
        okButton.addActionListener(e -> ok());
        okButton.setActionCommand(OK_CMD);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(ae -> dispose());
        cancelButton.setActionCommand(CANCEL_CMD);

        password = new JPasswordField();

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        addLineAt(topPanel, 0, "Hostname", hostnameField);
        addLineAt(topPanel, 1, "Graph name", graphField);
        addLineAt(topPanel, 2, "Port", portField);
        addLineAt(topPanel, 3, "", tlsCheck);
        addLineAt(topPanel, 4, "User name", usernameField);
        addLineAt(topPanel, 5, "Password", password);

        this.add(topPanel);
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.getRootPane().setDefaultButton(okButton);

    }

	private GridBagConstraints addLineAt(JPanel topPanel, int y, String label, JComponent field) {
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        topPanel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        topPanel.add(field, gbc);

		return gbc;
	}

    private void ok() {
        if (connectionCheck.test(getParameters())) {
            ok = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Cannot connect to Gremlin");
        }
    }

    public static void main(String[] args) {
        ConnectDialog connectDialog =
                new ConnectDialog(null, new GremlinClient(null)::connect, "localhost", "", 8182);
        connectDialog.showConnectDialog();
    }

    public String getHostname() {
        return hostnameField.getText();
    }

    public String getUsername() {
        return usernameField.getText();
    }

	public int getPort() {
		return parsePort();
	}

}
