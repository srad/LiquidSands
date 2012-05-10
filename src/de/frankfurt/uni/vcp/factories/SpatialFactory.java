package de.frankfurt.uni.vcp.factories;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import de.frankfurt.uni.vcp.enums.MaterialTypes;
import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.meshes.HexMarker;

/**
 * <h3>General factory to provide all application wide used spatials.</h3>
 *
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class SpatialFactory {

	/**
	 * Factory create method to provide, the actual spatials.
	 * 
	 * @param type
	 *            Available types are provided by the enum {@link SpatialTypes}.
	 * @return The {@link Spatial} object.
	 * @throws Exception
	 *             If the requestd spatial is not available an Exception is
	 *             thrown with an appropriate message.
	 */
	public static Spatial create(SpatialTypes type) throws Exception {
		AssetManager assetManager = Game.getInstance().getAssetManager();
		Spatial s = null;

		switch (type) {
		case CARPET:
			s = assetManager.loadModel("carpet.mesh.xml");
			break;
		case CAMEL:
			s = assetManager.loadModel("kamel.mesh.xml");
			break;
		case ROSA:
			s = assetManager.loadModel("rosa.mesh.xml");
			break;
		case SPHINX:
			s = assetManager.loadModel("sphinx.mesh.xml");
			break;
		case ROC_THE_EAGLE:
			s = assetManager.loadModel("roc.mesh.xml");
			s.setMaterial(MaterialFactory.create(MaterialTypes.ROC_THE_EAGLE));
			break;
		case MARKER_FIGHT:
			s = assetManager.loadModel("marker_fight.mesh.xml");
			//s.setMaterial(MaterialFactory.create(MaterialTypes.MARKER_FIGHT));
			s.setName("marker");
			break;
		case MARKER_TRADE:
			s = assetManager.loadModel("marker_trade.mesh.xml");
			//s.setMaterial(MaterialFactory.create(MaterialTypes.MARKER_TRADE));
			s.setName("marker");
			break;
		case MARKER_MOVE:
			s = assetManager.loadModel("marker_move.mesh.xml");
			//s.setMaterial(MaterialFactory.create(MaterialTypes.MARKER_MOVE));
			s.setName("marker");
			break;
		case MARKER_END:
			s = assetManager.loadModel("roc.mesh.xml");
			// s = assetManager.loadModel("marker_end.mesh.xml");
			s.setMaterial(MaterialFactory.create(MaterialTypes.ROC_THE_EAGLE));
			break;
		case MARKER_FIELD:
			s = new Geometry("center", new HexMarker());
			s.setMaterial(MaterialFactory.create(MaterialTypes.FIELD_CENTER));
			break;
		default:
			throw new Exception("Spatial does not exist");
		}
		return s;
	}

}
