package de.frankfurt.uni.vcp.nodes.movables;

import com.jme3.scene.Spatial;

import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;

/**
 * <h3>  This the class used to create a Roc unit </h3>
 * 
 * <p> Other than the model used to represent it, there is currently
 *     no special functionality added to the {@link Movable} class
 *     from which {@code Roc} is inherited. </p>
 * 
 * @author wladimir + saman + bernd
 *
 */

public class Roc extends Movable {
	
	public Roc() throws Exception {	
		super(null, "Roc");
		Spatial rocTheEagle = SpatialFactory.create(SpatialTypes.ROC_THE_EAGLE);
		this.attachChild(rocTheEagle);
		
		speed = 0.1f;
	}

	@Override
	public void onClick(String name) {
		// do nothing !
	}
	
}
