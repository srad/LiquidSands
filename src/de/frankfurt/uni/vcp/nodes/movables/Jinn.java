package de.frankfurt.uni.vcp.nodes.movables;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.net.UnitInfo;

/**
 * <h3>  This the class used to create a Jinn unit </h3>
 * 
 * <p> Other than the model used to represent it, there is currently
 *     no special functionality added to the {@link Movable} class
 *     from which {@code Jinn} is inherited. </p>
 * 
 * @author wladimir + saman + bernd
 *
 */

public class Jinn extends Movable {
	
	public Jinn(UnitInfo info) throws Exception {
		super(info, "Jinn");
		AssetManager assetManager = Game.getInstance().getAssetManager();
		
		Spatial model = assetManager.loadModel("jinn.mesh.xml");
		
		model.scale(0.3f);
		model.rotate(0f, -FastMath.HALF_PI, 0f);
		model.setLocalTranslation(0f, 0.2f, 0f);
		this.attachChild(model);	
	}
}
