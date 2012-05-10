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

package de.frankfurt.uni.vcp.meshes;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Matrix4f;
import com.jme3.scene.shape.Sphere;

import de.frankfurt.uni.vcp.nodes.Field;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>Defines a sphere shape used for each {@link Field} center markerd.</p>
 * 
 * @author Bernd Späth, Wladimir Spindler and Saman Sedighi Rad
 */
public class HexMarker extends Sphere {

	public HexMarker(){
		super(4, 4, 0.05f);
	}
	
	@Override
	// FIX PICKING
	// we don't want to select markers in picking
	public int collideWith(Collidable other, Matrix4f worldMatrix,
			BoundingVolume worldBound, CollisionResults results) {
		return 0;
	}
}
