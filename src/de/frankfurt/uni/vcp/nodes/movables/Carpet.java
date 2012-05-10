package de.frankfurt.uni.vcp.nodes.movables;

import com.jme3.scene.Spatial;

import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;
import de.frankfurt.uni.vcp.net.UnitInfo;

/**
 * <h3>  This the class used to create a Carpet unit </h3>
 * 
 * <p> Other than the model used to represent it, there is currently
 *     no special functionality added to the {@link Movable} class
 *     from which {@code Carpet} is inherited. </p>
 * 
 * @author wladimir + saman + bernd
 *
 */

public class Carpet extends Movable {

	public Carpet(UnitInfo info) throws Exception {
		super(info, "Jussuf");
		
		Spatial playerCarpet = SpatialFactory.create(SpatialTypes.CARPET);
		this.attachChild(playerCarpet);
	}
}
