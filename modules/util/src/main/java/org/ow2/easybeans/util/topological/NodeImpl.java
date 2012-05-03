package org.ow2.easybeans.util.topological;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic Implementation of the node interface.
 * @author Florent Benoit
 */
public class NodeImpl implements Node {

    /**
     * Name of the node.
     */
    private String name = null;

    /**
     * Dependencies for this node.
     */
    private List<Node> dependencies = null;


    /**
     * Build a node with the given name.
     * @param name the name of the node
     */
    public NodeImpl(final String name) {
        this.name = name;
        this.dependencies = new ArrayList<Node>();
    }


    /**
     * @return name of the node
     */
    public String getName() {
        return this.name;
    }

    /**
     * Add a dependency on another node.
     * @param node the node on which we're requiring a dependency
     */
    public void addDependency(final Node node) {
        this.dependencies.add(node);
    }

    /**
     * @return list of dependencies.
     */
    public List<Node> getDependencies() {
        return this.dependencies;
    }


    /**
     * @return string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Node[");
        sb.append(this.name);
        sb.append("]");
        return sb.toString();
    }
}
