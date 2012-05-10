package de.frankfurt.uni.vcp.factories;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;

import de.frankfurt.uni.vcp.enums.MaterialTypes;
import de.frankfurt.uni.vcp.game.Game;

/**
 * <h3>Material factory to provide all application wide used materials.</h3>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class MaterialFactory {

	public static Material create(ColorRGBA color) {
		Material m = defaultMaterial();

		m.setColor("Color", color);

		return m;
	}

	public static Material create(MaterialTypes type) throws Exception {
		AssetManager assetManager = Game.getInstance().getAssetManager();
		Material m = defaultMaterial();

		switch (type) {
			case FIELD_BORDER:
				m.setColor("Color", new ColorRGBA(0.7f, 0.6f, 0.4f, 1f));
				break;
			case FIELD_BORDER_SELECTED:
				m.setColor("Color", new ColorRGBA(0.7f, 0.6f, 0.4f, 1f)); // DUPLICATE
				break;
			case FIELD_CENTER:
				m.setColor("Color", new ColorRGBA(0.9f, 0.5f, 0.1f, 1f));
				break;
			case FIELD_SURFACE:
				m.setColor("Color", new ColorRGBA(1, 1, 1, 0.1f));
				m.setTransparent(true);
				break;
			case FIELD_SURFACE_SELECTED:
				m.setColor("Color", new ColorRGBA(1f, 0.8f, 0f, 1f));
				break;
			case CARPET:
				m.setTexture("ColorMap", assetManager.loadTexture("carpet.png"));
				break;
			case CAMEL:
				m.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("kamel.png", false)));
				break;
			case ROSA:
				m.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("rosa.png", false)));
				break;
			case SPHINX:
				m.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("sphinx.png", false)));
				break;
			case ROC_THE_EAGLE:
				m.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("roc.png", false)));
				break;
			case DEFAULT_SHADOW:
				m.setTexture("ColorMap", assetManager.loadTexture("shadow.png"));
				break;
			case HEALTHBAR_BACKGROUND:
				m.setTexture("ColorMap", assetManager.loadTexture("healthBarBackground.png"));
				break;
			case HEALTHBAR_FOREGROUND:
				m.setTexture("ColorMap", assetManager.loadTexture("bar.png"));
				break;
			case PLAYER_SELECTOR_BORDER:
				m.setTexture("ColorMap", assetManager.loadTexture("selection-border.png"));
				break;
			case DEFAULT_MATERIAL:
				m.setColor("Color", new ColorRGBA(1, 1, 1, 0f));
				break;
			case REACHABLE_FIELD_MATERIAL:
				ColorRGBA orange = ColorRGBA.Orange;
				m.setColor("Color", new ColorRGBA(orange.r, orange.g, orange.b, 0.4f));
				break;
			case PATH_IN_MOVE:
				m.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.0f, 0.8f));
				break;
			case PATH_PLANNED:
				m.setColor("Color", new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
				break;
			case MARKER_FIGHT:
				m.setTexture("ColorMap", assetManager.loadTexture("sabres.png"));
				break;
			case MARKER_TRADE:
				m.setTexture("ColorMap", assetManager.loadTexture("fight.png"));
				break;
			case MARKER_MOVE:
				m.setTexture("ColorMap", assetManager.loadTexture("move.png"));
				break;
			case FOG_PARTICLE:
				m = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
				m.setTexture("Texture", assetManager.loadTexture("shadow3.png"));
				break;
			case CLOUD:
				m = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
				m.setTexture("Texture", assetManager.loadTexture("cloud.png"));
				break;
			case GREEN:
				m.setColor("Color", new ColorRGBA(0, 1, 0, 1));
				break;
			case RED:
				m.setColor("Color", new ColorRGBA(1, 0, 0, 1));
				break;
			default:
				throw new Exception("Material doesn't exist");
		}
		return m;
	}

	/**
	 * Provides the basis for all material that are returned for this factory
	 * class.
	 * 
	 * @return The base {@link Material}.
	 */
	private static Material defaultMaterial() {
		AssetManager assetManager = Game.getInstance().getAssetManager();

		Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		return m;
	}
}
