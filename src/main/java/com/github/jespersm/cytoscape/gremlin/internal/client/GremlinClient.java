package com.github.jespersm.cytoscape.gremlin.internal.client;


import static org.apache.tinkerpop.gremlin.driver.AuthProperties.Property.PASSWORD;
import static org.apache.tinkerpop.gremlin.driver.AuthProperties.Property.USERNAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.driver.AuthProperties;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Cluster.Builder;
import org.apache.tinkerpop.gremlin.driver.RequestOptions;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import com.github.jespersm.cytoscape.gremlin.internal.graph.Graph;

public class GremlinClient {
    private static final String HELLO = "hello";
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GremlinClient.class);

    
    private Cluster cluster;
    private GremlinGraphFactory gremlinGraphFactory = new GremlinGraphFactory();

	private String alias = "g";

/*
    final static Class<?> SERIALIZER = org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0.class;
    final static List<Class<? extends IoRegistry>> IO_REGISTRIES = Collections.singletonList(TinkerIoRegistryV1d0.class);
*/
    //final static List<IoRegistry> IO_REGISTRIES = Collections.singletonList(org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry.class);
    
    public CompletableFuture<Boolean> connectAsync(ConnectionParameter connectionParameter) {
    		close();
    	
        	Builder builder = Cluster.build()
        			.addContactPoint(connectionParameter.getHost())
        			.enableSsl(connectionParameter.isUseTls());
			if (connectionParameter.getPort() != 0) {
        		builder = builder.port(connectionParameter.getPort());
        	}
        	String userName = StringUtils.trimToNull(connectionParameter.getUsername());
			if (userName != null) {
        		builder = builder.authProperties(new AuthProperties().with(USERNAME, userName));
        		builder = builder.authProperties(new AuthProperties().with(PASSWORD, connectionParameter.getPasswordAsString()));
        	}
			
	        HashMap<String, Object> configMap = new HashMap<>();
	        configMap.put(org.apache.tinkerpop.gremlin.driver.ser.AbstractGryoMessageSerializerV3d0.TOKEN_SERIALIZE_RESULT_TO_STRING, "false");
	        configMap.put(org.apache.tinkerpop.gremlin.driver.ser.AbstractGryoMessageSerializerV3d0.TOKEN_IO_REGISTRIES, Collections.singletonList("org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry"));
			GryoMessageSerializerV3d0 serializer = new org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0();
			serializer.configure(configMap, new HashMap<>());
			builder = builder.serializer(serializer);
        	this.cluster = builder.create();
        	String graphName = connectionParameter.getGraphName();
			if (graphName != null && ! graphName.equals("g")) {
        		this.alias = graphName;
        	}
            return pingAsync();
    }

    public static String trimToNull(String str) {
    	if (str != null) {
    		String ts = str.trim();
    		return ts.isEmpty() ? null : ts;
    	} else {
    		return null;
    	}
    }

    public boolean connect(ConnectionParameter connectionParameter) {
		try {
			return connectAsync(connectionParameter).get();
		} catch (Exception e) {
			logger.warn("Can't connect to Gremlin at " + connectionParameter.getBoltUrl(), e);
            return false;
        }
    }
    
    public CompletableFuture<Boolean> pingAsync() {
    	if (! isConnected()) return CompletableFuture.completedFuture(false);
    	return withRemoteTraversal(g -> g.inject(HELLO).promise(g2 -> g2.next().equals(HELLO)));
    }
    
    public <T> CompletableFuture<T> withClient(Function<Client, CompletableFuture<T>> query) {
    	ensureConnected();
		Client client = this.cluster.connect().alias(this.alias);
		return executeWithClient(query, client);
	}

	private void ensureConnected() {
		if (! isConnected()) { 
    		throw new IllegalStateException("Not connected to cluster yet");
    	}
	}

	private <T> CompletableFuture<T> executeWithClient(Function<Client, CompletableFuture<T>> query, Client client) {
		boolean clientWillCloseInFuture = true;
		try {
			logger.debug("Obtained client " + client);
			CompletableFuture<T> resultFuture = query.apply(client);
			Objects.requireNonNull(resultFuture, "Client must return CompletableFuture");
			clientWillCloseInFuture = false;
			return resultFuture.whenComplete((result, error) -> {
				if (error != null) {
					logger.warn("Exception raised during processing of client connection", error);
				}
				logger.debug("Releasing client " + client);
				client.close();
			});
		} catch (RuntimeException e) {
			logger.warn("Exception raised during processing of client connection", e);
			throw e;
		} finally {
			if (clientWillCloseInFuture) {
				logger.debug("Releasing client (after error) " + client);
				client.close();
				// If completing normally, client will closed by the whenComplete block above
			}
		}
	}

	public boolean isConnected() {
        return cluster != null && ! (cluster.isClosed() || cluster.isClosing());
    }
    
    public List<Result> executeQuery(ScriptQuery query) throws GremlinClientException {
        try {
        	return executeQueryAsync(query).thenCompose(ResultSet::all).get();
        } catch (Exception e) {
            throw new GremlinClientException(e.getMessage(), e);
        }
    }

    public CompletableFuture<Graph> getGraphAsync(ScriptQuery query) {
    	return executeQueryAsync(query)
    			.thenApply(result -> result
    					.stream()
    					.map(gremlinGraphFactory::create)
    					.collect(Collectors.toList()))
    			.thenApply(Graph::createFrom);
    }

    public Graph getGraph(ScriptQuery query) throws GremlinClientException {
        try {
        	return getGraphAsync(query).get();
        } catch (Exception e) {
            throw new GremlinClientException(e.getMessage(), e);
        }
    }

    public void explainQuery(ScriptQuery query) throws GremlinClientException {
        try {
            // TODO: session.run(query.getExplainQuery(), query.getParams());
        } catch (Exception e) {
            throw new GremlinClientException(e.getMessage(), e);
        }
    }

    public void close() {
        if (isConnected()) {
            cluster.close();
            cluster = null;
        }
    }

	public CompletableFuture<ResultSet> executeQueryAsync(ScriptQuery theQuery) {
		RequestOptions.Builder builder = RequestOptions.build();
		if (trimToNull(this.alias) != null) {
			builder.addAlias("g", this.alias);
		}
		theQuery.getParams().forEach((k, v) -> builder.addParameter(k, v));
		return withClient(client -> client.submitAsync(theQuery.getQuery(), builder.create()));
	}

	public <T> CompletableFuture<T> withRemoteTraversal(Function<GraphTraversalSource, CompletableFuture<T>> callersQuery) {
    	ensureConnected();
		DriverRemoteConnection connection = DriverRemoteConnection.using(this.cluster, this.alias);
		GraphTraversalSource traversalSource = AnonymousTraversalSource.traversal().withRemote(connection);
		return callersQuery.apply(traversalSource);
	}

}
