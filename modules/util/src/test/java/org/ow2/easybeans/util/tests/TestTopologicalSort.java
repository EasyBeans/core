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

package org.ow2.easybeans.util.tests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.easybeans.util.topological.GraphCycleException;
import org.ow2.easybeans.util.topological.Node;
import org.ow2.easybeans.util.topological.NodeImpl;
import org.ow2.easybeans.util.topological.TopologicalSort;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the topological sort.
 * @author Florent Benoit
 */
public class TestTopologicalSort {


    @Test
    public void testSort() {
        Node nodeA = new NodeImpl("A");
        Node nodeB = new NodeImpl("B");
        Node nodeC = new NodeImpl("C");
        Node nodeD = new NodeImpl("D");
        Node nodeE = new NodeImpl("E");
        Node nodeF = new NodeImpl("F");

        nodeD.addDependency(nodeA);
        nodeD.addDependency(nodeB);

        nodeE.addDependency(nodeB);
        nodeE.addDependency(nodeC);

        nodeF.addDependency(nodeD);
        nodeF.addDependency(nodeE);

        Set<Node> nodes = new HashSet<Node>();
        nodes.add(nodeA);
        nodes.add(nodeB);
        nodes.add(nodeC);
        nodes.add(nodeD);
        nodes.add(nodeE);
        nodes.add(nodeF);

        List<Node> sortedList = TopologicalSort.sort(nodes);

        // Get indexes
        int iNodeA = sortedList.indexOf(nodeA);
        int iNodeB = sortedList.indexOf(nodeB);
        int iNodeC = sortedList.indexOf(nodeC);
        int iNodeD = sortedList.indexOf(nodeD);
        int iNodeE = sortedList.indexOf(nodeE);
        int iNodeF = sortedList.indexOf(nodeF);

        // Check Node A and B are before D
        Assert.assertTrue(iNodeA < iNodeD);
        Assert.assertTrue(iNodeB < iNodeD);

        // Check Node B and C are before E
        Assert.assertTrue(iNodeB < iNodeE);
        Assert.assertTrue(iNodeC < iNodeE);

        // Check Node D and E are before F
        Assert.assertTrue(iNodeD < iNodeF);
        Assert.assertTrue(iNodeE < iNodeF);
    }


    @Test(expectedExceptions=GraphCycleException.class)
    public void testCycle() {
        Node nodeA = new NodeImpl("A");
        Node nodeB = new NodeImpl("B");
        Node nodeC = new NodeImpl("C");
        Node nodeD = new NodeImpl("D");

        nodeB.addDependency(nodeD);
        nodeC.addDependency(nodeD);

        nodeA.addDependency(nodeB);
        nodeA.addDependency(nodeC);

        nodeD.addDependency(nodeA);

        Set<Node> nodes = new HashSet<Node>();
        nodes.add(nodeA);
        nodes.add(nodeB);
        nodes.add(nodeC);
        nodes.add(nodeD);

        TopologicalSort.sort(nodes);
        Assert.fail("There is a cycle and it appears it was not found :-/");
    }
}
