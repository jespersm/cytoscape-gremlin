package com.github.jespersm.cytoscape.gremlin.internal.ui;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.AbstractTask;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.TaskExecutor;
import com.github.jespersm.cytoscape.gremlin.internal.ui.connect.ConnectToGremlinServer;

public class TaskMenuAction extends AbstractCyAction {

    private static final String MENU_LOC = "Apps.Gremlin Queries";
    private final transient ConnectToGremlinServer connectToGremlin;
    private final transient TaskExecutor taskExecutor;
    private final transient Supplier<AbstractTask> taskSupplier;

    public static TaskMenuAction create(String menuTitle, Services services, Supplier<AbstractTask> taskSupplier) {
        return new TaskMenuAction(menuTitle, services, taskSupplier);
    }

    private TaskMenuAction(String menuTitle, Services services, Supplier<AbstractTask> taskSupplier) {
        super(menuTitle);
        this.taskSupplier = taskSupplier;
        this.taskExecutor = services.getTaskExecutor();
        this.connectToGremlin = ConnectToGremlinServer.create(services);
        setPreferredMenu(MENU_LOC);
        setEnabled(false);
        setMenuGravity(0.1f);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (connectToGremlin.openConnectDialogIfNotConnected()) {
            AbstractTask abstractTask = taskSupplier.get();
            taskExecutor.execute(abstractTask);
        }
    }
}
