package com.github.jespersm.cytoscape.gremlin.internal;

import com.github.jespersm.cytoscape.gremlin.internal.Services;
import com.github.jespersm.cytoscape.gremlin.internal.client.ScriptQuery;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.AbstractImportTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ExportNetworkToGremlinTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.ImportQueryTemplateTask;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.TaskFactory;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.export.ExportNetworkConfiguration;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.GremlinQueryTemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class TaskFactoryTest {

    @Mock
    private Services services;

    @Test
    public void create() {
        TaskFactory taskFactory = TaskFactory.create(services);
        assertNotNull("create command factory should not return null", taskFactory);
    }

    @Test
    public void createImportGraphTask() {
        TaskFactory taskFactory = TaskFactory.create(services);
        AbstractImportTask task = taskFactory.createImportAllNodesAndEdgesFromGremlinTask("Network", "default");
        assertNotNull("create import graph should not return null", task);
    }

    @Test
    public void createExportNetworkToGremlinTask() {
        TaskFactory taskFactory = TaskFactory.create(services);
        ExportNetworkConfiguration exportNetworkConfiguration = mock(ExportNetworkConfiguration.class);
        ExportNetworkToGremlinTask task = taskFactory.createExportNetworkToGremlinTask(exportNetworkConfiguration);
        assertNotNull("create export network to Gremlin should not return null", task);
    }

    @Test
    public void createRetrieveDataFromQueryTemplateTask() {
        TaskFactory taskFactory = TaskFactory.create(services);
        GremlinQueryTemplate query = mock(GremlinQueryTemplate.class);
        ImportQueryTemplateTask task = taskFactory.createImportQueryTemplateTask("Networkname", query, "visualStyle");
        assertNotNull("create retrieve data from query-template should not return null", task);
    }

    @Test
    public void createExecuteGremlkinQueryTask() {
        TaskFactory taskFactory = TaskFactory.create(services);
        ScriptQuery query = mock(ScriptQuery.class);
        AbstractImportTask task = taskFactory.createImportQueryTask("Networkname", query, "visualStyle");
        assertNotNull("create execute gremlin-query should not return null", task);
    }

}