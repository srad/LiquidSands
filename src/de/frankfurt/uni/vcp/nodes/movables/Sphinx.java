package de.frankfurt.uni.vcp.nodes.movables;

import com.jme3.scene.Spatial;

import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;
import de.frankfurt.uni.vcp.net.UnitInfo;

/**
 * <h3>  This the class used to create a Sphinx unit </h3>
 * 
 * <p> Other than the model used to represent it, there is currently
 *     no special functionality added to the {@link Movable} class
 *     from which {@code Sphinx} is inherited. </p>
 * 
 * @author wladimir + saman + bernd
 *
 */

public class Sphinx extends Movable {
		  
	public Sphinx(UnitInfo info) throws Exception {
		super(info, "Sphinx");
		  
		Spatial model = SpatialFactory.create(SpatialTypes.SPHINX);
		this.attachChild(model);
    }
	  
}
