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

package de.frankfurt.uni.vcp.nodes;


/**
 * <h3>  This is the base class from which all unselectable objects in the game are inherited </h3>
 * 
 * <p> Some objects in the game, such as the heightmap used to display the
 *     landscape on which the game takes place don't need to be selectable
 *     by the player. </p>
 *     
 * <p> To ease picking of objects beneath the mouse cursor all unselectable
 *      objects in the game are inherited from this class. </p>
 *      
 * <p> Classes derived from this class won't be pickable. </p>
 *      
 * @author wladimir + saman + bernd
 *
 */
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.scene.Node;

public class Unselectable extends Node {
	
	@Override
	public int collideWith(Collidable arg0, CollisionResults arg1) {
		return 0;
	}

}
