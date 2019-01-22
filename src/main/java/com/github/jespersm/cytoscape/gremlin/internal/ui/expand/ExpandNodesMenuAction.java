/**
 *
 */
package com.github.jespersm.cytoscape.gremlin.internal.ui.expand;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExpandNodesTask;

/**
 * @author sven
 */
public class ExpandNodesMenuAction implements NetworkTaskFactory {

    private static final long serialVersionUID = 1L;
    private final transient Services services;
    private Boolean onlySelected;
    private Direction direction;

    /**
     *
     */
    public ExpandNodesMenuAction(Services services, Boolean onlySelected, Direction direction) {
        this.services = services;
        this.onlySelected = onlySelected;
        this.direction = direction;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        if (this.isReady(network)) {
            return new TaskIterator(new ExpandNodesTask(services, network, this.onlySelected, this.direction));
        } else {
            return null;
        }
    }

    @Override
    public boolean isReady(CyNetwork arg0) {
        return arg0 != null && arg0.getNodeCount() > 0;
    }

    public static ExpandNodesMenuAction create(Services services, Boolean onlySelected, Direction direction) {
        return new ExpandNodesMenuAction(services, onlySelected, direction);
    }

}
