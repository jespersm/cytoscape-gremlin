/**
 *
 */
package com.github.jespersm.cytoscape.gremlin.internal.ui.expand;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExpandNodeViewTask;


/**
 * @author sven
 */
public class ExpandNodeMenuAction extends AbstractNodeViewTaskFactory {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final transient Services services;
    private Boolean redoLayout;
    private Direction direction;

    public static ExpandNodeMenuAction create(Services services, Boolean redoLayout, Direction direction) {
        return new ExpandNodeMenuAction(services, redoLayout, direction);
    }

    private ExpandNodeMenuAction(Services services, Boolean redoLayout, Direction direction) {
        this.services = services;
        this.redoLayout = redoLayout;
        this.direction = direction;
    }

    @Override
    public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
        if (this.isReady(nodeView, networkView)) {
            ExpandNodeViewTask expandNodeTask = new ExpandNodeViewTask(nodeView, networkView, this.services, this.redoLayout);
            expandNodeTask.setDirection(this.direction);
            return new TaskIterator(expandNodeTask);

        } else {
            return null;
        }
    }


}
