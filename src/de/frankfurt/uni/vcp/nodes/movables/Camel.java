package de.frankfurt.uni.vcp.nodes.movables;

import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;
import de.frankfurt.uni.vcp.net.UnitInfo;

/**
 * <h3>  This the class used to create a Camel unit </h3>
 * 
 * <p> Other than the model used to represent it, there is currently
 *     no special functionality added to the {@link Movable} class
 *     from which {@code Camel} is inherited. </p>
 * 
 * @author wladimir + saman + bernd
 *
 */

public class Camel extends Movable {
	
	public Camel(UnitInfo info) throws Exception {
		super(info, "Kami");
			
		Spatial model = SpatialFactory.create(SpatialTypes.CAMEL);
		model.setLocalScale(0.45f);
		model.rotate(0f, FastMath.HALF_PI, 0f);
		this.attachChild(model);
	}
}
