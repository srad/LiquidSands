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
import java.util.LinkedList;

import de.frankfurt.uni.vcp.nodes.Field;

/**
 * <h3> The abstract base class used to calculate and to represent a
 * shortest path between to given Fields </h3>
 * 
 * <p> Given starting field and ending field the shortest path between both
 *     fields can be calculated using the {@code getShortestPath} method,
 *     which will return a list of all the fields in between in sequential
 *     order </p> 
 * 
 * @author wladimir + saman + bernd
 * 
 */
public abstract class AbstractShortestPath {

	protected Field startField;
	protected Field endField;

	 /**
   * Get all fields, that are reachable from the starting field
   * within a given ammount of steps
   * 
   * @param range The maximium range, that is allowed
   * @return List of the fields, that are reachable within the given range
   */
  abstract public Collection<Field> getRange(int range);

	
	/**
	 * Get the shortest path between the two fields set as 
	 * starting and ending field.
	 * 
	 * @return List of the fields, that lie between starting and ending field
	 */
	abstract public LinkedList<Field> getShortestPath();
	
	/**
	 * Get the field currently set as starting field.
	 * @return The starting field
	 */
	public Field getStartField() {
		return startField;
	}
	
	/**
	 * Set the starting field of this path
	 * @param startField The starting field
	 */
	public void setStartField(Field startField) {
		this.startField = startField;
	}

	/**
	 * Get the field currently set as ending field.
	 * @return The ending field
	 */
	public Field getEndField() {
		return endField;
	}

	/**
	 * Set the ending field of this path
	 * @param endField The ending field
	 */
	public void setEndField(Field endField) {
		this.endField = endField;
	}	
}
