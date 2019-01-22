package com.github.jespersm.cytoscape.gremlin.internal.client;

public class ConnectionParameter {
    private final String host;
    private final int port;
    private final boolean useWebSocket;
    private final String graphName;
    private final String username;
    private final char[] password;
	private final boolean useTls;

    public ConnectionParameter(String host, int port, boolean useTls, boolean useWebSocket, String graphName, String username, char[] password) {
		super();
		this.host = host;
		this.port = port;
		this.useTls = useTls;
		this.useWebSocket = useWebSocket;
		this.graphName = graphName;
		this.username = username;
		this.password = password;
	}

	String getBoltUrl() {
        return (useWebSocket ? "ws" : "http") + 
        		(useTls ? "s" : "") +
        		"://" + host +
        		((port != 0) ? (":" + port) : "") +
        		"/gremlin";
    }

    String getUsername() {
        return username;
    }

    String getPasswordAsString() {
        return new String(password);
    }
    
    public String getGraphName() {
		return graphName;
	}
    
    public String getHost() {
		return host;
	}
    
    public char[] getPassword() {
		return password;
	}
    
    public int getPort() {
		return port;
	}

	public boolean isUseTls() {
		return useTls;
	}
    
}
