package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.xml;


import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class Parameters {

    @XmlElement(name = "parameter")
    private List<Parameter> parameterList = new ArrayList<>();

    public List<Parameter> getParameterList() {
        return parameterList;
    }
}
