/**
 *
 */
package com.github.jespersm.cytoscape.gremlin.internal.ui.expand;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractExpandNodesTask.Direction;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExpandNodesTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.PropertyNodesTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * @author sven
 */
public class PropertyNodesMenuAction implements NetworkTaskFactory {

    private static final long serialVersionUID = 1L;
    private final transient Services services;
    private Boolean onlySelected;
    private Direction direction;

    /**
     *
     */
    public PropertyNodesMenuAction(Services services, Boolean onlySelected) {
        this.services = services;
        this.onlySelected = onlySelected;
        this.direction = direction;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        if (this.isReady(network)) {
            return new TaskIterator(new PropertyNodesTask(services, network, this.onlySelected));
        } else {
            return null;
        }
    }

    @Override
    public boolean isReady(CyNetwork arg0) {
        return arg0 != null && arg0.getNodeCount() > 0;
    }

    public static PropertyNodesMenuAction create(Services services, Boolean onlySelected) {
        return new PropertyNodesMenuAction(services, onlySelected);
    }

}
