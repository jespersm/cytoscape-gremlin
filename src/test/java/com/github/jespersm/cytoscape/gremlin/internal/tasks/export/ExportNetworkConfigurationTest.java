package com.github.jespersm.cytoscape.gremlin.internal.tasks.export;

import static com.github.jespersm.cytoscape.gremlin.internal.Constants.REF_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.jespersm.cytoscape.gremlin.internal.graph.implementation.NodeLabel;

@RunWith(MockitoJUnitRunner.class)
public class ExportNetworkConfigurationTest {

    @Mock
    private CyNetwork cyNetwork;

    @Mock
    private CyNode cyNode;

    @Mock
    private CyEdge cyEdge;

    @Mock
    private CyRow cyRow;

    @Mock
    private CyRow cyEdgeRow;

    @Before
    public void before() {
        when(cyNetwork.getRow(cyNode)).thenReturn(cyRow);
        when(cyNetwork.getRow(cyEdge)).thenReturn(cyEdgeRow);
    }

    @Test
    public void create() {
        List<String> labels = Arrays.asList("label1", "label2");
        when(cyRow.isSet("label")).thenReturn(true);
        when(cyRow.getRaw("label")).thenReturn(labels);
        when(cyRow.get("label", List.class)).thenReturn(labels);

        when(cyRow.isSet(REF_ID)).thenReturn(true);
        when(cyRow.getRaw(REF_ID)).thenReturn(1l);
        when(cyRow.get(REF_ID, Long.class)).thenReturn(1l);

        when(cyRow.get("shared name", String.class)).thenReturn("name");

        when(cyEdgeRow.isSet("shared name")).thenReturn(true);
        when(cyEdgeRow.getRaw("shared name")).thenReturn("link");
        when(cyEdgeRow.get("shared name", String.class)).thenReturn("link");

        ExportNetworkConfiguration exportNetworkConfiguration =
                ExportNetworkConfiguration.create(
                        NodeLabel.create("label"),
                        "shared name",
                        REF_ID,
                        "label",
                        "props"
                );
        assertEquals("label", exportNetworkConfiguration.getNodeLabel().getLabel());
        assertEquals("link", exportNetworkConfiguration.getRelationship(cyNetwork, cyEdge));
        assertEquals(REF_ID, exportNetworkConfiguration.getNodeReferenceIdColumn());
        assertEquals("props", exportNetworkConfiguration.getNodePropertiesColumnName());
        assertEquals(2, exportNetworkConfiguration.getNodeLabels(cyNode, cyNetwork).size());
        assertEquals("label1", exportNetworkConfiguration.getNodeLabels(cyNode, cyNetwork).get(0).getLabel());
        assertEquals(1l, exportNetworkConfiguration.getNodeReferenceId(cyNode, cyNetwork));
        assertEquals("name", exportNetworkConfiguration.getNodeName(cyNetwork, cyNode));

    }

    @Test
    public void REF_ID_SUID() {

        when(cyRow.isSet(REF_ID)).thenReturn(false);
        when(cyNode.getSUID()).thenReturn(1l);
        ExportNetworkConfiguration exportNetworkConfiguration = ExportNetworkConfiguration.create(NodeLabel.create("label"), "relationship", REF_ID, "label", "props");
        assertEquals(1l, exportNetworkConfiguration.getNodeReferenceId(cyNode, cyNetwork));
        verify(cyNode).getSUID();
    }

    @Test
    public void relationshipDefault() {

        when(cyEdgeRow.isSet("relationship")).thenReturn(false);
        ExportNetworkConfiguration exportNetworkConfiguration =
                ExportNetworkConfiguration.create(
                        NodeLabel.create("label"), "relationship", REF_ID, "label", "props");
        assertEquals("relationship", exportNetworkConfiguration.getRelationship(cyNetwork, cyEdge));
    }

}