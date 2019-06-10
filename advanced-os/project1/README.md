# Node.java
## Main:

- `config()`

    1. Get the Hostname:
        - Call `InetAddress.getHostName()`
        - Returns "dcXX.utdallas.edu"
        - Set `hostname = dcXX`
    2. Read the config file and parse:
        - Set global params
        - for each `node` definition:
            - `if (node.hostName == hostname)` then init `this` node with `nodeId`, `hostName`, and `listenPort`
        - for each node's `neighbors`:
            - ` if current node (aka lineNumber) == this.nodeId` then set `node.neighbors`

- `this.run()`
    
    1. Open a `ServerSocket` on this node's `listenPort`
    2. for each of this node's `neighbor`s:
        - Open a `Socket` to `neighbor.hostName` on port `neighbor.listenPort`
        - If fail (ie: neighbor isn't up yet) then retry.
    
    ...
