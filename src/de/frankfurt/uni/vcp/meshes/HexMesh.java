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


import static com.jme3.math.FastMath.sin;
import static com.jme3.math.FastMath.PI;
import static com.jme3.math.FastMath.ONE_THIRD;

import com.jme3.scene.VertexBuffer.Type;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>Defines the actual hexagon {@link Mesh} as an <i>TriangleFan</i>.</p>
 * 
 * @author Bernd Späth, Wladimir Spindler and Saman Sedighi Rad
 */
public class HexMesh extends Mesh {
	
	public static final float ANGLE = PI * ONE_THIRD;

	public static final Vector3f[]BASE = {	
		//CENTER
		new Vector3f(0f, 0f, 0f),
		// TOP
		new Vector3f(0f, 0f, -1f),
		// LEFT
		new Vector3f(-sin(ANGLE) , 0f, -.5f),
		new Vector3f(-sin(ANGLE) , 0f, .5f),
		// BOTTOM
		new Vector3f(0f, 0f, 1f),
		// RIGHT
		new Vector3f(sin(ANGLE), 0f, .5f),
		new Vector3f(sin(ANGLE), 0f, -.5f)
	}; 

	/**
	 * Construct a HexMesh
	 * @param size base length of the hexagon.
	 */
	public HexMesh (float size){
		
		/*          X  v1
		 *        / | \
		 *  v2  X   |   X  v6
		 *      | \ | / |
		 *      |   X v0|  
		 *      | / |   |
		 *  v3  X   |   X  v5
		 *        \ | /
		 *      v4  X    
		 */    
		
		Vector3f[] vertices = new Vector3f[7];
		for (int i = 0; i < 7; ++i)
			vertices[i] = BASE[i].clone().mult(size);

		Vector2f[] texCoord = new Vector2f[7];
		for (int i = 0; i < 7; ++i)
			texCoord[i] = new Vector2f(BASE[i].x / 2 + .5f, BASE[i].z / 2 + .5f);

		int[] indexes = { 0, 1, 2, 3, 4, 5, 6, 1 };

		setMode(Mesh.Mode.TriangleFan);

		setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));

		updateBound();
	}
}
