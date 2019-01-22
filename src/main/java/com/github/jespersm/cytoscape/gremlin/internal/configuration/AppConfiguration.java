package com.github.jespersm.cytoscape.gremlin.internal.configuration;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * This clas stores data that is used by the plugin.
 * The settings are stored in a property file the temporary directory (java.io.tmpdir).
 */
public class AppConfiguration {

    private static final Logger LOG = Logger.getLogger(AppConfiguration.class);
    private static final String TEMPLATEDIR = "templatedir";
    private static final String GREMLIN_HOST = "gremlin.host";
    private static final String GREMLIN_USERNAME = "gremlin.username";
    private static final String GREMLIN_PORT = "gremlin.port";
    private static final String GREMLIN_QUERY = "gremlin.query";
    private Properties properties = new Properties();

    public String getTemplateDirectory() {
        return properties.getProperty(TEMPLATEDIR);
    }

    public String getGremlinHost() {
        return properties.getProperty(GREMLIN_HOST);
    }

    public String getGremlinUsername() {
        return properties.getProperty(GREMLIN_USERNAME);
    }

    public String getGremlinQuery() {
        return properties.getProperty(GREMLIN_QUERY);
    }

    public int getPort() {
    	try {
    		return Integer.parseInt(properties.getProperty(GREMLIN_PORT));
    	} catch (Exception e) {
    		return 0;
    	}
    }

    public void setTemplateDirectory(String templateDir) {
        properties.setProperty(TEMPLATEDIR, templateDir);
        save();
    }

    public void load() {
        Path configurationPath = getConfigurationFile();
        if (configurationPath.toFile().exists()) {
            try {
                properties.load(Files.newInputStream(configurationPath, StandardOpenOption.READ));
            } catch (IOException e) {
                LOG.warn("Error reading configuration");
            }
        } else {
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty(GREMLIN_QUERY, "g.V().union(__.identity(),__.outE())");
        properties.setProperty(GREMLIN_HOST, "localhost");
        properties.setProperty(GREMLIN_USERNAME, "");
        properties.setProperty(GREMLIN_PORT , "8182");
        properties.setProperty(TEMPLATEDIR, "");
    }

    public void save() {
        Path configurationFile = getConfigurationFile();
        try {
            properties.store(Files.newOutputStream(configurationFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING), "saved app config: " + LocalDateTime.now());
        } catch (IOException e) {
            LOG.warn("Error writing config file");
        }
    }

    private Path getConfigurationFile() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir, "cytoscape-gremlin.properties");
    }

    public void setConnectionParameters(String hostname, String username, Integer port) {
        properties.setProperty(GREMLIN_HOST, hostname);
        properties.setProperty(GREMLIN_USERNAME, username);
        properties.setProperty(GREMLIN_PORT, port == null ? "" : port.toString());
    }
}
