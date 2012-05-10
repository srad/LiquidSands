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

package de.frankfurt.uni.vcp.game;

import static com.jme3.math.FastMath.sqrt;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionTrack;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.PointLight;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import de.frankfurt.uni.vcp.Clickable;
import de.frankfurt.uni.vcp.audio.GameMusic;
import de.frankfurt.uni.vcp.audio.Sounds;
import de.frankfurt.uni.vcp.audio.enums.SoundType;
import de.frankfurt.uni.vcp.config.MapConfig;
import de.frankfurt.uni.vcp.enums.FogMode;
import de.frankfurt.uni.vcp.enums.PlayerStates;
import de.frankfurt.uni.vcp.enums.SelectionMode;
import de.frankfurt.uni.vcp.enums.SpatialTypes;
import de.frankfurt.uni.vcp.factories.SpatialFactory;
import de.frankfurt.uni.vcp.gui.controllers.Help2ScreenController;
import de.frankfurt.uni.vcp.gui.controllers.HudScreenController;
import de.frankfurt.uni.vcp.gui.controllers.MapScreenController;
import de.frankfurt.uni.vcp.gui.controllers.PlayerScreenController;
import de.frankfurt.uni.vcp.gui.controllers.ScenarioScreenController;
import de.frankfurt.uni.vcp.gui.controllers.ServerScreenController;
import de.frankfurt.uni.vcp.gui.controllers.SettingsScreenController;
import de.frankfurt.uni.vcp.gui.controllers.StartScreenController;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.listeners.KeyBoardActionListener;
import de.frankfurt.uni.vcp.listeners.KeyBoardAnalogListener;
import de.frankfurt.uni.vcp.listeners.MouseActionListener;
import de.frankfurt.uni.vcp.net.GameInfo;
import de.frankfurt.uni.vcp.net.MessageInfo;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.net.TCPClient;
import de.frankfurt.uni.vcp.net.TypeInfo;
import de.frankfurt.uni.vcp.net.UnitInfo;
import de.frankfurt.uni.vcp.nodes.ActionOptions;
import de.frankfurt.uni.vcp.nodes.DesertHeightMap;
import de.frankfurt.uni.vcp.nodes.Explosion;
import de.frankfurt.uni.vcp.nodes.Field;
import de.frankfurt.uni.vcp.nodes.HexMap;
import de.frankfurt.uni.vcp.nodes.markers.UnitMarker;
import de.frankfurt.uni.vcp.nodes.movables.Camel;
import de.frankfurt.uni.vcp.nodes.movables.Carpet;
import de.frankfurt.uni.vcp.nodes.movables.Jinn;
import de.frankfurt.uni.vcp.nodes.movables.Movable;
import de.frankfurt.uni.vcp.nodes.movables.Rosa;
import de.frankfurt.uni.vcp.nodes.movables.Sphinx;
import de.frankfurt.uni.vcp.units.Inventory;
import de.frankfurt.uni.vcp.units.Player;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Button;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>
 * Actual implementation of the {@code SimpleApplication} class provided by the
 * jMonkeyEngine
 * </p>
 * 
 * <p>
 * This class contains all the game logic
 * </p>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class Game extends SimpleApplication {

    public boolean quickTest = false;

    // CONSTANTS
    static final float SQRT_3_2 = sqrt(3) / 2f;
    static final float UPDATE_INTERVAL = 1f;

    /** Holds persistent game settings. */
    private GameSettings gameSettings;

    /** The players name on this client. */
    private String playerName;

    /** The player id returned from the server. */
    private String playerId;

	/**
	 * Used to determine who was the last player and to compare if the player on
	 * this client has the turn. Makes more sense with >2 players, which the game
	 * was originally designed for.
	 */
    private Player lastPlayer;

    /** Increments when each round is finished, is returned by the server. */
    private int currentTurn;

    /** File name for settings file */
    static private final String SETTINGS_FILENAME = "settings.dat";

    /**
     * List of directories used by the game to locate needed resources such as
     * images, textures and models
     */
    static final String[] RESOURCE_LOCATIONS = { "data/textures",
        "data/maps/images", "data/meshes", "data/fonts" };

    /** Current game only reachable via singleton */
    private static Game instance = null;

    /** Holds the main structure information */
    public MapConfig terrainMap;

    /** Holds information about all units */
    public MapConfig unitMap;

    /** The hexmap node */
    private HexMap hexMap;

    /** Represent the image that is generated as an overview for the current map. */
    public MiniMap miniMap;

    /** Different unit types. */
    public HashMap<String, TypeInfo> unitTypes = new HashMap<String, TypeInfo>();

    /** All players */
    public HashMap<String, Player> players = new HashMap<String, Player>();

    /** Random player id for test */
    private String dummyPlayerId;

    /** Currently clicked unit */
    public Movable selectedUnit;

    /** Currently selected unit by {@link #activePlayer} */
    public Movable activeUnit;

    /** Game time timer */
    private float time = 0f;

    /** Toggles the {@link #simpleUpdate(float)} execution */
    private boolean running = false;

    /** Reference to the nifty gui */
    private Nifty nifty;

    /** Controller for the first start screen */
    private StartScreenController startScreen;

    /** Settings nifty controller for settings screen */
    private SettingsScreenController settingsScreen;

    /** The controller for the screen that describes the game scenario */
    private ScenarioScreenController scenarioScreen;

    /** The in game screen */
    private HudScreenController hud;
    
    /** This is the screen showing "help" infos. Like the hot keys. */
    private Help2ScreenController helpScreen;

    /** The screen where the user select the map */
    private MapScreenController mapScreen;

    /** Screen where to add players */
    private PlayerScreenController playerScreen;

    /** Screen where to select the game on the server */
    public ServerScreenController serverScreen;

    private Spatial hitPoint;

    private Node clickables = new Node("Clickables");

    public FogMode fogOfWar = FogMode.FOG_DYNAMIC;

    /** Node which holds the player button above the uni for specific actions */
    public ActionOptions unitOptions = null;

    /** This is the game music */
    private GameMusic gameMusic;
    
    /** This sound is player if a fight took place. */
    private Sounds explosionSound;

	/**
	 * Used for the mouse over event, TODO: this have been removed due to better
	 * game design. Remove if necessary.
	 */
    public Movable mouseOverUnit = null;
    
    private SelectionMode selectionMode = SelectionMode.ANY;

    /** The actial client that handles the player aktion to the server */
    private TCPClient client = new TCPClient("localhost", 1504,	TCPClient.CLIENT_INFO);

    /** The id of the current map this client plays on. */
    private String mapId = "";
    
    /** The game id which this client joined. */
    private String gameId = "";
    
    /** Hold the explosion node. */
    private Explosion explosion = null;

    /** Logical game information provided by tcp client-server */
    public GameInfo gameInfo;

    // CONSTRUCTOR
    private Game() throws IOException, ClassNotFoundException {
        this.gameSettings = getGameSettings();
        // Thread resumes even if the window has no focus.
        setPauseOnLostFocus(false);
    };

    /** Heightmap for the hexmap */
    public DesertHeightMap map = null;
    public Cinematic cinematic = new Cinematic(rootNode, 5);
    // STATIC
    /**
     * Get the instance of the currently running game. If no game is running,
     * start a new one.
     * 
     * @return Reference to the current game
     */
    public static Game getInstance() {
        if (instance == null) {
            try {
                instance = new Game();
            } catch (Exception e) {
                LogHelper.getLogger().error("getInstance(): " + e.getMessage());
            }
        }
        return instance;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * Get the {@link HexMap} used by this gamee
     * 
     * @return The {@code MapConfig} of this game
     */
    public HexMap getHexMap() {
        return hexMap;
    }

    /**
     * Get the {@link MapConfig} used by this gamee
     * 
     * @return The {@code MapConfig} of this game
     */
    public MapConfig getMapConfig() {
        return terrainMap;
    }

    public TypeInfo getUnitType(String name) {
        return unitTypes.get(name);
    }

    /**
     * Returns the player who plays on this client.
     * @return
     */
    public Player getPlayer() {
        return getPlayer(getPlayerName());
    }

    public Player getActivePlayer() {
        return gameInfo.activeplayer;
    }

    /**
     * Add another player to this game
     * 
     * @param playerName
     *            The name chosen by the player
     * @param playerId
     *            The id given to this player by the server
     */
    public void addPlayer(String playerName, String playerId) {		
        Player player = new Player(playerName, playerId);
        player.number = players.size();

        LogHelper.getLogger().info("Adding player: " + player.playerName);

        players.put(playerName, player);

        clickables.attachChild(player);

        if (playerId == null)
            return;

        dummyPlayerId = playerId;
    }

	/**
	 * <p>
	 * Overloads the method {@link #addPlayer(String, String)} for convenience
	 * to simply add a player to the game with given name.
	 * </p>
	 * 
	 * @param playerName
	 */
    public void addPlayer(String playerName) {
        try {
            this.playerName = TCPClient.sanitize(getPlayerName());
            this.playerId = getClient().addplayer(getGameId(), playerName);

            addPlayer(playerName, this.playerId);
        } catch (Exception e) {
            LogHelper.getLogger().error("addPlayer(): " + e.getMessage());
        }
    }

    /**
     * Get the player with a given name
     * 
     * @param name
     *            The name of the player
     * @return The player, or null if no player with this name exists
     */
    public Player getPlayer(String name) {
        if (name == null) {
            return null;
        }
        return players.get(name);
    }

    /**
     * Get a collection of all players currently participating in the game
     * 
     * @return The players
     */
    public Collection<Player> getPlayers() {
        return players.values();
    }

	/**
	 * Distribute a set of units given by a unitmap to the players actually
	 * participation in the game
	 * 
	 * @param unitMap
	 *            The unimap
	 * @throws Exception
	 */
	public void distributeUnits() throws Exception {
		try {
			int unitId;

			for (int i = 0; i < unitMap.height; ++i) {
				for (int j = 0; j < unitMap.width; ++j) {
					unitId = unitMap.csv[j][i];

					if (unitId != 0) {

						UnitInfo info = client.unitinfo(gameId, dummyPlayerId, unitId);
						Player player = info.owner;

						if (player.playerId != null) {
							info = client.unitinfo(gameId, player.playerId, unitId);
						}

						LogHelper.getLogger().info("Adding unit: " + info);
						Field field = hexMap.getField(i, j);

						Movable unit;
						if (info.utype.name.equals("fightersmall"))
							unit = new Rosa(info);
						else if (info.utype.name.equals("fightermedium"))
							unit = new Sphinx(info);
						else if (info.utype.name.equals("fighterheavy"))
							unit = new Jinn(info);
						else if (info.utype.name.equals("cargosmall"))
							unit = new Carpet(info);
						else if (info.utype.name.equals("cargoheavy"))
							unit = new Camel(info);
						else {
							LogHelper.getLogger().info("No model for unittype: " + info.utype + " using default model.");
							unit = new Rosa(info);
						}

						unit.setMarker(new UnitMarker(
								getPlayerPosition(player.playerName)));
						player.addUnit(field, unit);
					}
				}
			}
		} catch (Exception e) {
			LogHelper.getLogger().error("distributeUnits(): " + e.getMessage());
		}
	}

    /**
     * <p>
     * Returns the current position of the given player in the global player
     * list from the server.
     * </p>
     * 
     * <p>
     * This must be different from the local hash map positions in
     * {@link #players}, because each client add himself first to the list.
     * </p>
     * @throws Exception 
     */
    private int getPlayerPosition(String playerName) throws Exception {
        int playerPosition = gameInfo.playernames.indexOf(playerName);

        if (playerPosition < 0) {
            throw new Exception("Player not found");			
        }
        return playerPosition;
    }

    public GameInfo getGameInfo() throws IOException, StatusError, ProtocolError {
        gameInfo = client.gameinfo(gameId, dummyPlayerId);
        return gameInfo;
    }

    /**
     * Triggers the trade action along the network.
     * @param from The unit who wants to trade.
     * @param to The unit with which shall be traded.
     * @param give {@link Inventory} object of the goods which the <b>from</b> player wants to give away.
     * @param get {@link Inventory} object of the goods that the player wants to have.
     * @throws IOException
     * @throws StatusError
     * @throws ProtocolError
     */
    public void trade(Movable from, Movable to, Inventory give, Inventory get) throws IOException, StatusError, ProtocolError {
        client.trade(gameId, gameInfo.activeplayer.playerId, from.info.unitid, to.info.unitid, give, get);
    }

    public void tradereply(String playerId, String response) throws IOException, StatusError, ProtocolError {
        client.tradereply(playerId, gameId, response);
    }

    /**
     * <p>This is an ugly fix for the units, which are reset on their actual position.</p>
     * <p>The are some situations we need to call this method, unfortunately.</p>
     * <p>The usage of this method could be redundant in the future.</p>
     */
    public void fixUnitMap() {
        try {
            int unitId;
            unitMap = new MapConfig(client.unitmap(gameId, dummyPlayerId));

            for (int i = 0; i < unitMap.height; ++i) {
                for (int j = 0; j < unitMap.width; ++j) {
                    unitId = unitMap.csv[j][i];
                    if (unitId != 0) {
                        UnitInfo info = client.unitinfo(gameId, dummyPlayerId, unitId);
                        LogHelper.getLogger().info("Fixing unit position for unit: " + info);

                        Field field = hexMap.getField(i, j);
                        Movable m = getUnit(unitId);

                        m.getField().unit=null;
                        m.setLocalTranslation((field.getLocalTranslation()));
                        field.unit=m;
                    }
                }
            }
            updateVisibleUnits();
        }
        catch (Exception e) {
        	LogHelper.getLogger().error("fixUnitMap(): " + e.getMessage());
        }
    }

	/**
	 * End the current players turn and activate the next player
	 */
	public void nextPlayer() {
		try {
			// don't end this move, until the player has acted with a unit.
			if (activeUnit == null) {
				return;
			}

			activeUnit.setState(PlayerStates.END);
			activeUnit.detachChildNamed("options");

			if (selectedUnit != null) {
				selectedUnit.unselect();
				selectedUnit = null;
			}
			
			client.endturn(playerId, activeUnit.info.unitid, gameId);
			gameInfo = client.gameinfo(gameId, dummyPlayerId);
			hud.logConsole("It's \"" + gameInfo.activeplayer.playerName + "\"s turn.");
			hud.disableEndTurnButton();
			lastPlayer = null;
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
			hud.errorMessage("nextPlayer(): " + e.getMessage());
		}
		LogHelper.getLogger().info("waiting for next turn ...");
		updateVisibleUnits();
	}

    public void updateFog(){
        for (Player p : players.values()) {
            if (p != getActivePlayer())
                for (Movable m : p.units) {
                    m.enableFog();
                }
            else {
                for (Movable m : p.units) {
                    m.disableFog();
                }
            }
        }
        updateVisibleUnits();
    }

    public void updateVisibleUnits() {
        for (Player p : players.values()) {
            if (p != getPlayer()) {
                for (Movable m : p.units) {
                    if (m.getField().getViewers() == 0 && !fogOfWar.equals(FogMode.FOG_OFF))
                        m.setCullHint(CullHint.Always);
                    else
                        m.setCullHint(CullHint.Dynamic);
                }
            }
            else {
                for (Movable m : p.units) {
                    if (m.getField().getViewers() != 0)
                        m.setCullHint(CullHint.Dynamic);
                }
            }
        }
    }

    /**
     * Refresh every players units
     * 
     * @throws IOException
     * @throws StatusError
     * @throws ProtocolError
     */
    public void nextTurn() throws IOException, StatusError, ProtocolError {
        LogHelper.getLogger().info("Next Turn");

        refreshUnits();
        if (selectedUnit != null) {
            selectedUnit.highlightPath();
        }
    }
    
    /**
     * Updates all units data by getting them from the server.
     * @throws IOException
     * @throws StatusError
     * @throws ProtocolError
     */
    private void refreshUnits() throws IOException, StatusError, ProtocolError {
        for (Player p : players.values()) {
            p.refreshUnits();
        }
    }

    /**
     * Get the unit currently selected
     * 
     * @return The selected unit, or {@code null} if no unit is selected
     */
    public Movable getSelectedUnit() {
        return selectedUnit;
    }

    /**
     * Set the unit currently selected
     * 
     * @param unit
     *            The unit to select
     */
	public void setSelectedUnit(Movable unit) {
		try {
			if (selectedUnit != null) {
				selectedUnit.unselect();
			}
			unit.select();
			selectedUnit = unit;
		} catch (Exception e) {
			hud.errorMessage("setSelectedUnit(): " + e.getMessage());
		}
	}

    /**
     * Get the active unit of the current player
     * 
     * @return The active unit, or {@code null} if no unit is active
     */
    public Movable getActiveUnit() {
        return activeUnit;
    }

    /**
     * Set the active unit of the current player
     * 
     * @param unit
     *            The unit to be set as active unit
     */
    public void setActiveUnit(Movable unit) {
        Button b = hud.getScreen().findNiftyControl("nextRoundButton", Button.class);

        activeUnit = unit;
        if (activeUnit == null) {
            b.disable();
        }
        else {
            b.enable();
        }
    }

    public Movable getUnit(int unitid) {
        for (Player p : players.values()) {
            for (Movable m : p.units) {
                if (m.info.unitid == unitid) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Get the object the player clicked upon
     * 
     * @return The unit clicked upon, or {@code null} if no unit was hit
     */
    public Clickable pick() {
        Clickable result;

        Vector2f click2d = getInputManager().getCursorPosition();
        Vector3f position = getCamera().getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f direction = getCamera()
                .getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f)
                .clone().subtractLocal(position);

        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(position, direction);

        clickables.collideWith(ray, results);
        if (results.size() > 0) {
            Geometry geometry = results.getClosestCollision().getGeometry();
            hitPoint.setLocalTranslation(results.getClosestCollision()
                    .getContactPoint());
            result = (Clickable) geometry.getParent().getParent();
            return result;
        }

        return null;
    }

    // NETWORKING
    /**
     * Set the {@link TCPClient} used by this game
     * 
     * @param client
     *            The client to be set
     */
    public void setClient(TCPClient client) {
        this.client = client;
    }

    /**
     * Get the {@link TCPClient} used by this game
     * 
     * @return The client of this game
     */
    public TCPClient getClient() {
        return client;
    }

    /**
     * Set the id of this game
     * 
     * @param gameId
     *            The id to be set
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Get the id of this game
     * 
     * @return This games id
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Set the map id of this game
     * 
     * @param mapId
     *            The map id
     */
    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    /**
     * Get the map id of this game
     * 
     * @return The map id
     */
    public String getMapId() {
        return mapId;
    }

    /**
     * Get a player id usable for requests used by the game engine acting on
     * itself
     * 
     * @return A usable player id
     */
    public String getDummyPlayerId() {
        return dummyPlayerId;
    }

    // LISTENERS
    /**
     * Adds mouse events and its handlers.
     */
	private void initKeys() {
		try {
			inputManager.deleteMapping("FLYCAM_ZoomIn");
			inputManager.deleteMapping("FLYCAM_ZoomOut");
			inputManager.deleteMapping("SIMPLEAPP_Exit");

			inputManager.addMapping("select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
			inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

			// TODO: Because of the interfering key mapping in the chat window and tha Game, I dectivate
			// the game key bindding for now.
			inputManager.addMapping("undo", new KeyTrigger(KeyInput.KEY_U));
			inputManager.addMapping("redo", new KeyTrigger(KeyInput.KEY_R));
			inputManager.addMapping("fight", new KeyTrigger(KeyInput.KEY_F));
			inputManager.addMapping("trade", new KeyTrigger(KeyInput.KEY_T));

			inputManager.addMapping("shift", new KeyTrigger(KeyInput.KEY_LSHIFT));

			//inputManager.addMapping("next", new KeyTrigger(KeyInput.KEY_N));
			//inputManager.addMapping("pass", new KeyTrigger(KeyInput.KEY_P));
			//inputManager.addMapping("automate", new KeyTrigger(KeyInput.KEY_X));

			//inputManager.addMapping("test", new KeyTrigger(KeyInput.KEY_T));

			// Players camera movement
			inputManager.addMapping("left", new KeyTrigger(gameSettings.getLeftKey()));
			inputManager.addMapping("right", new KeyTrigger(gameSettings.getRightKey()));
			inputManager.addMapping("up", new KeyTrigger(gameSettings.getUpKey()));
			inputManager.addMapping("down", new KeyTrigger(gameSettings.getDownKey()));

			inputManager.addListener(new MouseActionListener(), new String[] { "select", "move" });
			inputManager.addListener(new KeyBoardActionListener(), new String[] { "undo", "redo", "shift", "next", "pass", "automate", "fight",
					"trade", "test" });
			inputManager.addListener(new KeyBoardAnalogListener(), new String[] { "left", "right", "up", "down" });

			LogHelper.getLogger().info("Initialized input listeners");
		} catch (Exception e) {
			LogHelper.getLogger().error("initKeys(): " + e.getMessage());
		}
	}

    // HELPER FUNCTIONS
    /**
     * fix<b>Me</b> would have been a better name to choose, it seems. Due to
     * some unfixed regression shadows still won't be rendered correct
     */
    private void fixShadows() {
        getViewPort().getQueue().setGeometryComparator(Bucket.Transparent,
                new GeometryComparator() {
            @Override
            public int compare(Geometry o1, Geometry o2) {
                if (o1.getName().equals("shadow")
                        || o1.getName().equals("selection-border"))
                    return -1;
                return 0;
            }

            @Override
            public void setCamera(Camera arg0) {
            }
        });
    }

    /**
     * Add a {@link DesertHeightMap} to this game The heightmap will be
     * generated from the map config associated with this game
     */
    private void addHeightMap() {
        try {
            map = new DesertHeightMap(terrainMap, assetManager);
            rootNode.attachChild(map);
            LogHelper.getLogger().info("Attached desert height map");
        } catch (IOException e) {
            LogHelper.getLogger().error("addHeightMap(): " + e.getMessage());
        }
    }

    /**
     * Add a {@link HextMap} to this game The map will be generated from the map
     * config associated with this game
     */
    public void addHexMap() {
        hexMap = (new HexMap(terrainMap));
        clickables.attachChild(getHexMap());
        LogHelper.getLogger().info("Attached hex map");
    }

    /**
     * Move the camera, so we have a nice viewing perspective
     */
    private void adjustCameraPosition() {
        cam.setLocation(new Vector3f(-6, 15f, 4.5f));
        cam.lookAt(new Vector3f(-6, 0, -5.5f), new Vector3f(0f, 0f, -1f));

        LogHelper.getLogger().info("Adjusted camera");
    }

    /**
     * Attaches a skybox to the root node.
     */
    public void addSkyBox() {
        Texture west, east, north, south, up, down;

        west = assetManager.loadTexture("skybox/dunes/dunes_left.JPG");
        east = assetManager.loadTexture("skybox/dunes/dunes_right.JPG");
        north = assetManager.loadTexture("skybox/dunes/dunes_front.jpg");
        south = assetManager.loadTexture("skybox/dunes/dunes_back.JPG");
        up = assetManager.loadTexture("skybox/dunes/dunes_top.jpg");
        down = assetManager.loadTexture("skybox/dunes/dunes_bottom.jpg");

        rootNode.attachChild(SkyFactory.createSky(assetManager, west, east,
                north, south, up, down));

        LogHelper.getLogger().info("Added sky box");
    }

    /**
     * Start the game
     * 
     * @throws Exception
     */
    public void startgame() throws Exception {
        LogHelper.getLogger().info("Game started");

        // MAPDATA
        LogHelper.getLogger().info("Loading mapdata");

        // TERRAINMAP
        terrainMap = new MapConfig(client.terrainmap(gameId, playerId));

        LogHelper.getLogger().info("Loaded terrainmap:\n " + terrainMap);

        LogHelper.getLogger().info("Generating heightmap");
        addHeightMap();
        LogHelper.getLogger().info("Adding hexmap");
        addHexMap();
        miniMap = new MiniMap(terrainMap);

        // UNITMAP
        unitMap = new MapConfig(client.unitmap(gameId, playerId));

        LogHelper.getLogger().info("Loaded unitmap:\n" + unitMap);

        // FILL UNITINFO DATABASE
        LogHelper.getLogger().info("Loading unit types:");
        for (TypeInfo info : client.unittype(mapId)) {
            LogHelper.getLogger().info("  adding: " + info);
            unitTypes.put(info.name, info);
        }

        this.gameInfo = client.gameinfo(gameId, dummyPlayerId);
        LogHelper.getLogger().info("Loaded gameinfo: " + gameInfo);

        // ADD PLAYERS
        for (String name : gameInfo.playernames)
            if (getPlayer(name) == null) {
                LogHelper.getLogger().info(" adding Player:" + name);
                addPlayer(name, null);
            }

        // DISTRIBUTE UNITS
        LogHelper.getLogger().info("Distributing units: " + gameInfo);
        distributeUnits();

        gameInfo = client.gameinfo(gameId, dummyPlayerId);

		for (Player p : players.values()) {
			for (Movable m : p.units) {
				miniMap.drawHex(m.getField().iIndex, m.getField().jIndex, pColors[p.number]);
				m.lastposI = m.getField().iIndex;
				m.lastposJ = m.getField().jIndex;
			}
		}
		hud.updateMiniMap();
		miniMap.saveImage();

		initFog();
		// updateMinimap();

		initKeys();
		setRunning(true);

		LogHelper.getLogger().info("Active player: " + getActivePlayer());
	}

    /**
     * Sets up the fog of war by surrounding the units range with a fog.
     */
	public void initFog() {

		for (Movable m : getPlayer().units) {
			m.fieldOfView = m.getVisibleFields(m.info.utype.maxmovement);
			m.disableFog();
		}

		updateVisibleUnits();
	}

	/** This array holds the players colors. */
	public Color[] pColors = { new Color(0.3f, 0.8f, 0.0f), new Color(0.0f, 0.6f, 0.8f), new Color(0.7f, 0.7f, 0.0f) };

	/**
	 * Redraws the mini hex map to update the player positions.
	 */
	public void updateMinimap() {
		for (Player p : players.values()) {
			for (Movable m : p.units) {
				if (m.getField().fog.isEnabled()) {
					miniMap.resetHex(m.getField().iIndex, m.getField().jIndex);
				} else
					miniMap.drawHex(m.getField().iIndex, m.getField().jIndex, pColors[p.number]);
			}
		}
		hud.updateMiniMap();
	}

	/**
	 * Removes the fog of war from the users display.
	 */
    public void disableFogOfWar(){
        fogOfWar = FogMode.FOG_OFF;
        getHexMap().clearFog();
        updateVisibleUnits();
    }

    /**
     * Brings back the fog of war dynamically.
     */
    public void enableFogOfWar(){
        fogOfWar = FogMode.FOG_DYNAMIC;
        getHexMap().fillFog();
        initFog();
    }
    
    FilterPostProcessor fpp;
    BloomFilter bloom;

	/**
	 * Initialize the game
	 */
	@Override
	public void simpleInitApp() {
		try {
			guiNode.detachAllChildren();
			cinematic.bindCamera("cam", cam);
			
			// REGISTER RESOURCES
			for (String location : RESOURCE_LOCATIONS) {
				assetManager.registerLocator(location, FileLocator.class.getName());
			}
			LogHelper.getLogger().info("Registered resources");

			initGui();
			
			initShadow();
			
			// Add bloom. TODO: Bloom messes up the render order.
			fpp = new FilterPostProcessor(assetManager);
			bloom = new BloomFilter();
			bloom.setBloomIntensity(0.4f);
			fpp.addFilter(bloom);
			
			toggleBloom();
			
			// Start the music at the beginning.
			gameMusic = new GameMusic();
			gameMusic.setLoopMode(true);
			gameMusic.setPositional(true);

			if (gameSettings.isMusicEnabled()) {
				gameMusic.play();
			}

			flyCam.setMoveSpeed(10);
			flyCam.setEnabled(false);

			inputManager.setCursorVisible(true);

			rootNode.attachChild(clickables);
			hitPoint = SpatialFactory.create(SpatialTypes.MARKER_FIELD);
			rootNode.attachChild(hitPoint);
			unitOptions = new ActionOptions();

			adjustCameraPosition();
			addSkyBox();
			fixShadows();
			addLights();

			startScreen.setServerLocation(gameSettings.getGameServer());
			startScreen.setPlayerName(gameSettings.getPlayerName());

			LogHelper.getLogger().info("Finished simple init");
		} catch (Exception e) {
			LogHelper.getLogger().error("simpleInitApp(): " + e.getMessage());
		}
	}
	
	/** Used to determine if the bloom is activated. */
	private boolean bloomEnabled = false;
	
	/**
	 * Inverts the bloom state.
	 */
	public void toggleBloom() {
		if (!bloomEnabled) {
			viewPort.addProcessor(fpp);
		}
		else {
			viewPort.removeProcessor(fpp);
		}
		bloomEnabled = !bloomEnabled;
	}

	/** Dynamic jme shadow renderer. */
	private PssmShadowRenderer pssmRenderer;

	/**
	 * Adds advanced shadows. The basic shadow class from jme leads to
	 * exception, don't ask me why.
	 */
	private void initShadow() {
	    pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 3);
	    pssmRenderer.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal()); // light direction
	    viewPort.addProcessor(pssmRenderer);
	}

	/**
	 * Add lights to the scene, presumably on the 4 corners of the Map
	 */
	public void addLights() {
		PointLight p = new PointLight();
		p.setPosition(new Vector3f(0f, 10f, 0f));
		rootNode.addLight(p);
		PointLight p2 = new PointLight();
		p2.setPosition(new Vector3f(0f, 10f, -50f));
		rootNode.addLight(p2);
		PointLight p3 = new PointLight();
		p3.setPosition(new Vector3f(-50f, 10f, -50f));
		rootNode.addLight(p3);
		PointLight p4 = new PointLight();
		p4.setPosition(new Vector3f(-50f, 10f, 0f));
		rootNode.addLight(p4);
	}

	/**
	 * Adds an explosion and play an explosion sound with correct positional audio.
	 */
    public void addExplosion(Explosion explosion) {
    	this.explosion = explosion;
    	// We play the sound in the explosion update for timing reasons.
    	this.setExplosionSound(new Sounds(this.explosion, SoundType.EXPLOSION));    	
    }
    
    /**
     * Removes an explosion at the given index.
     * @param index
     */
    public void removeExplosion() {
    	this.explosion = null;
    	this.setExplosionSound(null);
    }
    
	/**
	 * Calls each {@link #explosion} update callback. Was originally a list of
	 * {@link Explosion} objects, but had an issue with thread concurrency, so
	 * we have now only one.
	 * 
	 * @param tpf Timer per frame of the original game.
	 */
	public void updateExplosions(float tpf) {
		if (explosion != null) {
			explosion.update(tpf);
		}
    }

	@Override
	public void simpleUpdate(float tpf) {
		if (!running) {
			return;
		}

		updateExplosions(tpf);
		// This line makes the 3d sound happening by
		// attaching the listening location to the camera.
		listener.setLocation(cam.getLocation());

		// we have to wait for our next turn.
		if (getPlayer() != getActivePlayer()) {

			time += tpf;
			if (time > UPDATE_INTERVAL) {
				// Reset timer
				time = 0f;

				try {		
					// CHAT MESSAGES
					List<MessageInfo> messages = client.getdata();
					for (MessageInfo i : messages) {
						// TODO: ATTENTION WORKAROUND TO FIND THE PLAYERS NAME FOR
						// THE CHAT
						// The problem now is that we only have the player id
						// and we try
						// to match is with the playername to get the real name
						// for the chat display.
						for (String playerName : gameInfo.playernames) {
							if (i.senderplayerid.startsWith(playerName)) {
								hud.logConsole(playerName + "> " + i.message);
							}
						}
					}

					// Get latest server info
					gameInfo = client.gameinfo(gameId, playerId);

					// TRADE
					if (gameInfo.tradepartner != null) {
						if (getPlayer() == gameInfo.tradepartner) {
							if (!hud.isOfferVisible()) {
								hud.showOffer();
							}
						}
					}
				} catch (Exception e) {
					// TODO: Doesn't work, the exception is thrown somewhere else.
					// The winning condition is stupidly returned from the
					// server as error. However, it needs to be caught here.
					try {
						if (e.getMessage().equals("game_ended")) {
						    String reply = ((StatusError) e).fullMessage;
						    String infoString = reply.split(" ")[2];
						    
						    GameInfo info = new GameInfo(infoString);
						    
							hud.errorMessage("The winner is: " + info.winner);
							running = false;
							return;
						} else {
							throw new Exception(e.getMessage());
						}		
					} catch (Exception e2) {
						LogHelper.getLogger().error("simpleUpdate(): " + e.getMessage());
					}
				}
			}
		}
		// check if we have been activated. This is the wait condition until it's our turn.
		else if (lastPlayer != getPlayer()) {		
			lastPlayer = getPlayer();
			hud.logConsole("It's \"" + getActivePlayerName() + "\"s turn.");
			// Has been disabled when our turn has been finished.
			hud.enableEndTurnButton();
			fixUnitMap();

			if (gameInfo.turn > currentTurn) {
				currentTurn = gameInfo.turn;
				try {
					nextTurn();
				} catch (Exception e) {
					LogHelper.getLogger().error("simpleUpdate(): " + e.getMessage());
				}
			}

			setActiveUnit(null);
		}
		// All players units which have not been moven
		// within the current round will have a rotating marker
		// above their heads.
		for (Movable m : getActivePlayer().units) {
			if (m.canStillAct()) {
				m.marker.rotate(0f, FastMath.HALF_PI * tpf, 0f);
			}
		}

		// Rotated action buttons above the selected unit head
		unitOptions.rotate(0f, FastMath.QUARTER_PI / 4f * tpf, 0f);
		if (!unitOptions.animDone) {
			unitOptions.animateAction(tpf);
		}

		Movable unit = selectedUnit;

		if (unit == null) {
			return;
		}
		if (unit != null && unit.isMoving) {
			unit.walkPath(tpf);
			if (!unit.isMoving)
				if (unit.hasNeighbours())
					unitOptions.unfoldOptions(activeUnit.getState());
				else {
					nextPlayer();
				}
		}
		if (unit != null && unit.getState().equals(PlayerStates.REACHED)) {
			if (!unit.isAnyUnitInRange()) {
				hud.setInventoryLabel("");
				nextPlayer();
				// getHexMap().highlightNeighbourUnits(unit);
			}
		}
	}

    /**
     * TODO: not used right now, but can do a mouse over animation.
     * @param tpf
     */
	@SuppressWarnings("unused")
    private void doMouseOverAnimation(float tpf) {
        if (mouseOverUnit != null) {
            mouseOverUnit.marker.rotate(0f, tpf, 0f);
        }
    }

    public HudScreenController getHud() {
        return hud;
    }

    /**
     * Initialize the gui of the game
     */
	public void initGui() {
		assetManager.registerLocator("data/gui/", FileLocator.class.getName());
		assetManager.registerLocator("data/sounds/", FileLocator.class.getName());
		assetManager.registerLocator("data/textures/", FileLocator.class.getName());

		startScreen = new StartScreenController();
		setSettingsScreen(new SettingsScreenController());
		scenarioScreen = new ScenarioScreenController();
		hud = new HudScreenController();
		mapScreen = new MapScreenController();
		playerScreen = new PlayerScreenController();
		serverScreen = new ServerScreenController();
		helpScreen = new Help2ScreenController();

		stateManager.attach(startScreen);
		stateManager.attach(getSettingsScreen());
		stateManager.attach(scenarioScreen);
		stateManager.attach(hud);
		stateManager.attach(mapScreen);
		stateManager.attach(playerScreen);
		stateManager.attach(serverScreen);
		stateManager.attach(helpScreen);

		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);

		nifty = niftyDisplay.getNifty();

		nifty.fromXml("screen_game.xml", "start", startScreen, hud, scenarioScreen, getSettingsScreen(), mapScreen, playerScreen, serverScreen, helpScreen);

		guiViewPort.addProcessor(niftyDisplay);

		flyCam.setDragToRotate(true);
	}

    /**
     * Starts the player movement along the selected path.
     */
    public void startMovement() {
        if ((selectedUnit == null) || !selectedUnit.getState().equals(PlayerStates.START)) {
            return;
        }
        if (selectedUnit.info.movement != 0) {
            setActiveUnit(selectedUnit);
            selectedUnit.setState(PlayerStates.MOVE);
            selectedUnit.setMoving(true);
        }
    }

    /**
     * Get the game's running state
     * 
     * @return {@code true} if this game running, {@code false} otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Set this game's running state
     * 
     * @param running
     *            {@code true} to set this game running, {@code false} otherwise
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Get the {@code Nifty} instance used by this game
     * 
     * @return The {@code Nifty} instance
     */
    public Nifty getNifty() {
        return this.nifty;
    }

    /**
     * Get the selection mode used, when the user clicks a mouse button
     * 
     * @return The selection mode
     */
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    /**
     * Get the selection mode used, when the user clicks a mouse button
     * 
     * @param selectionMode
     *            The selection mode
     */
    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        logConsoleSelectionMode();
    }

    /**
     * Writes the status to the console within the hud.
     */
    private void logConsoleSelectionMode() {
    	String message = "";
    	if (this.selectionMode == SelectionMode.FIGHT) {
    		message = "You are now ready to fight, select your target.";
    	}
    	else if (this.selectionMode == SelectionMode.TRADE) {
    		message = "Your are ready to trade, select your trade partner.";
    	}
    	if (!message.isEmpty()) {
    		Game.getInstance().getHud().logConsole(message);
    	}
	}

	public SettingsScreenController getSettingsScreen() {
        return settingsScreen;
    }

    public void setSettingsScreen(SettingsScreenController settingsScreen) {
        this.settingsScreen = settingsScreen;
    }

    public GameMusic getGameMusic() {
        return gameMusic;
    }

    public void setGameMusic(GameMusic gameMusic) {
        this.gameMusic = gameMusic;
    }

    /**
     * If the file with the game settings doesn't exist it will be created.
     * 
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
	public GameSettings getGameSettings() {
		if (gameSettings != null) {
			LogHelper.getLogger().info("Game settings already exist, returning gameSettings");
			return gameSettings;
		}
		FileInputStream savedFile = null;
		try {
			savedFile = new FileInputStream(SETTINGS_FILENAME);
		} catch (FileNotFoundException e) {
			try {
				setGameSettings(new GameSettings());
				savedFile = new FileInputStream(SETTINGS_FILENAME);
			} catch (IOException IOe) {
				LogHelper.getLogger().error(
						"getGameSettings:" + IOe.getMessage());
			}
		}

		ObjectInputStream save;
		GameSettings g = null;

		try {
			save = new ObjectInputStream(savedFile);
			g = (GameSettings) save.readObject();
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
		return g;
	}

    /**
     * Saves the game settings to a file.
     * 
     * @param gameSettings
     * @throws IOException
     */
    public void setGameSettings(GameSettings gameSettings) throws IOException {
        FileOutputStream saveFile = new FileOutputStream(SETTINGS_FILENAME);
        ObjectOutputStream save = new ObjectOutputStream(saveFile);

        save.writeObject(gameSettings);
        save.close();
    }

    /**
     * Saves the current game settings.
     * 
     * @throws IOException
     */
    public void saveGameSettings() throws IOException {
    	// Save server location and player name.
        gameSettings.setGameServer(startScreen.getServerLocation());
        gameSettings.setPlayerName(getPlayerName());
        
        // Window resolution.
        gameSettings.setvResolution(settings.getHeight());
        gameSettings.sethResolution(settings.getWidth());
        
        // Setting saves also the object.
        setGameSettings(gameSettings);
    }

    /**
     * Moves the scene camera on a {@link MotionPath} from a start to an end position.
     * @param startPos Start position
     * @param endPos End position.
     */
	public void moveCam(Vector3f startPos, final Vector3f endPos) {
		MotionPath mp = new MotionPath();
		
		mp.addWayPoint(startPos);
		mp.addWayPoint(endPos);

		MotionTrack mt = new MotionTrack(cinematic.getCamera("cam"), mp) {
			@Override
			public void onUpdate(float tpf) {
				super.onUpdate(tpf);
				cam.setLocation(getSpatial().getLocalTranslation());
			}

			@Override
			public void onStop() {
				super.onStop();
				//cam.setLocation(endPos);
			}

		};
		mt.setDirectionType(MotionTrack.Direction.Path);
		mt.setInitialDuration(0.8f);
		mt.setLoopMode(LoopMode.DontLoop);
		
		mt.play();
	}

    public String getPlayerName() {
        return this.playerName;
    }
    
    /**
     * Returns the player who has the current turn.
     * @return Player name.
     */
    public String getActivePlayerName() {
    	return gameInfo.activeplayer.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

	public Sounds getExplosionSound() {
		return explosionSound;
	}

	public void setExplosionSound(Sounds explosionSound) {
		this.explosionSound = explosionSound;
	}

}
