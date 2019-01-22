package com.github.jespersm.cytoscape.gremlin.internal.ui.importgraph.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.github.jespersm.cytoscape.gremlin.internal.ui.DialogMethods;

@SuppressWarnings("serial")
public class GremlinQueryDialog extends JDialog { //NOSONAR, hierarchy > 5

    private static final String INITIAL_QUERY = "g.V().has(label, 'god')";
    private String scriptQuery;
    private boolean executeQuery;
    private final String[] visualStyles;
    private String network;
    private String visualStyleTitle;
    private boolean explainQuery;

    public GremlinQueryDialog(Frame owner, String[] visualStyles) {
        super(owner);
        this.visualStyles = visualStyles;
        this.scriptQuery = INITIAL_QUERY;
        this.network = "Network";
    }

    public GremlinQueryDialog(Frame owner, String[] visualStyles, String scriptQuery, String network) {
        super(owner);
        this.visualStyles = visualStyles;
        this.scriptQuery = scriptQuery;
        this.network = network;
    }

    public void showDialog() {

        setTitle("Execute Gremlin Query");

        JEditorPane queryText = new JEditorPane();
        queryText.setText(scriptQuery);
        JScrollPane queryTextScrollPane = new JScrollPane(queryText);
        queryTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JComboBox visualStyleComboBox = new JComboBox(visualStyles);
        JLabel visualStyleLabel = new JLabel("Visual Style");
        JTextField networkNameField = new JTextField(network, 30);
        JLabel networkNameLabel = new JLabel("network");

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            executeQuery = false;
            GremlinQueryDialog.this.dispose();
        });
        JButton executButton = new JButton("Execute Query");
        executButton.addActionListener(e -> {
            executeQuery = true;
            scriptQuery = queryText.getText();
            network = networkNameField.getText();
            visualStyleTitle = (String) visualStyleComboBox.getSelectedItem();
            GremlinQueryDialog.this.dispose();
        });

        JButton explainButton = new JButton("Explain");
        executButton.addActionListener(e -> {
            explainQuery = true;
            scriptQuery = queryText.getText();
            network = networkNameField.getText();
            visualStyleTitle = (String) visualStyleComboBox.getSelectedItem();
            GremlinQueryDialog.this.dispose();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        topPanel.add(networkNameLabel);
        topPanel.add(networkNameField);
        topPanel.add(visualStyleLabel);
        topPanel.add(visualStyleComboBox);
        queryPanel.add(queryTextScrollPane, BorderLayout.CENTER);
        buttonPanel.add(cancelButton);
        //TODO: buttonPanel.add(explainButton);
        buttonPanel.add(executButton);

        add(topPanel, BorderLayout.NORTH);
        add(queryPanel);
        add(buttonPanel, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(400, 300));
        DialogMethods.center(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(true);
        pack();
        setVisible(true);
    }

    public String getGremlinQuery() {
        return scriptQuery;
    }

    public boolean isExecuteQuery() {
        return executeQuery;
    }

    public boolean isExplainQuery() {
        return explainQuery;
    }

    public String getNetwork() {
        return network;
    }

    public String getVisualStyleTitle() {
        return visualStyleTitle;
    }

    public static void main(String[] args) {
        GremlinQueryDialog dialog = new GremlinQueryDialog(null, new String[]{"v1", "v2"});
        dialog.showDialog();
    }
}
