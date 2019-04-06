package com.github.jespersm.cytoscape.gremlin.internal.client;


import com.github.jespersm.cytoscape.gremlin.internal.graph.*;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AbstractGremlinGraphFactory {

    /**
     * This and interface for a function class.
     * It has only one non-static public method.
     *
     * @param result
     * @return
     */

    GraphObject create(Result result);


    static GraphObject create(Map<String,Object> elements) {
        return elements.entrySet().stream()
                .collect(GraphMap::new,
                        (map, el) ->
                                map.add(el.getKey().toString(),
                                        create((Element)el.getValue())),
                        (map1, map2) -> map1.merge(map2));
    }

    static GraphObject create(List<Object> objects) {
        return objects.stream()
                .filter(o -> o instanceof Element)
                .map(o -> create((Element) o))
                .collect(GraphList::new,
                        (list, o) -> list.add(o),
                        (list1, list2) -> list1.addAll(list2));
    }

    static GraphObject create(Element entity) {
        if (entity instanceof Edge) {
            return create((Edge) entity);
        } else if (entity instanceof Vertex) {
            return create((Vertex) entity);
        }
        throw new IllegalStateException();
    }

    /**
     * When the properties in the result set need to be normalized.
     * @param element
     * @return
     */
    static Map<String, Object> toMap(Element element) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (String key: element.keys()) {
            map.put(key, element.value(key));
        }
        return map;
    }

    /**
     * In some cases the remote does not return with a
     * node provisioned with it properties.
     * In other cases it does.
     *
     * @param relationship
     * @return
     */
    static GraphEdge create(Edge relationship) {
        return create(relationship, toMap(relationship));
    }

    static GraphEdge create(Edge relationship, Map<String, Object> properties) {
        GraphEdge graphEdge = new GraphEdge();
        graphEdge.setStart(relationship.outVertex().id().toString());
        graphEdge.setEnd(relationship.inVertex().id().toString());
        graphEdge.setProperties(properties);
        graphEdge.setType(relationship.label());
        graphEdge.setId(relationship.id().toString());
        return graphEdge;
    }

    /**
     * In some cases the remote does not return with a
     * node provisioned with it properties.
     * In other cases it does.
     *
     * @param node
     * @return
     */

    static GraphNode create(Vertex node) {
        return create(node, toMap(node));
    }

    static GraphNode create(Vertex node, Map<String, Object> properties) {
        GraphNode graphNode = new GraphNode(node.id().toString());
        graphNode.setProperties(properties);
        graphNode.setLabel(node.label());
        return graphNode;
    }
}
