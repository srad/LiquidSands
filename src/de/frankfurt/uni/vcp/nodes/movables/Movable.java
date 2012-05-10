package de.frankfurt.uni.vcp.nodes.movables;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionTrack;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import de.frankfurt.uni.vcp.Clickable;
import de.frankfurt.uni.vcp.algorithmns.Dijkstra;
import de.frankfurt.uni.vcp.enums.FogMode;
import de.frankfurt.uni.vcp.enums.FollowMode;
import de.frankfurt.uni.vcp.enums.MaterialTypes;
import de.frankfurt.uni.vcp.enums.PlayerStates;
import de.frankfurt.uni.vcp.enums.SelectionMode;
import de.frankfurt.uni.vcp.factories.MaterialFactory;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.gui.controllers.HudScreenController;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.meshes.Plane;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.net.TCPClient;
import de.frankfurt.uni.vcp.net.UnitInfo;
import de.frankfurt.uni.vcp.nodes.Explosion;
import de.frankfurt.uni.vcp.nodes.Field;
import de.frankfurt.uni.vcp.nodes.HealthBar;
import de.frankfurt.uni.vcp.nodes.HexMap;
import de.frankfurt.uni.vcp.units.Inventory;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>
 * Any node which has the capability to move within the game needs to subclass
 * {@link Movable}. This class provides helper methods to follow a
 * {@link MotionPath}.
 * </p>
 * 
 * @author Bernd Sp�th, Wladimir Spindler and Saman Sedighi Rad
 */
public abstract class Movable extends Node implements Clickable {

	/**
	 * Current unit state which determines next available state. This is
	 * basically implemented as an state machine with transitions
	 */
	protected PlayerStates state;

	/** Type of movement automatic/manual */
	private PlayerStates moveMode;

	/**
	 * The {@link UnitInfo} associated with this unit, as it is reported by the
	 * game server
	 */
	public UnitInfo info;

	/** Selector above the unit */
	public Node marker;
	
	/* The actual model for this node */
	public Spatial model;
	
	/** The shadow is emulated by an image below the unit */
	public Geometry shadow;
	
	/** The circle selection border around the unit */
	public Spatial selectionBorder;

	/** The health bar above the unit */
	public HealthBar healthBar;

	/** TODO: comment */
	public boolean automate = false;

	/** The selected way for this unit */
	public Deque<LinkedList<Field>> ways = new LinkedList<LinkedList<Field>>();
	
	/** Saves the undid path segments */
	private Deque<LinkedList<Field>> undoStack = new LinkedList<LinkedList<Field>>();

	/** Is used to determines who shall be followed */
	private Movable target = null;
	
	/** Automatic or manually follow */
	private FollowMode followMode;

	/** Saves the last path that has been walked for the follow function */
	private LinkedList<Field> lastPath = new LinkedList<Field>();
	
	/** All fields which are visible for the unit (the range) used for the fog of war */
	public LinkedList<Field> fieldOfView = new LinkedList<Field>();
	
	public int lastposI=0;
	public int lastposJ=0;

	/**
	 * Construct a new Movable from a given {@link UnitInfo}
	 * 
	 * @param info
	 *            The {@code UnitInfo} giving detailed information about the
	 *            movable to be constructed
	 * @throws Exception
	 */
	public Movable(UnitInfo info, String unitName) throws Exception {
		this.info = info;
		// Initial state.
		this.setState(PlayerStates.START);

		marker = new Node();
		marker.setLocalTranslation(0f, 1f, 0f);
		marker.setShadowMode(ShadowMode.Off);
		this.attachChild(marker);

		// SHADOW, TODO: remove static shadow finally.
//		shadow = new Geometry("shadow", new Plane(1f));
//		shadow.setMaterial(MaterialFactory.create(MaterialTypes.DEFAULT_SHADOW));
//		shadow.setQueueBucket(Bucket.Translucent);
//		shadow.setLocalScale(1.8f);
//		shadow.setLocalTranslation(0f, .01f, 0f);
//		this.attachChild(shadow);

		// SELECTION-BORDER
		selectionBorder = new Geometry("selection-border", new Plane(1f));
		selectionBorder.setQueueBucket(Bucket.Translucent);

		selectionBorder.setMaterial(MaterialFactory.create(MaterialTypes.PLAYER_SELECTOR_BORDER));
		selectionBorder.setLocalScale(1.6f);
		selectionBorder.setLocalTranslation(0f, .3f, 0f);
		selectionBorder.setShadowMode(ShadowMode.Off);

		// HEALTH BAR
		healthBar = new HealthBar();
		healthBar.setShadowMode(ShadowMode.Off);
		
		setShadowMode(ShadowMode.Cast);
		setName(unitName);
	}

	/**
	 * As the features <i>intercept</i> and <i>follow</i> are currently not
	 * implemented at the moment this method has no effect. <br>
	 * Its inteded use is to set this movables target.
	 * 
	 * @param target
	 *            The target to intercept/follow
	 */
	public void setTarget(Movable target) {
		this.target = target;
	}

	/**
	 * Add a new waypoint to this movables planned movement. <br>
	 * A shortest path from either the last waypoint given, or the actual
	 * location of this movable if no previous waypoint exists will be
	 * calculated. <br>
	 * All fields on this path will be added to this movables way planning.
	 * 
	 * @param field
	 */
	public void addWaypoint(Field field) {

		if (ways.size() > 0) {
			// LinkedList<Field> way = ways.getLast();
			LogHelper.getLogger().info(
					"Adding way point " + field.toString() + " for "
							+ getName());
		}

		Field start = (ways.size() > 0) ? ways.getLast().getLast() : this
				.getField();
		if (start == field)
			return;

		LinkedList<Field> path = new Dijkstra(start, field).getShortestPath();
		ways.addLast(path);

		undoStack.clear();
		highlightPath();
	}

	/**
	 * Remove the last waypoint from this movables way planning <br>
	 * This will also remove any fields lying on the route between this and the
	 * previously added waypoint.
	 */
	public void undo() {
		if (ways.size() > 0) {
			undoStack.addFirst(ways.removeLast());
		}
		highlightPath();
	}

	/**
	 * Undos all steps at once.
	 */
	public void undoAll() {
		while (!ways.isEmpty()) {
			undo();
		}
	}

	/**
	 * Add the last waypoint previously removed to this movables way planning
	 */
	public void redo() {
		if (undoStack.size() > 0)
			ways.addLast(undoStack.removeFirst());
		highlightPath();
	}

	/**
	 * Highlight the fields lying on the path of this movables way planning
	 */
	public void highlightPath() {
		HexMap hexMap = Game.getInstance().getHexMap();
		try {
			ColorRGBA color = new ColorRGBA(0f, 1f, 0f, 1f);

			int i = 0;
			float factor = 1f;

			hexMap.clearLayer("path");
			for (List<Field> l : ways) {
				for (Field f : l) {

					if (i > info.movement && (i - info.movement) % info.utype.maxmovement == 0)
						factor *= .5f;
					++i;
					if (i < info.movement)
						color = new ColorRGBA(0f, 1f, 0f, 1f);

					if (i == info.movement)
						factor = 1f;

					if (i > info.movement)
						color = new ColorRGBA(1f, 1f, 0f, .2f + factor);

					f.select("path", color);
				}
			}
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}

	/**
	 * Determines the nodes that are reachable from a given radius around the
	 * movables current field position.
	 * 
	 * @param k
	 *            Field radius.
	 * @return List of readable fields within the given radius.
	 */
	public LinkedList<Field> getReachableFields(int k) {

		LinkedList<Field> all = new LinkedList<Field>();

		List<Field> last = new LinkedList<Field>();
		LinkedList<Field> next = new LinkedList<Field>();
		last.add(this.getField());

		for (int i = 0; i < k; i++) {

			for (Field f : last) {

				for (Field nf : f.getNeighbours()) {
					if (!all.contains(nf) && nf.unit == null) {
						all.add(nf);
						next.add(nf);
					}
				}
			}
			last.clear();
			last.addAll(next);
			next.clear();
		}
		return all;
	}

	/**
	 * TODO: what does this do, and how it differs from {@link #getReachableFields(int)} ?
	 * @param k
	 * @return
	 */
	public LinkedList<Field> getVisibleFields(int k) {
		
		LinkedList<Field> all = new LinkedList<Field>();

		List<Field> last = new LinkedList<Field>();
		LinkedList<Field> next = new LinkedList<Field>();
		last.add(this.getField());
		all.add(this.getField());

		for (int i = 0; i < k; i++) {

			for (Field f : last) {

				for (Field nf : f.getNeighbours()) {

					if (!all.contains(nf)) {
						CollisionResults results = new CollisionResults();
						Vector3f here = getField().getLocalTranslation().add(
								0f, 0.01f, 0f);
						Vector3f there = nf.getLocalTranslation().add(0f,
								0.01f, 0f);
						Ray ray = new Ray(here, there.subtract(here)
								.normalize());
						Game.getInstance().map.collideWith(ray, results);
						if (results.size() > 0) {
							CollisionResult closest = results
									.getClosestCollision();

							if (closest.getDistance() > here.distance(there)) {
								all.add(nf);
								next.add(nf);
							}
						}
					}
				}
			}
			last.clear();
			last.addAll(next);
			next.clear();
		}
		return all;
	}

	public void updateVisibleArea() {
		enableFog();
		fieldOfView = getVisibleFields(info.utype.maxmovement);
		disableFog();
	}

	/**
	 * Enables the fog of war for the fields {@linkplain #fieldOfView}.
	 */
	public void enableFog() {
		for (Field f : fieldOfView) {
			f.enableFog();
		}
	}

	/**
	 * Disables the fog of war for the fields {@linkplain #fieldOfView}.
	 */
	public void disableFog() {
		for (Field f : fieldOfView) {
			f.disableFog();
		}
	}

	/**
	 * Highlights the given list of fields with the {@link Material} of
	 * {@link MaterialTypes#REACHABLE_FIELD_MATERIAL}.
	 * 
	 * @param list
	 */
	public void highlightRange(LinkedList<Field> list) {
		for (Field f : list) {
			try {
				Material material = MaterialFactory.create(MaterialTypes.REACHABLE_FIELD_MATERIAL);
				f.select("range", material);
			} catch (Exception e) {
				LogHelper.getLogger().error(e.getCause());
			}
		}
	}

	/**
	 * Get a list of the fields lying on the path in this movables way planning
	 * that are still reachable in this turn.
	 * 
	 * @return List of fields still reachable in this turn
	 */
	public List<Field> getPathInRange() {
		List<Field> path = new LinkedList<Field>();

		int range = info.movement;

		for (List<Field> l : ways) {
			for (Field f : l) {
				--range;
				if (range < 0)
					return path;
				path.add(f);
			}
		}
		return path;
	}

	/**
	 * Get the field this movable is currently standing on.
	 * 
	 * @return The field of this movable
	 */
	public Field getField() {
		HexMap hexMap = Game.getInstance().getHexMap();
		return hexMap.positionToField(this.getLocalTranslation().clone());
	}

	/**
	 * Update the unitinfo associated with this movable
	 * 
	 * @throws IOException
	 * @throws StatusError
	 * @throws ProtocolError
	 */
	public void updateStats() throws IOException, StatusError, ProtocolError {
		Game game = Game.getInstance();
		String gameId = game.getGameId();
		TCPClient client = game.getClient();
		String playerId = info.owner.playerId;

		if (playerId == null)
			playerId = game.getDummyPlayerId();

		info = client.unitinfo(gameId, playerId, info.unitid);
		// float value = Math.max(0, info.utype.maxhitpoints - info.hitpoints);
		this.healthBar.setPercentage(((float) info.hitpoints)
				/ ((float) info.utype.maxhitpoints));
	}

	/**
	 * Move this movable along the fields in its way planning
	 * 
	 * @throws IOException
	 * @throws StatusError
	 * @throws ProtocolError
	 */
	public void move() throws IOException, StatusError, ProtocolError {
		Game game = Game.getInstance();
		String gameId = game.getGameId();
		TCPClient client = game.getClient();

		String playerId = game.getActivePlayer().playerId;

		game.getHexMap().clearLayer("range");

		if (game.getActiveUnit() != null)
			return;

		if (!canStillAct())
			return;

		List<Field> path = getPathInRange();

		client.move(gameId, playerId, info.unitid, path);
		getField().unit = null;
		setMoveMode(PlayerStates.MANUAL_MOVE);
		game.setActiveUnit(this);
		game.startMovement();

	}

	/**
	 * Attack another movable <br>
	 * Depending on the outcome of this attack status information for both
	 * movables participating in the fight will be update.
	 * 
	 * @param target
	 *            The movable to attack
	 * @throws IOException
	 * @throws StatusError
	 * @throws ProtocolError
	 */
	public void attack(Movable target) throws IOException, StatusError,	ProtocolError {
		Game game = Game.getInstance();
		String gameId = game.getGameId();
		TCPClient client = game.getClient();

		String playerId = game.getActivePlayer().playerId;
		
		game.setActiveUnit(this);
		game.setSelectionMode(SelectionMode.ANY);
		client.attack(gameId, playerId, info.unitid, target.info.unitid);

		setState(PlayerStates.END);
		game.nextPlayer();

		this.updateStats();
		target.updateStats();

		game.setSelectionMode(SelectionMode.ANY);
	}

	/**
	 * Start a trade dialog with another unit. <br>
	 * 
	 * @param target
	 */
	public void trade() {
		Game game = Game.getInstance();

		Movable from = game.getSelectedUnit();
		game.setActiveUnit(from);
		
		game.getHud().tradePopup(this);
		game.setSelectionMode(SelectionMode.ANY);
	}

	/**
	 * Set the movable to follow another movable, using specified
	 * {@link FollowMode}
	 * 
	 * @param target
	 *            The movable to follow
	 * @param followMode
	 *            The follow mode to use
	 */
	public void follow(Movable target, FollowMode followMode) {
		this.setFollowMode(followMode);
		this.setTarget(target);
	}

	/**
	 * Follow another movable
	 */
	public void follow() {
		if (getTarget() != null) {
			LogHelper.getLogger().info(
					getName() + "is following now " + getTarget().getName());
			if (getFollowMode().equals(FollowMode.FOLLOW)
					|| getFollowMode().equals(FollowMode.AUTOMATIC_FOLLOW)) {
				ways.clear();
				ways.add(getShortestPathToTarget()); // before we follow, we
														// need a path to the
														// player
				ways.getLast().removeLast(); // The field on which the player
												// stays if not yet added...
			}
			highlightPath();
			LogHelper.getLogger().info("Following path: " + ways.toString());
		}
	}

	/**
	 * Follow another movable
	 * 
	 * @param target
	 *            The movable to follow
	 */
	public void follow(Movable target) {
		this.setTarget(target);
		this.follow();
	}

	/**
	 * Intercept another movable
	 * 
	 * @param target
	 *            The movable to intercept
	 */
	public void intercept(Movable target) {
		this.setTarget(target);
		this.intercept();
	}

	/**
	 * Check wether this unit is in follow mode
	 * 
	 * @return {@code true} if the unit is in follow mode, {@code false}
	 *         otherwise
	 */
	public boolean isFollowMode() {
		return this.getFollowMode().equals(FollowMode.FOLLOW);
	}

	/**
	 * Check wether this unit is in intercept mode
	 * 
	 * @return {@code true} if the unit is in intercept mode, {@code false}
	 *         otherwise
	 */
	public boolean isInterceptionMode() {
		return this.getFollowMode().equals(FollowMode.INTERCEPT);
	}

	/**
	 * Check wether this unit has any target
	 * 
	 * @return {@code true} if the unit has any target, {@code false} otherwise
	 */
	public boolean hasAnyTarget() {
		return (getTarget() != null);
	}

	/**
	 * Recalculate the path used to intercept another unit
	 */
	public void intercept() {
		if (this.getFollowMode().equals(FollowMode.INTERCEPT)
				&& this.getTarget() != null) {
			ways.clear();
			ways.add(getShortestPathToTarget()); // before we follow, we need a
													// path to the player
			ways.getLast().removeLast(); // The field on which the player stays
											// if not yet added...
		}
	}

	/** Predefined movement speed */
	public float speed = 1f;
	/**
	 * The {@link MotionTrack} event handler callback set this variable to track
	 * <b>if</b> a {@link Movable} is currently moving.
	 */
	public boolean isMoving = false;

	/**
	 * Make this Movable follow a given path
	 * 
	 * @param path
	 *            a list of Field references to travel along
	 * @param loop
	 *            repeat animation, or just play it once
	 */
	public void followPath(List<Field> path, boolean loop) {
		if (path == null)
			return;
		if (path.size() < 1)
			return;

		MotionPath motionPath = new MotionPath();
		motionPath.setCycle(loop);

		for (Field f : path) {
			motionPath.addWayPoint(f.getLocalTranslation());
		}

		// Callback track via an instance variable
		// if this movable node is currently moving.
		MotionTrack motionControl = new MotionTrack(this, motionPath) {
			@Override
			public void onPlay() {
				isMoving = true;
			}

			@Override
			public void onStop() {
				isMoving = false;
			}
		};

		motionControl.setDirectionType(MotionTrack.Direction.PathAndRotation);
		motionControl.setRotation(new Quaternion().fromAngleNormalAxis(
				FastMath.PI, Vector3f.UNIT_Y));
		motionControl.setInitialDuration(path.size() * .4f / speed);

		if (loop == true) {
			motionControl.setLoopMode(LoopMode.Loop);
		}
		motionControl.play();
	}

	/**
	 * Follow a given path
	 * 
	 * @param tpf
	 *            Time per frame as reported by the game enginge
	 */
	public void walkPath(float tpf) {
		HexMap hexMap = Game.getInstance().getHexMap();

		// AUSGANG - keine felder mehr in der wegplanung
		if (ways.isEmpty()) {
			isMoving = false;
			state = PlayerStates.REACHED;
			getField().unit = this;
			return;
		}

		LinkedList<Field> way = ways.getFirst();

		if (info.movement == 0) {
			// AUSGANG - Einheit hat keine Bewegungspunkte mehr
			isMoving = false;
			if (way.isEmpty())
				ways.removeFirst();
			state = PlayerStates.REACHED;
			getField().unit = this;
			return;
		}
		if (way.isEmpty()) {
			ways.removeFirst();
			return;
		}
		Field field = getField();
		Field target = way.getFirst();

		if (!hexMap.isFieldAvailableForUnit(target, this)) {
			// AUSGANG - n�chstes Feld ist nicht begehbar
			isMoving = false;
			state = PlayerStates.REACHED;
			getField().unit = this;
			return;
		}
		Vector3f p1 = getLocalTranslation();
		p1.y = 0;
		Vector3f p2 = target.getLocalTranslation();
		p2.y = 0;

		Vector3f d = p2.subtract(p1);

		// Rotate player towards the next field.
		lookAt(target.getLocalTranslation(), Vector3f.UNIT_Y);

		// we reached a new field
		if (d.length() < 0.1) {
			if(!Game.getInstance().fogOfWar.equals(FogMode.FOG_OFF)){
				updateVisibleArea();
				Game.getInstance().updateVisibleUnits();
			}
			resetPrevFieldOnMiniMap(target);
			Game.getInstance().updateMinimap();
			
			setLocalTranslation(target.getLocalTranslation());
			way.removeFirst();
			field.unselect("path");

			info.movement--;
		} else if (isMoving) {
			d.normalize();
			Vector3f step = d.mult(tpf * 3f);
			step = (step.length() > 1f) ? step.normalize() : step;
			setLocalTranslation(p1.add(step));
		}
	}
	
	private void resetPrevFieldOnMiniMap(Field target){
		Game game=Game.getInstance();
		game.miniMap.resetHex(lastposI, lastposJ);
		lastposI=target.iIndex;
		lastposJ=target.jIndex;
	}

	/**
	 * Display this unit as selected
	 * 
	 * @throws IOException
	 * @throws StatusError
	 * @throws ProtocolError
	 */
	public void select() throws IOException, StatusError, ProtocolError {
		Game game = Game.getInstance();
		HudScreenController hud = (HudScreenController) game.getNifty()
				.getScreen("hud").getScreenController();

		LogHelper.getLogger().info("Select unit:" + info);

		if (!state.equals(PlayerStates.END)) {
			attachChild(game.unitOptions);
			game.unitOptions.unfoldOptions(getState());
		}

		highlightPath();
		highlightRange(getReachableFields(info.utype.maxmovement));

		updateStats();

		this.attachChild(selectionBorder);
		healthBar.setLocalTranslation(0f, 3.1f, 0f);
		this.attachChild(healthBar);

		hud.showActions(this);
	}
	
	public boolean hasNeighbours(){
		for(Field f : getField().getNeighbours())
			if(f.unit!=null)
				return true;
		return false;
				
	}

	/**
	 * Remove any selection mark from this Field.
	 */
	public void unselect() {
		Game game = Game.getInstance();
		HexMap hexMap = game.getHexMap();
		HudScreenController hud = (HudScreenController) game.getNifty()
				.getScreen("hud").getScreenController();
		// game.selectedUnit=null;

		if (selectionBorder.getParent() != null) {
			selectionBorder.removeFromParent();
		}
		if (healthBar.getParent() != null) {
			healthBar.removeFromParent();
		}

		hexMap.clearLayer("path");
		hexMap.clearLayer("range");

		hud.hideActions();
	}

	/**
	 * Highlights all adjacent fields. TODO: Remove or add properly
	 * 
	 * @param flag
	 *            true means select the fields, false means unselect the fields.
	 * @throws Exception
	 */
	public void highlightAdjacentFields(Field field, boolean flag) {
		// try {
		// List<Field> neighbourFields = field.getNeighbours();
		// for (int i = 0; i < neighbourFields.size(); i++) {
		// if (flag)
		// neighbourFields.get(i).select();
		// else
		// neighbourFields.get(i).unselect();
		// }
		// } catch (Exception e) {
		// LogHelper.getLogger().error(e.getMessage());
		// }

	}

	/**
	 * Get the last path of this movable
	 * 
	 * @return List of fields lying on the last path
	 */
	public LinkedList<Field> getLastPath() {
		return lastPath;
	}

	/**
	 * Set the last path of this movable
	 * 
	 * @param lastPath
	 *            List of fields to add as last path
	 */
	public void setLastPath(LinkedList<Field> lastPath) {
		this.lastPath = lastPath;
	}

	/**
	 * Get the follow mode of this unit
	 * 
	 * @return The {@link FollowMode} currently set for this unit
	 */
	public FollowMode getFollowMode() {
		return followMode;
	}

	/**
	 * Set the follow mode of this unit
	 * 
	 * @return The {@link FollowMode} to set for this unit
	 */
	public void setFollowMode(FollowMode followMode) {
		if (followMode.equals(FollowMode.AUTOMATIC_FOLLOW)
				|| followMode.equals(FollowMode.FOLLOW)) {
			this.ways.clear();
			this.ways.add(this.getShortestPathToTarget());
		}
		this.followMode = followMode;
	}

	/**
	 * Get the shortest path to this movable's target
	 * 
	 * @return List of fields lying on the shortest path to this movable's
	 *         target
	 */
	public LinkedList<Field> getShortestPathToTarget() {
		if (this.getTarget() == null) {
			return null;
		} else {
			return new Dijkstra(this.getField(), this.getTarget().getField())
					.getShortestPath();
		}
	}

	/**
	 * Check if this movable is currently moving
	 * 
	 * @return {@code true} if this movable is moving, {@code false} otherwise
	 */
	public boolean isMoving() {
		return this.state.equals(PlayerStates.MOVE);
	}

	/**
	 * Get this movables {@link Inventory}
	 * 
	 * @return This movable's inventory
	 */
	public Inventory getInventory() {
		return info.cargo;
	}

	/**
	 * Set the marker displayed above this movable
	 * 
	 * @param m
	 *            The marker to use
	 */
	public void setMarker(Node m) {
		this.marker.attachChild(m);
	}

	/**
	 * Clear any marker set to be displayed above this movable
	 */
	public void clearMarker() {
		this.marker.detachAllChildren();
	}

	/**
	 * Get the current state of this movable
	 * 
	 * @see PlayerStates
	 * @return The state of this movable
	 */
	public PlayerStates getState() {
		return state;
	}

	/**
	 * Set the current state of this movable
	 * 
	 * @see PlayerStates
	 * @param state
	 *            The state to set
	 */
	public void setState(PlayerStates state) {
		this.state = state;
	}

	/**
	 * Get the target of this movable
	 * 
	 * @return This movable's target
	 */
	public Movable getTarget() {
		return target;
	}

	/**
	 * Get the current move mode of this movable
	 * 
	 * @see PlayerStates
	 * @return The state of this movable
	 */
	public PlayerStates getMoveMode() {
		return moveMode;
	}

	/**
	 * Set the current move mode of this movable
	 * 
	 * @see PlayerStates
	 * @param moveMode
	 *            The mode to set
	 */
	public void setMoveMode(PlayerStates moveMode) {
		this.moveMode = moveMode;
	}

	/**
	 * Set this movables moving state
	 * 
	 * @param isMoving
	 *            {@code true} set this unit moving, {@code false} otherwise
	 */
	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	/**
	 * Decide wether this movable can still act
	 * 
	 * @param isMoving
	 *            {@code true} if it can still act, {@code false} otherwise
	 */
	public boolean canStillAct() {
		Movable activeUnit = Game.getInstance().getActiveUnit();

		if (this.state == PlayerStates.END)
			return false;
		if (activeUnit != null && activeUnit != this)
			return false;
		return true;
	}

	/**
	 * Determine if any other movable is positioned on a field neighboring this
	 * movable's field
	 * 
	 * @return {@code true} if there is any neighboring unit, {@code false}
	 *         otherwise
	 */
	public boolean isAnyUnitInRange() {
		for (Field f : getField().getNeighbours()) {
			if (f.getUnitOn() != null)
				return true;
		}
		return false;
	}

	/**
	 * Depending on the action associated with this mouse-click either select
	 * this unit, attack this unit, initiate trade with it, or even follow it. <br>
	 * <b>Note:</b> Neither trade nor follow are currently implemented.
	 */
	@Override
	public void onClick(String name) throws IOException, StatusError, ProtocolError {
		Game game = Game.getInstance();

		if (name.equals("select")) {
			switch (game.getSelectionMode()) {
				case ANY:
					if (!game.isRunning()) {
						game.getHud().logConsole("The game has already been finished...");
						return;
					}
					if (game.getPlayer() != game.getActivePlayer()) {
						game.getHud().logConsole("Sorry, it's not your turn.");
					}
					else if (info.owner == game.getActivePlayer()) {
						game.setSelectedUnit(this);
						game.getHud().updateInventoryDisplay();
						game.getHud().logConsole("You selected your unit: " + game.getSelectedUnit().getName() + ".");
					}
					break;
				case FIGHT:
					// Rotate towards the enemy
					game.getSelectedUnit().lookAt(this.getLocalTranslation(), Vector3f.UNIT_Y);
					game.getSelectedUnit().attack(this);
					game.addExplosion(new Explosion());
					break;
				case TRADE:
					// Rotate towards trade partner
					game.getSelectedUnit().lookAt(this.getLocalTranslation(), Vector3f.UNIT_Y);
					trade();
					game.getHud().logConsole("Trading with \"" + game.getSelectedUnit().getName() + "\".");
					break;
				case FOLLOW:
					// TODO: NOT YET IMPLEMENTED
					break;
			}
		}
	}

}
