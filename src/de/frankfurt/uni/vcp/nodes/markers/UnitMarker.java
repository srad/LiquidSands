package de.frankfurt.uni.vcp.nodes.markers;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import de.frankfurt.uni.vcp.enums.MaterialTypes;
import de.frankfurt.uni.vcp.factories.MaterialFactory;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.meshes.Oktahedron;
import de.frankfurt.uni.vcp.nodes.Unselectable;

public class UnitMarker extends Unselectable {
	
	ColorRGBA[] colors={new ColorRGBA(0.3f,0.8f,0.0f,0.6f), new ColorRGBA(0.0f,0.6f,0.8f,0.4f), new ColorRGBA(0.7f,0.7f,0.0f,0.6f)};
	
	public UnitMarker(int i) throws Exception {
		
		oktahedron(new Vector3f(0f,1.5f,0f), true, i);
		oktahedron(new Vector3f(0f,1.5f,0f), false, i);
	}
	
	public void oktahedron(Vector3f pos, boolean wf, int i){
		Oktahedron ok=new Oktahedron();
		Material mat=null;
		try {
			mat = MaterialFactory.create(MaterialTypes.DEFAULT_MATERIAL);
		} catch (Exception e) {
			LogHelper.getLogger().error("DEFAULT_MATERIAL could not be loaded");
		}
		mat.setColor("Color", colors[i]);
		mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		if(wf){
			mat.setColor("Color", new ColorRGBA(0f,0f,0f,1f));
			mat.getAdditionalRenderState().setWireframe(true);
		}
		
		Geometry geo=new Geometry("oktahedron", ok);
		geo.setMaterial(mat);
		geo.setQueueBucket(Bucket.Translucent);
		geo.move(pos);
		geo.scale(0.5f);
		
		this.attachChild(geo);
	}
}
