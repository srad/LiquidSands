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

package de.frankfurt.uni.vcp.comparable;

import de.frankfurt.uni.vcp.nodes.Field;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>Datastructure used to implement Dijkstra's SPSP algorithm.
 * This Comparable implementation allows to compare the determine
 * the weight difference of adjacent {@link Field}.</p>
 * 
 * @author Bernd Sp�th
 */
public class SearchNode implements Comparable<SearchNode> {

	public SearchNode (Field field, int weight, Field parent) {
		this.field = field;
		this.weight = weight;
		this.parent = parent;
	}

	/**
	 * Compare this nodes weight to another nodes weight.
	 */
	@Override
	public int compareTo(SearchNode other) {
		return this.weight - other.weight;
	}
	
	// reference to the node
	private Field field;
	// reference to the the parent node
	private Field parent;
	// total cost of the shortest path to this node
	private int weight;
	
	// OMG OOOP! tons of silly access functions here!
	
	/**
	 * Get the reference to a field in this node
	 * @return A reference to the stored field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Set the stored reference to a field in this node
	 * @param field A reference to the field to be stored
	 */
	public void setField(Field field) {
		this.field = field;
	}

	/**
	 * Get the field stored as this nodes parent field
	 * @return The parent field
	 */
	public Field getParent() {
		return parent;
	}

	/**
	 * Set the field to be stored as this nodes parent field
	 * @param parent The parent field
	 */
	public void setParent(Field parent) {
		this.parent = parent;
	}
	
	/**
	 * Get the weight of an edge between the stored field and its parent field
	 * @return The weight of this node
	 */
	public int getWeight() {
		return weight;
	}

	/** Set the weight of an edge between the stored field and its parent field
	 * @param weight The weight to be set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}