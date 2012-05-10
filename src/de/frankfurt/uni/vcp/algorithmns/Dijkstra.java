/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.frankfurt.uni.vcp.algorithmns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import de.frankfurt.uni.vcp.comparable.SearchNode;
import de.frankfurt.uni.vcp.nodes.Field;

/**
 * <h3> Calculates the shortest path between two given fields using Dijkstra's
 * SPSP algorithm. </h3>
 * 
 * <p> This class implements the {@code getShortestPath} method inherited from 
 *     {@link AbstractShortestPath} using Dijkstra's Single Source Shortest Path
 *     algorithm </p>
 */
public class Dijkstra extends AbstractShortestPath {

    private Map<Field,Field> hash;
    private PriorityQueue<SearchNode> queue;

    /**
     * @param startField
     * @param endField
     */
    public Dijkstra(Field startField, Field endField) {
        this.endField = endField;
        this.startField = startField;
    }


    public Collection<Field> getRange(int range) {
        hash = new HashMap<Field, Field>();
        queue = new PriorityQueue<SearchNode>();

        --range;

        queue.add(new SearchNode(this.startField, 0, this.startField));

        while (queue.size() != 0) {
            SearchNode node = queue.poll();

            if (hash.get(node.getField()) != null)
                continue;

            hash.put(node.getField(), node.getParent());

            if (node.getWeight() > range)
                continue;

            for (Field f : node.getField().getNeighbours())
                queue.add(new SearchNode(f, node.getWeight() + this.getWeight(node.getField(), f), node.getField()));
        }

        return hash.keySet ();
    }

    /**
     * This is the implementation of the abstract method inherited from
     * {@link AbstractShortesPath}
     */
    public LinkedList<Field> getShortestPath() {
        hash = new HashMap<Field, Field>();
        queue = new PriorityQueue<SearchNode>();

        queue.add(new SearchNode(this.startField, 0, this.startField));

        while (queue.size() != 0) {
            SearchNode node = queue.poll();
            if (hash.get(node.getField()) != null)
                continue;

            hash.put(node.getField(), node.getParent());

            if (node.getField() == this.endField) {
                LinkedList<Field> list = new LinkedList<Field>();
                Field field = this.endField;
                list.add(field);

                while ((field = hash.get(field)) != this.startField)
                    list.add(field);

                Collections.reverse(list);
                return list;
            }

            for (Field f : node.getField().getNeighbours()) {
                if (hash.get(f) == null) {
                    if (f.isUsable())
                        queue.add(new SearchNode(f, node.getWeight() + this.getWeight(node.getField(), f), node.getField()));
                }
            }
        }
        return null;
    }

    /**
     * Returns the cost for each edge from one to another node within the graph
     * used to determine the shortest path between two node.
     * 
     * @param from Start node
     * @param to End node
     * @return The cost the edge to move from the start to an end {@link Field}.
     */
    public int getWeight (Field from, Field to){
        return 1;
    }

}
