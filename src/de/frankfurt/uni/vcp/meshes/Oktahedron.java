package de.frankfurt.uni.vcp.meshes;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

/**
 * This marker appears above the player units in different colors to determine
 * which player units belong to which player.
 * 
 */
public class Oktahedron extends Mesh {
	
	private Vector3f[] vertices = { new Vector3f(0.0f, 1.0f, 0.0f),
			new Vector3f(0.5f, 0.0f, 0.5f), new Vector3f(-.5f, 0.0f, 0.5f),
			new Vector3f(-.5f, 0.0f, -.5f), new Vector3f(0.5f, 0.0f, -.5f),
			new Vector3f(0.0f, -1.f, 0.0f) };
	
	private Vector2f[] texCoord = { new Vector2f(0.5f, 1.0f),
			new Vector2f(0.25f, 0.5f), new Vector2f(0.5f, 0.5f),
			new Vector2f(0.75f, 0.5f), new Vector2f(1.0f, 0.5f),
			new Vector2f(0.5f, 0.0f) };
	
	/** Outer faces first */
	// private int[] indexes = { 0,2,1, 0,3,2, 0,4,3, 0,1,4, 5,1,2, 5,2,3,
	// 5,3,4, 5,4,1, 0,1,2, 0,2,3, 0,3,4, 0,4,1, 5,2,1, 5,3,2, 5,4,3, 5,1,4 };
	
	/** Inner faces first */
	private int[] indexes = { 0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 1, 5, 2, 1, 5,
			3, 2, 5, 4, 3, 5, 1, 4, 0, 2, 1, 0, 3, 2, 0, 4, 3, 0, 1, 4, 5, 1,
			2, 5, 2, 3, 5, 3, 4, 5, 4, 1 };

	public Oktahedron() {
		setMode(Mesh.Mode.Triangles);
		setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));
		updateBound();
	}
}
