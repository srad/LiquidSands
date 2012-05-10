/* This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.frankfurt.uni.vcp.nodes;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import de.frankfurt.uni.vcp.Clickable;
import de.frankfurt.uni.vcp.config.MapConfig;
import de.frankfurt.uni.vcp.enums.MaterialTypes;
import de.frankfurt.uni.vcp.factories.MaterialFactory;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.gui.controllers.HudScreenController;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.meshes.HexBorder;
import de.frankfurt.uni.vcp.meshes.HexMarker;
import de.frankfurt.uni.vcp.meshes.HexMesh;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.nodes.movables.Movable;
import de.frankfurt.uni.vcp.units.Player;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2.</h3>
 * 
 * <p>
 * A composition of one single field based on {@link HexBorder} and
 * {@link HexMesh} and {@link HexMarker}.
 * </p>
 * 
 * @author Bernd Sp�th, Wladimir Spindler and Saman Sedighi Rad
 */
public class Field extends Node implements Clickable {

	/** Used to determine the position of adjacent fields for even rows. */
	public static int[][] evenOffsets = { { +1, +1 }, { 0, +1 }, { +1, 0 }, { -1, 0 }, { +1, -1 }, { 0, -1 } };

	/** Used to determine the position of adjacent fields for odd rows. */
	public static int[][] oddOffsets = { { 0, +1 }, { -1, +1 }, { +1, 0 }, { -1, 0 }, { 0, -1 }, { -1, -1 } };

	public int iIndex;
	public int jIndex;
	public Movable unit = null;

	public Node mesh;
	public Geometry border;
	public Geometry marker;

	public ParticleEmitter fog;
	private int viewers = 0;

	public int getViewers() {
		return viewers;
	}

	public void setViewers(int i) {
		viewers = i;
	}

	public HashMap<String, Material> layers = new HashMap<String, Material>();

	// /** Used to save the current material if one field has been selected
	// multiple times. */
	// private int stackCounter;

	/**
	 * Construct a new field.
	 * 
	 * @param iIndex
	 *            Index of this Field in horizontal direction
	 * @param jIndex
	 *            Index of this Field in vertical direction
	 */
	public Field(int iIndex, int jIndex) {
		this.iIndex = iIndex;
		this.jIndex = jIndex;

		// this.stackCounter = 0;

		try {
			// MESH
			Geometry geo = new Geometry("mesh", new HexMesh(1f));
			geo.setMaterial(MaterialFactory.create(MaterialTypes.FIELD_SURFACE));
			geo.setQueueBucket(Bucket.Transparent);
			geo.setUserData("i", new Integer(iIndex));
			geo.setUserData("j", new Integer(jIndex));
			mesh = new Node();
			mesh.attachChild(geo);
			this.attachChild(this.mesh);

			// BORDER
			this.border = new Geometry("meshBorder", new HexBorder(1f));
			this.border.setMaterial(MaterialFactory.create(MaterialTypes.FIELD_BORDER));
			// this.border.setQueueBucket(Bucket.Transparent);
			this.attachChild(this.border);

			// MARKER
			this.marker = new Geometry("center", new HexMarker());
			this.marker.setMaterial(MaterialFactory.create(MaterialTypes.FIELD_CENTER));
			this.attachChild(marker);
			this.marker.setLocalTranslation(this.mesh.center().getLocalTranslation());
		} catch (Exception e) {
			e.printStackTrace();
		}
		addParticles();
	}

	/**
	 * Determine wether this field is usable, or if it is occupied by any unit
	 * 
	 * @return {@code true} if this field is usable, {@code false} otherwise
	 */
	public boolean isUsable() {
		return unit == null;
	}

	/**
	 * Calculate all usable fields connected to a given field.
	 * 
	 * @return list of usable fields connected to this field
	 */
	public List<Field> getNeighbours() {
		List<Field> list = new LinkedList<Field>();

		HexMap hexMap = Game.getInstance().getHexMap();
		MapConfig mapConfig = Game.getInstance().getMapConfig();

		int[][] offsets = (this.jIndex % 2 == 0) ? evenOffsets : oddOffsets;
		for (int[] offset : offsets) {

			int i = this.iIndex + offset[0];
			int j = this.jIndex + offset[1];
			if (mapConfig.isUsable(i, j))
				list.add(hexMap.getField(i, j));
		}
		return list;
	}

	@Override
	public String toString() {
		return "<Field: i=" + iIndex + " j=" + jIndex + ">";
	}

	// NEW

	/**
	 * Mark this Field as selected using default selection color
	 * 
	 * @throws Exception
	 */
	public void updateSelection() {
		Game game = Game.getInstance();
		HexMap hexMap = game.getHexMap();

		try {

			for (String l : hexMap.layerOrder) {
				Material material = layers.get(l);
				if (material != null) {
					this.mesh.setMaterial(material);

					return;
				}
			}
			this.mesh.setMaterial(MaterialFactory.create(MaterialTypes.FIELD_SURFACE));
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}

	public void select(String layer, ColorRGBA color) {

		layers.put(layer, MaterialFactory.create(color));

		updateSelection();
	}

	public void select(String layer, Material material) {
		layers.put(layer, material);

		updateSelection();
	}

	/**
	 * Remove any selection mark from this Field.
	 * 
	 * @throws Exception
	 */
	public void unselect(String layer) {
		layers.remove(layer);

		updateSelection();
	}

	/**
	 * Remove any selection mark from this Field.
	 * 
	 * @throws Exception
	 */
	public void unselectAll() {
		layers.clear();

		updateSelection();
	}

	// ORIGINAL

	// /**
	// * Mark this Field as selected using default selection color
	// * @throws Exception
	// */
	// public void select() {
	// try {
	// Material m =
	// MaterialFactory.create(MaterialTypes.FIELD_SURFACE_SELECTED);
	// this.mesh.setMaterial(m);
	// ++this.stackCounter;
	// } catch (Exception e) {
	// LogHelper.getLogger().error(e.getMessage());
	// }
	// }
	//
	// /**
	// * Mark this Field as selected using a specified color
	// * @param color The color to be used to highlight this field
	// * @throws Exception
	// */
	// public void select(ColorRGBA color) {
	// Material m;
	// try {
	// m = MaterialFactory.create(MaterialTypes.FIELD_SURFACE);
	// m.setColor("Color", color);
	// this.mesh.setMaterial(m);
	// ++this.stackCounter;
	// }
	// catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * Remove any selection mark from this Field.
	// * @throws Exception
	// */
	// public void unselect() {
	// try {
	// --this.stackCounter;
	// if (this.stackCounter <= 0) {
	// this.mesh.setMaterial(MaterialFactory.create(MaterialTypes.FIELD_SURFACE));
	// this.stackCounter = 0;
	// }
	// } catch (Exception e) {
	// LogHelper.getLogger().error(e.getMessage());
	// }
	// }
	//

	/**
	 * Get the unit (if any), that is currently positioned on this field.
	 * 
	 * @return The unit positioned on this field if there is any, {@code null}
	 *         otherwise
	 */
	public Movable getUnitOn() {
		for (Player unit : Game.getInstance().getPlayers()) {
			for (Movable m : unit.units) {
				if (m.getField() == this)
					return m;
			}
		}
		return null;
	}

	/**
	 * Add this field as a new waypoint to the way planning of the currently
	 * selected unit
	 * @throws ProtocolError 
	 * @throws StatusError 
	 * @throws IOException 
	 */
	@Override
	public void onClick(String name) throws IOException, StatusError, ProtocolError {
		// TODO Auto-generated method stub
		Game game = Game.getInstance();
		Movable selectedUnit = game.getSelectedUnit();
		HudScreenController hud = (HudScreenController) game.getNifty().getScreen("hud").getScreenController();

		LogHelper.getLogger().info("onClick: " + this);

		// SELECT
		if (name.equals("select")) {
			switch (game.getSelectionMode()) {
				case ANY:
					// During movement no field selection is allowed,
					// because we already sent it to the server.
					if ((selectedUnit != null) && selectedUnit.isMoving()) {
						return;
					}
					if (selectedUnit != null) {
						selectedUnit.addWaypoint(this);
						if (selectedUnit.ways.size() > 0) {
							hud.showUndoRedoPanel();
						}
					}
					break;
			}
		}

		// MOVE: Just move on right click.
		if (name.equals("move") && selectedUnit != null) {
			if (selectedUnit.ways.size() != 0)
				game.getSelectedUnit().move();
			// TODO: Was originally the different movement modes, we don't use
			// that anymore.
			// game.getNifty().getScreen("hud").findElementByName("movement_layer").show();
		}
	}

	/**
	 * The fog is implemented as {@link ParticleEmitter}, which is enabled of
	 * this field by this method.
	 */
	public void addParticles() {
		fog = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 50);

		try {
			fog.setMaterial(MaterialFactory.create(MaterialTypes.FOG_PARTICLE));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fog.setNumParticles(4);
		fog.setRandomAngle(true);
		fog.setImagesX(1);
		fog.setImagesY(1); // 2x2 texture animation
		fog.setEndColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.6f)); // red

		fog.setStartColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f)); // yellow
		fog.getParticleInfluencer().setInitialVelocity(new Vector3f(0.5f, 0, 0));
		fog.setStartSize(1.4f);
		fog.setEndSize(1.4f);
		fog.setGravity(0.05f, -0.05f, 0);
		fog.setLowLife(1f);
		fog.setHighLife(2f);
		fog.getParticleInfluencer().setVelocityVariation(0.5f);
		fog.move(0f, 1f, 0f);
		fog.setQueueBucket(Bucket.Translucent);
		fog.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		this.attachChild(fog);
	}

	/**
	 * Displays the fog above the all fields in range.
	 */
	public void enableFog() {
		if (viewers > 0)
			viewers--;
		if (viewers < 1)
			fog.setEnabled(true);
	}

	/**
	 * Removes the fog from all fields.
	 */
	public void disableFog() {
		viewers++;
		fog.setEnabled(false);
		fog.killAllParticles();
	}

	public LinkedList<MaterialTypes> matStack = new LinkedList<MaterialTypes>();

	/**
	 * Changes this fields material.
	 * 
	 * @param materialType
	 *            Material to use for this field.
	 */
	public void highlight(MaterialTypes materialType) {
		try {
			this.mesh.setMaterial(MaterialFactory.create(materialType));

			matStack.push(materialType);

		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}

}
