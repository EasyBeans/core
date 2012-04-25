/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
 * Contact: easybeans@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.topological;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Implementation of the topological sort.
 * @param <T> the interface extending Node
 * @author Florent Benoit
 */
public class TopologicalSort<T extends Node> {

    /**
     * Apply a topological sort on the given set of nodes.
     * @param nodes the nodes to sort
     * @param <T> the interface extending Node
     * @return a sorted list of the nodes.
     */
    public static <T extends Node> List<T> sort(final Collection<T> nodes) {
        TopologicalSort<T> topologicalSort = new TopologicalSort<T>(nodes);
        topologicalSort.sort();
        return topologicalSort.getSortedList();
    }

    /**
     * Nodes that we will sort.
     */
    private Collection<T> nodes = null;


    /**
     * Sorted List.
     */
    private List<T> sortedList = null;


    /**
     * Global visited List of nodes.
     */
    private Set<T> visitedNodes = null;

    /**
     * Sort has been made.
     */
    private boolean sorted = false;


    /**
     * Constructor with the given set of nodes to sort.
     * @param nodes the list of nodes on which topological sort will be applied
     */
    protected TopologicalSort(final Collection<T> nodes) {
        this.nodes = nodes;
        this.sortedList = new ArrayList<T>();
        this.visitedNodes = new HashSet<T>();
    }


    /**
     * Sort the current nodes.
     */
    protected void sort() {
        if (this.sorted) {
            return;
        }
        for (T node : this.nodes) {
            // Visit a node and empty stack
            visit(node, new Stack<T>());
        }
        // It's now sorted
        this.sorted = true;
    }


    /**
     * @return a sorted list
     */
    protected List<T> getSortedList() {
        if (!this.sorted) {
            throw new IllegalStateException("Unable to get a sorted list as sort() has not been called");
        }
        return this.sortedList;
    }

    /**
     * Here is the algorithm for the topological sort.
     * L <-- Empty list that will contain the sorted nodes <br/>
     * S <-- Set of all nodes with no outgoing edges<br/>
     * for each node n in S do<br/>
     *     visit(n) <br/>
     * <br/>
     * function visit(node n)<br/>
     *     if n has not been visited yet then<br/>
     *         mark n as visited<br/>
     *         for each node m with an edge from m to n do<br/>
     *             visit(m)<br/>
     *         add n to L<br/>
     *
     *
     * @param analyzingNode node that is being analyzed
     * @param stackVisitedNodes the local stack of visited nodes to detect cycles
     */
    public void visit(final T analyzingNode, final Stack<T> stackVisitedNodes) {

        // Cycle detected
        if (stackVisitedNodes.contains(analyzingNode)) {
            StringBuilder sb = new StringBuilder();
            sb.append("There is a cycle error for the node '");
            sb.append(analyzingNode.getName());
            sb.append("' and cycle is made between the nodes '");
            sb.append(stackVisitedNodes);
            sb.append("'. Dependencies are : \n");
            for (Node stackNode : stackVisitedNodes) {
                sb.append(" ( ");
                sb.append(stackNode.getName());
                sb.append("--->");
                sb.append(stackNode.getDependencies());
                sb.append(" )");
            }
            throw new GraphCycleException(sb.toString());
        }

        // Not yet visited, so visit the node
        if (!this.visitedNodes.contains(analyzingNode)) {
            this.visitedNodes.add(analyzingNode);
            for (T node : this.nodes) {
                if (analyzingNode.getDependencies().contains(node)) {
                    stackVisitedNodes.push(analyzingNode);
                    visit(node, stackVisitedNodes);
                    stackVisitedNodes.pop();
                }
            }
            // Add element to the sorted list
            this.sortedList.add(analyzingNode);

        }
    }


}
