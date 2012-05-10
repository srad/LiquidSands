package de.frankfurt.uni.vcp.meshes;

import com.jme3.scene.VertexBuffer.Type;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;


public class Plane extends Mesh {
	
	public Plane (float textureScale){

		Vector3f[] vertices = { 
				new Vector3f (-.5f, 0f, .5f),
				new Vector3f (.5f, 0f, .5f),
				new Vector3f (.5f, 0f, -.5f),
				new Vector3f (-.5f, 0f, -.5f),
		};

		Vector2f[] texCoord = { 
				new Vector2f (0f, 0f),
				new Vector2f (textureScale, 0f),
				new Vector2f (textureScale, textureScale),
				new Vector2f (0f, textureScale),
		};

		int[] indexes = { 0, 1, 2, 3 };

		setMode(Mesh.Mode.TriangleFan);

		setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));

		updateBound();
	}
	
	@Override
	public int collideWith(Collidable other, Matrix4f worldMatrix,
			BoundingVolume worldBound, CollisionResults results) {
		return 0;
	}
	
}