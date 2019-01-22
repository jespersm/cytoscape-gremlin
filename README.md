# Cytoscape plugin for Gremlin

Gremlin Graphs are often too large for Cytoscape: this plugin allows you to write Gremlin queries and import the result as a network. 
Queries can be parameterized and stored for reuse.  

Eventually, the plugin can be downloaded from the [Cytoscape App Store](http://apps.cytoscape.org/apps/cytoscapegremlin)

## Features
Connects to Gremlin with a username/password using the Gremlin Server interface, either as WebSocket or HTTP(s).

### Importing graphs
There are three main methods of importing a graph:
- Import a Gremlin query into Cytoscape

### Expanding nodes
The plugin allows you to expand a single node, selected nodes or all nodes in the network at once. This way you can browse through your graph.

Main menu:
- Expand all (selected) nodes in the network through all edges (bidirectional)
- Expand all (selected) nodes, incoming edges only
- Expand all (selected) nodes, outgoing edges only

Context menu (not yet implemented):
- Expand single node, bidirectional, incoming or outgoing edges
- Expand single node, bidirectional, incoming or outgoing edges, based on the _available edges connected to this node_
- Expand single node, bidirectional, incoming or outgoing edges, based on the _available nodes connected to this node_

### Other features
- Show all edges (relationships) between all nodes in the network or only between selected nodes.
- (Not yet implemented:) Get the shortest paths from the database between the selected nodes. When more than two nodes are selected, all combinations will be queried: Gremlin does not allow shortest path calculations between more than two nodes (a.k.a. 'via').
