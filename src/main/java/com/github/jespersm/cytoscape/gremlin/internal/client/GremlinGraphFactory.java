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

public class GremlinGraphFactory implements AbstractGremlinGraphFactory {

    /**
     * This is a function class.
     * It has only one non-static public method declared in its interface.
     *
     * @param result
     * @return
     */

    public GraphObject create(Result result) {
    	Object value = result.getObject(); 
        if (value instanceof Vertex) {
            return AbstractGremlinGraphFactory.create(result.getVertex());
        } else if (value instanceof Edge) {
            return AbstractGremlinGraphFactory.create(result.getEdge());
        } else if (value instanceof List) {
            return AbstractGremlinGraphFactory.create((List<Object>)value);
        } else if (value instanceof Map) {
            return AbstractGremlinGraphFactory.create(((Map<String,Object>)value));
        } else {
            return new GraphSimple(value);
        }
    }

}
