package com.github.jespersm.cytoscape.gremlin.internal.tasks;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.github.jespersm.cytoscape.gremlin.internal.Services;

/**
 * This class executes a command in the cytoscape dialog task manager
 */
public class TaskExecutor {

    private final DialogTaskManager dialogTaskManager;

    public static TaskExecutor create(Services services) {
        return new TaskExecutor(services.getDialogTaskManager());
    }

    private TaskExecutor(DialogTaskManager dialogTaskManager) {
        this.dialogTaskManager = dialogTaskManager;
    }

    public void execute(Task task) {
        TaskIterator it = new TaskIterator(task);
        dialogTaskManager.execute(it);
    }
}
