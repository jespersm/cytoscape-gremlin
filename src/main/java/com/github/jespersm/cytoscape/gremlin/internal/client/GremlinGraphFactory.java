package com.github.jespersm.cytoscape.gremlin.internal.client;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jespersm.cytoscape.gremlin.internal.graph.*;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class GremlinGraphFactory {

    GraphObject create(Result result) {
    	Object value = result.getObject(); 
        if (value instanceof Vertex) {
            return create(result.getVertex());
        } else if (value instanceof Edge) {
            return create(result.getEdge());
        } else if (value instanceof List) {
            return create((List<Object>)value);
        } else if (value instanceof Map) {
            return create(((Map<String,Object>)value));
        } else {
            return new GraphSimple(value);
        }
    }

    private GraphObject create(Map<String,Object> elements) {
        return elements.entrySet().stream()
                .filter(e -> e.getValue() instanceof Element)
                .collect(GraphMap::new,
                        (map, el) -> map.add(el.getKey(), this.create((Element)el.getValue())),
                        (map1, map2) -> map1.merge(map2));
    }

    private GraphObject create(List<Object> objects) {
        return objects.stream()
                .filter(o -> o instanceof Element)
                .map(o -> this.create((Element) o))
                .collect(GraphList::new, (list, o) -> list.add(o), (list1, list2) -> list1.addAll(list2));
    }

    private GraphObject create(Element entity) {
        if (entity instanceof Edge) {
            return create((Edge) entity);
        } else if (entity instanceof Vertex) {
            return create((Vertex) entity);
        }
        throw new IllegalStateException();
    }

    private GraphEdge create(Edge relationship) {
        GraphEdge graphEdge = new GraphEdge();
        graphEdge.setStart(relationship.outVertex().id().toString());
        graphEdge.setEnd(relationship.inVertex().id().toString());
        graphEdge.setProperties(toMap(relationship));
        graphEdge.setType(relationship.label());
        graphEdge.setId(relationship.id().toString());
        return graphEdge;
    }

    private Map<String, Object> toMap(Element element) {
    	LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    	for (String key: element.keys()) {
    		map.put(key, element.value(key));
    	}
		return map;
	}

	private GraphNode create(Vertex node) {
        GraphNode graphNode = new GraphNode(node.id().toString());
        graphNode.setProperties(toMap(node));
        graphNode.setLabel(node.label());
        return graphNode;
    }
}
