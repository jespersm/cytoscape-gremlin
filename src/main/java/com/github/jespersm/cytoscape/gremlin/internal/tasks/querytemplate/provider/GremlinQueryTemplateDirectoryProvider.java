package com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.provider;

import org.apache.log4j.Logger;

import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.GremlinQueryTemplate;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.Reader;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.ReaderException;
import com.github.jespersm.cytoscape.gremlin.internal.tasks.querytemplate.template.Writer;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GremlinQueryTemplateDirectoryProvider {

    private static Logger logger = Logger.getLogger(GremlinQueryTemplateDirectoryProvider.class);

    private Map<Long, TemplateMapEntry> gremlinQueryTemplateMap = new HashMap<>();

    public static GremlinQueryTemplateDirectoryProvider create() {
        return new GremlinQueryTemplateDirectoryProvider();
    }

    private GremlinQueryTemplateDirectoryProvider() {
    }

    public Map<Long, GremlinQueryTemplate> getGremlinQueryTemplateMap() {
        return gremlinQueryTemplateMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().queryTemplate));
    }

    public Optional<GremlinQueryTemplate> getGremlinQueryTemplate(Long id) {
        if (gremlinQueryTemplateMap.containsKey(id)) {
            return Optional.of(gremlinQueryTemplateMap.get(id).queryTemplate);
        } else {
            return Optional.empty();
        }
    }

    public void readDirectory(Path templateDirectory) {
        this.gremlinQueryTemplateMap.clear();
        Reader reader = new Reader();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(templateDirectory, "*.xml")) {
            long id = 0;
            for (Path filePath : directoryStream) {
                GremlinQueryTemplate queryTemplate = parseTemplateQueryXml(reader, filePath);
                if (queryTemplate != null) {
                    this.gremlinQueryTemplateMap.put(id++, new TemplateMapEntry(queryTemplate, filePath));
                }
            }
        } catch (IOException e) {
            logger.warn("Cannot read file: " + e.getMessage());
        }
    }

    private GremlinQueryTemplate parseTemplateQueryXml(Reader reader, Path filePath) throws IOException {
        InputStream in = Files.newInputStream(filePath, StandardOpenOption.READ);
        try {
            return reader.read(in);
        } catch (ReaderException e) {
            logger.warn("Cannot parse query template file: " + filePath.toAbsolutePath().toString() + " : " + e.getMessage());
            return null;
        }
    }

    public long addGremlinQueryTemplate(Path path, GremlinQueryTemplate queryTemplate) throws IOException, JAXBException {

        Writer writer = new Writer();
        OutputStream outputStream = Files.newOutputStream(path);
        writer.write(queryTemplate, outputStream);
        outputStream.close();

        long id = 1 + gremlinQueryTemplateMap.keySet().stream().max(Long::compare).orElse(0l);
        gremlinQueryTemplateMap.put(id, new TemplateMapEntry(queryTemplate, path));
        return id;
    }

    public void putGremlinQueryTemplate(Long id, GremlinQueryTemplate queryTemplate) throws IOException, JAXBException {

        if (gremlinQueryTemplateMap.containsKey(id)) {
            Path path = gremlinQueryTemplateMap.get(id).filePath;
            Writer writer = new Writer();
            OutputStream outputStream = Files.newOutputStream(path);
            writer.write(queryTemplate, outputStream);
            outputStream.close();
            gremlinQueryTemplateMap.put(id, new TemplateMapEntry(queryTemplate, path));
        } else {
            throw new IllegalStateException();
        }
    }

    private class TemplateMapEntry {
        final GremlinQueryTemplate queryTemplate;
        final Path filePath;

        private TemplateMapEntry(GremlinQueryTemplate queryTemplate, Path filePath) {
            this.queryTemplate = queryTemplate;
            this.filePath = filePath;
        }
    }
}
