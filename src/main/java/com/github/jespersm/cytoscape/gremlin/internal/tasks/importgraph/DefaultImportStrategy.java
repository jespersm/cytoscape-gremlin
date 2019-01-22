package com.github.jespersm.cytoscape.gremlin.internal.tasks.importgraph;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphEdge;
import com.github.jespersm.cytoscape.gremlin.internal.graph.GraphNode;

import static com.github.jespersm.cytoscape.gremlin.internal.Constants.REF_ID;
import static com.github.jespersm.cytoscape.gremlin.internal.Constants.CYCOLUMN_GREMLIN_LABEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This class implements an import strategy that copies all labels and properties to cytoscape
 */
/**
 * @param cyTable
 * @param cyId
 * @param key
 * @param value
 */
public class DefaultImportStrategy implements ImportGraphStrategy {


    private Map<Object, Object> idMap = new HashMap<>();
    private Map<Object, GraphNode> nodeMap = new HashMap<>();
    private List<GraphEdge> edges = new ArrayList<>();

    @Override
    public void postProcess(CyNetwork network) {
        nodeMap.values().stream().forEach(graphNode -> saveNode(network, graphNode));
        edges.forEach(graphEdge -> saveEdge(network, graphEdge));
    }


    @Override
    public void createTables(CyNetwork network) {
        CyTable defNodeTab = network.getDefaultNodeTable();
        if (defNodeTab.getColumn(REF_ID) == null) {
            defNodeTab.createColumn(REF_ID, String.class, false);
        }
        if (defNodeTab.getColumn(CYCOLUMN_GREMLIN_LABEL) == null) {
            defNodeTab.createColumn(CYCOLUMN_GREMLIN_LABEL, String.class, false);
        }

        CyTable defEdgeTab = network.getDefaultEdgeTable();
        if (defEdgeTab.getColumn(REF_ID) == null) {
            defEdgeTab.createColumn(REF_ID, String.class, false);
        }
        if (defEdgeTab.getColumn(CYCOLUMN_GREMLIN_LABEL) == null) {
            defEdgeTab.createColumn(CYCOLUMN_GREMLIN_LABEL, String.class, false);
        }
    }

    public String getRefIDName() {
        return REF_ID;
    }

    public void copyNode(CyNetwork network, GraphNode graphNode) {
        String nodeId = graphNode.getProperties().getOrDefault(REF_ID, graphNode.getId()).toString();
        idMap.put(graphNode.getId(), nodeId);
        nodeMap.putIfAbsent(nodeId, graphNode);
    }

    public void copyEdge(CyNetwork network, GraphEdge graphEdge) {
        edges.add(graphEdge);
    }

    private String createJSONArray(List<Object> values) {
        ArrayList<String> quoted = new ArrayList<String>();
        values.forEach(v -> {
            if (!(v instanceof String)) {
                quoted.add((String) v);
            } else {
                quoted.add("\"" + v + "\"");
            }
        });
        return "[" + String.join(",", quoted) + "]";

    }

    private void saveNode(CyNetwork network, GraphNode graphNode) {

        String nodeId = graphNode.getProperties().getOrDefault(REF_ID, graphNode.getId()).toString();
        if (nodeExists(network, nodeId)) {
            return;
        }

        CyTable cyTable = network.getDefaultNodeTable();
        CyNode cyNode = getOrCreateNode(network, nodeId);

        graphNode.getProperties().entrySet().stream()
                .filter(this::isCyColumn)
                .forEach(mapToColumn(cyTable, cyNode));

        createListColumn(cyTable, CYCOLUMN_GREMLIN_LABEL, String.class);
        setEntry(cyTable, cyNode, CYCOLUMN_GREMLIN_LABEL, graphNode.getLabel());
    }

    private Consumer<Map.Entry<String, Object>> mapToColumn(CyTable cyTable, CyNode cyNode) {
        return entry -> {
            if (entry.getValue() instanceof List) {
                createListColumn(cyTable, entry.getKey(), String.class);
                setEntry(cyTable, cyNode, entry.getKey(), entry.getValue());
            } else {
            	Object value = safeValue(entry.getValue());
                createColumn(cyTable, entry.getKey(), value.getClass());
                setEntry(cyTable, cyNode, entry.getKey(), value);
            }
        };
    }

    private boolean isCyColumn(Map.Entry<String, Object> entry) {
        return !(
                CYCOLUMN_GREMLIN_LABEL.equals(entry.getKey())
        );
    }

    private boolean nodeExists(CyNetwork network, String nodeId) {
        return !network.getDefaultNodeTable().getMatchingRows(REF_ID, nodeId).isEmpty();
    }

    private void saveEdge(CyNetwork network, GraphEdge graphEdge) {
        String edgeId = graphEdge.getProperties().getOrDefault(REF_ID, graphEdge.getId()).toString();
        if (edgeExists(network, edgeId)) {
            return;
        }
        CyTable cyTable = network.getDefaultEdgeTable();

        Object start = idMap.getOrDefault(graphEdge.getStart(), graphEdge.getStart());
        Object end = idMap.getOrDefault(graphEdge.getEnd(), graphEdge.getEnd());
        String label = graphEdge.getLabel();

        CyNode startNode = getOrCreateNode(network, start);
        CyNode endNode = getOrCreateNode(network, end);

        CyEdge cyEdge = network.addEdge(startNode, endNode, true);

        network.getRow(cyEdge).set(REF_ID, edgeId);
        network.getRow(cyEdge).set(CYCOLUMN_GREMLIN_LABEL, label);
        network.getRow(cyEdge).set(CyEdge.INTERACTION, label);

        for (Map.Entry<String, Object> entry : graphEdge.getProperties().entrySet()) {
            if (entry.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                String value = createJSONArray((List<Object>) entry.getValue());
                createColumn(cyTable, entry.getKey(), value.getClass());
                setEntry(cyTable, cyEdge, entry.getKey(), value);
            } else {
            	Object value = safeValue(entry.getValue());
                createColumn(cyTable, entry.getKey(), value.getClass());
                setEntry(cyTable, cyEdge, entry.getKey(), value);
            }
        }
    }

    /**
     * Return value which can be stored in Cytoscape
     * 
     * @param o Value from graph
     * @return Integer, Boolean, Double, String or null
     */
    private Object safeValue(Object o) {
		if (o instanceof String)
			return o;
		else if (o instanceof Integer)
			return o;
		else if (o instanceof Boolean)
			return o;
		else if (o instanceof Double)
			return o;
		else if (o instanceof Long)
			return o;
		else
			return Objects.toString(o, "<null>");
    }
    
    private boolean edgeExists(CyNetwork network, Object id) {
        return !network.getDefaultEdgeTable().getMatchingRows(REF_ID, id).isEmpty();
    }

    private CyNode getOrCreateNode(CyNetwork network, Object id) {
        CyColumn primaryKey = network.getDefaultNodeTable().getPrimaryKey();
        return network.getDefaultNodeTable()
                .getMatchingRows(REF_ID, id)
                .stream()
                .findFirst()
                .map(cyRow -> network.getNode(cyRow.get(primaryKey.getName(), Long.class)))
                .orElseGet(() -> this.createNode(network, id));
    }

    private CyNode createNode(CyNetwork network, Object id) {
        CyNode cyNode = network.addNode();
        setEntry(network.getDefaultNodeTable(), cyNode, REF_ID, id);
        return cyNode;
    }

    private void createColumn(CyTable cyTable, String key, Class<?> type) {
        if (cyTable.getColumn(key) == null) {
            cyTable.createColumn(key, type, true);
        }
    }

    private void createListColumn(CyTable cyTable, String key, Class<?> type) {
        if (cyTable.getColumn(key) == null) {
            cyTable.createListColumn(key, type, true);
        }
    }


    private void setEntry(CyTable cyTable, CyIdentifiable cyId, String key, Object value) {
        CyColumn cyColumn = cyTable.getColumn(key);
        if (cyColumn != null && cyColumn.getType().isInstance(value)) {
            cyTable.getRow(cyId.getSUID()).set(key, value);
        }
    }
}
