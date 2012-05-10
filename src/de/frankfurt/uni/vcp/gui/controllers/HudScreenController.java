package de.frankfurt.uni.vcp.gui.controllers;

import java.io.IOException;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import de.frankfurt.uni.vcp.enums.FogMode;
import de.frankfurt.uni.vcp.enums.PlayerStates;
import de.frankfurt.uni.vcp.enums.SelectionMode;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.game.MiniMap;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.net.GameInfo;
import de.frankfurt.uni.vcp.net.GameInfo.TradeStatus;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.nodes.movables.Movable;
import de.frankfurt.uni.vcp.units.Inventory;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleExecuteCommandEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;

/**
 * <h3>Players man "hud" screen within the game.</h3>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class HudScreenController extends AbstractScreenController {

    
    private static final float UPDATE_INTERVAL = 1000;
    private float time;
    
    Movable to;    

    /** The trade's layer container. */
    public static String TRADE_LAYER = "tradeLayer";

    /** Panel which holds the undo redo buttons. */
    private static String UNDO_REDO_PANEL = "undoRedoPanel";

    private static String CONSOLE = "console";
    
    /** Inventory display label at the hud top. */
	private Label inventoryLabel;
	
	/** Used to show only a start message once in the hud. */
	private boolean gameHasStarted = false;
	
    private InputManager im;
    
    private Camera cam;

    public void initialize(AppStateManager stateManager, Application app) {
    	super.initialize(stateManager, app);
    	this.game = (Game) app;
    	this.cam = game.getCamera();
    	
    	im = app.getInputManager();
    	im.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL,	false));
    	im.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
    	im.addMapping("toggleFog", new KeyTrigger(KeyInput.KEY_F1));    	
    }
    
    /** Confirm button for error message. */
    public void ok() {
        screen.findElementByName("Hud_error").hide();
    }

    /**
     * Displays an error message. NOT used yet.
     * 
     * @param message
     *            Error message to show.
     */
    public void showErrorMessage(String message) {
        findElement("errorMessageLayer").show();
        Label l = (Label) findElement("errorMessageLabel");
        l.setText(message);
    }

    /**
     * Displays an error message in the main player screen, the "hud" screen.
     * 
     * @param message
     */
    public void errorMessage(String message) {
        Element layer = screen.findElementByName("Hud_error");
        layer.show();

        Label errorLabel = screen
                .findNiftyControl("hudErrorLabel", Label.class);
        LogHelper.getLogger().error(message);
        errorLabel.setText(message);
    }

    public Screen getScreen() {
        return screen;
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);
        initTradeDialog();
    }

    /**
     * Sets the users state to {@link SelectionMode#FOLLOW}.
     */
    public void intercept() {
        game.setSelectionMode(SelectionMode.FOLLOW);
        LogHelper.getLogger().info(
                "Chose: " + game.getSelectionMode().toString() + ","
                        + game.getSelectedUnit().getFollowMode());
    }

    /**
     * Sets the users state to {@link SelectionMode#FOLLOW}.
     */
    public void follow() {
        game.setSelectionMode(SelectionMode.FOLLOW);
        LogHelper.getLogger().info(
                "Chose: " + game.getSelectionMode().toString() + ","
                        + game.getSelectedUnit().getFollowMode());
    }

    /**
     * Returns to the "start" screen.
     */
    public void back() {
        nifty.gotoScreen("start");
    }

    /**
     * Set the initial quantity of all traded items to 0
     */
    public void initTradeDialog() {
        TextField tf;
        for (int i = 1; i < 6; i++) {
            tf = screen.findNiftyControl("item" + i + "_offer_textfield",
                    TextField.class);
            tf.setText("0");

            tf = screen.findNiftyControl("item" + i + "_want_textfield",
                    TextField.class);
            tf.setText("0");
        }
    }

	/**
	 * Displays the inventory of the {{@link Game#selectedUnit} at the top bar
	 * of the HUD.
	 */
	public void updateInventoryDisplay() {
		String s = "Inventory: ";

		if (game.getSelectedUnit() != null) {
			Inventory inventory = game.getSelectedUnit().getInventory();
			int size = inventory.items.length;

			for (int i = 0; i < size; i++) {
				s += inventory.getGoodNameByIndex(i) + ": " + inventory.items[i] + ((i < size - 1) ? ", " : "");
			}
		}
		setInventoryLabel(s);
	}

    /**
     * Sets the players mode to {@link SelectionMode#TRADE}, which will pop-up
     * the trade dialog on the players next selction.
     */
    public void trade() {
        game.setSelectionMode(SelectionMode.TRADE);
    }

    /**
     * Brings up the trade layer and the dialog with trade controls.
     */
    public void tradePopup(Movable to) {
        this.to = to;
        toggleVisibility(TRADE_LAYER);
        initTradeDialog();
    }

    /**
     * Sends a players trade offer to the server.
     */
    public void sendOffer() {
        TextField tf;

        Movable from = game.getSelectedUnit();

        Inventory give = new Inventory();
        Inventory get = new Inventory();

        for (int i = 1; i < 6; i++) {
            tf = screen.findNiftyControl("item" + i + "_offer_textfield",
                    TextField.class);
            give.items[i - 1] = Integer.parseInt(tf.getText());

            tf = screen.findNiftyControl("item" + i + "_want_textfield",
                    TextField.class);
            get.items[i - 1] = Integer.parseInt(tf.getText());
        }
		screen.findElementByName("tradeLayer").hide();

		try {
			game.trade(from, to, give, get);

			// TRADEPARTNER PLAYS ON ANOTHER CLIENT
			// ... we have to wait until he makes a decision
			while (true) {
				GameInfo info = game.getGameInfo();

				if (info.tradepartner == null) {
					if (info.tradestatuslast == TradeStatus.ACCEPTED) {
						from.updateStats();
						updateInventoryDisplay();
						logConsole("You offer has been accepted.");
						game.nextPlayer();
					}
					return;
				}
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			logConsole(e.getMessage());
		}
		game.setSelectionMode(SelectionMode.ANY);
	}

    /**
     * Shows the offer, that another player has made.
     */
    public void showOffer() throws IOException, StatusError, ProtocolError {

        GameInfo info = game.getGameInfo();

        Inventory give = info.tradegive;
        Inventory get = info.tradeget;

        String message = "Ich gebe dir " + give.toTradeString()
                + "\n wenn du mir " + get.toTradeString() + " gibst.";

        screen.findNiftyControl("offer_message", Label.class).setText(message);
        screen.findElementByName("tradeLayer").hide();
        screen.findElementByName("trade_receiver").show();
    }
    
    public boolean isOfferVisible() {
        return  screen.findElementByName("trade_receiver").isVisible();
    }

    /**
     * Accept the actual offer.
     */
    public void acceptOffer() {

        try {
            GameInfo info = game.getGameInfo();
            
            game.tradereply(info.tradepartner.playerId, "accepted");
            info.tradepartnerunit.updateStats();
            
        }
        catch (Exception e) {
            errorMessage(e.getMessage());
            e.printStackTrace();
        }

        screen.findElementByName("trade_receiver").hide();
    }

    /**
     * Decline the actual offer.
     */
    public void declineOffer() {
        try {
            GameInfo info = game.getGameInfo();
            game.tradereply(info.tradepartner.playerId, "rejected");
        }
        catch (Exception e) {
        	LogHelper.getLogger().error(e.getMessage());
        }
        screen.findElementByName("trade_receiver").hide();
    }

    /**
     * Sets the players mode to {@link SelectionMode#FIGHT}, which will start
     * the fight action.
     */
    public void fight() {
        game.setSelectionMode(SelectionMode.FIGHT);
    }

    /**
     * Button callback to mark that the current round has been marked as
     * complete by the player.
     */
    public void nextRound() {
    	setInventoryLabel("");
        game.nextPlayer();
    }

    /**
     * Undo function for the selected path.
     */
    public void undo() {
        game.getSelectedUnit().undo();
    }

    /**
     * Undos all selections.
     */
    public void undoAll() {
        game.getSelectedUnit().undoAll();
    }

    /**
     * Redo button for the selected path.
     */
    public void redo() {
        game.getSelectedUnit().redo();
    }

    /**
     * Increases the amount of an item within the trade dialog.
     * 
     * @param item
     *            Id of the item control.
     */
	public void increase(String item) {
		TextField tf = screen.findNiftyControl(item, TextField.class);
		
		int i = Integer.parseInt(tf.getText());
		int j = Integer.parseInt(item.substring(4, 5));
		
		if (item.substring(6, 11).equals("offer")
				&& i < game.getSelectedUnit().info.cargo.items[j - 1]) {
			tf.setText("" + (i + 1));
		} else if (i < 99 && item.substring(6, 10).equals("want"))
			tf.setText("" + (i + 1));
	}

    /**
     * Decreases the amount of an item within the trade dialog.
     * 
     * @param item
     *            Id of the item control.
     */
    public void decrease(String item) {
        TextField tf;
        tf = screen.findNiftyControl(item, TextField.class);
        int i = Integer.parseInt(tf.getText());
        if (i > 0)
            tf.setText("" + (i - 1));
    }

    /**
     * Changes the player mode to {@link PlayerStates#AUTOMATIC_MOVE}.
     */
    public void automaticMovement() {
        game.getSelectedUnit().setMoveMode(PlayerStates.AUTOMATIC_MOVE);
        toggleVisibility("movement_layer");
        move();
    }

    /**
     * Starts the player manual movement mode.
     */
    public void manualMovement() {
        Movable selectedUnit = game.getSelectedUnit();

        try {
            selectedUnit.move();
        } catch (Exception e) {
            Game.getInstance().getHud()
                    .errorMessage("manMov " + e.getMessage());
        }
    }

    /**
     * Moves the player.
     */
    public void move() {
        game.startMovement();
    }

    public void showUndoRedoPanel() {
        show(UNDO_REDO_PANEL);
    }

    public void hideUndoRedoPanel() {
        hide(UNDO_REDO_PANEL);
    }

    /**
     * Toggles the visibility of the panels which provide the buttons for the
     * player's actions. For example, when the player has selected a path he
     * will see the undo button, otherwise not, same for fight and trade buttons
     * for units that are next to the player.
     * 
     * @param unit
     */
    public void showActions(Movable unit) {
        if (unit.ways.size() > 0)
            showUndoRedoPanel();
        else
            hideUndoRedoPanel();
    }

    /**
     * Completely hides the players panels hich allow actions.
     */
    public void hideActions() {
        hideUndoRedoPanel();
    }
    
    @Override
    public void update(float tpf) {
        time += tpf;
        if (time > UPDATE_INTERVAL) {
            time = 0;
            // TODO: do something useful here.
        } 
    };
    
    /**
     * Redraws the mini map.
     */
	public void updateMiniMap() {
		AssetManager assetManager = Game.getInstance().getAssetManager();
		MiniMap miniMap = Game.getInstance().miniMap;
		Texture tex = new Texture2D();
		
		tex.setImage(new AWTLoader().load(miniMap.getImage(), true));

		// ((DesktopAssetManager)assetManager).clearCache();
		long id = System.nanoTime();

		((DesktopAssetManager) assetManager).addToCache(new TextureKey("minimap" + id), tex);
		NiftyImage newImage = nifty.getRenderEngine().createImage("minimap" + id, false);

		nifty.getScreen("hud").findElementByName("mini_map").getRenderer(ImageRenderer.class).setImage(newImage);
	}

    /**
     * This method translates the clicked mini map x-y-coordinates to world coordinates and positions the Camera to these coordinates
     * @param x	ClickLoc coordinate
     * @param y	ClickLoc coordinate
     */
	public void clickedMap(int x, int y) {
		int Y = screen.findElementByName("mini_map").getY();

		Vector3f start = game.getCamera().getLocation();

		Vector3f end = new Vector3f(-(210 - x) * (43.3f / 178f), game
				.getCamera().getLocation().y, -(150 - (y - Y)) * (43.3f / 160f)
				+ cam.getLocation().y * 0.8f);
		game.moveCam(start, end);
	}

    @NiftyEventSubscriber(id = "fogOfWarCheckBox")
    public void onCheckBoxChangeFogOfWar(final String id, final CheckBoxStateChangedEvent event) {
        if (!event.isChecked()) {
            game.disableFogOfWar();
        } else {
            game.enableFogOfWar();
        }
    }
    
    public void setMusicVolume(float volume) {
    	screen.findNiftyControl("musicSliderQuickOptions", Slider.class).setValue(volume);
    }
    
    public void setMuteMusic(boolean isMuted) {
    	screen.findNiftyControl("musicCheckBoxQuickOptions", CheckBox.class).setChecked(isMuted);
    }
    
	@NiftyEventSubscriber(id = "musicSliderQuickOptions")
    public void onSliderMusic(final String id, final SliderChangedEvent event) {
    	game.getGameMusic().setVolume(event.getValue());
    	game.getSettingsScreen().setMusicVolume(event.getValue());
    }
	
	@NiftyEventSubscriber(id = "musicCheckBoxQuickOptions")
	public void onMuteMusic(final String id, CheckBoxStateChangedEvent event) {
		if (event.isChecked()) {
			game.getGameMusic().stop();
		}
		else {
			game.getGameMusic().play();
		}
		// TODO: Quick Setting and Settings must be synchronized
		//game.getSettingsScreen().setMuteMusic(event.isChecked());
	}
	
	@NiftyEventSubscriber(id = "bloomCheckBox")
	public void onBloomChange(final String id, CheckBoxStateChangedEvent event) {
		game.toggleBloom();
	}

    public void quickOptions() {
//    	setMusicVolume(game.getGameSettings().getMusicVolume());
//    	setMuteMusic(!game.getGameSettings().isMusicEnabled());
    	toggleVisibility("quickOptionsPanel");
    }

	public ActionListener actionListener = new ActionListener() {
		@Override
		public void onAction(String name, boolean keyPressed, float arg2) {
			if (name.equals("toggleFog") && !keyPressed) {
				if (game.fogOfWar.equals(FogMode.FOG_DYNAMIC)) {
					game.disableFogOfWar();
				} else {
					game.enableFogOfWar();
				}
			}
		}
	};

	private AnalogListener analogListener = new AnalogListener() {
		@Override
		public void onAnalog(String name, float value, float tpf) {
			if (name.equals("ZoomIn") && cam.getLocation().y > 2) {
				cam.setLocation(cam.getLocation().add(cam.getDirection().mult(3)));
			}
			if (name.equals("ZoomOut") && cam.getLocation().y < 35) {
				cam.setLocation(cam.getLocation().subtract(cam.getDirection().mult(3)));
			}
		}
	};

    @Override
    public void onEndScreen() {
        super.onEndScreen();
        im.removeListener(analogListener);
        im.removeListener(actionListener);
    }

    @Override
    public void onStartScreen() {
        super.onStartScreen();
        
        if (!gameHasStarted) {
        	logConsole("Game has started, \"" + game.getActivePlayerName() + "\" begins.");
        }
        focusQuickOptionsButton();

    	this.inventoryLabel = screen.findNiftyControl("inventoryLabel", Label.class);
    	addListeners();
    }
    
    public void focusQuickOptionsButton() {
    	screen.findNiftyControl("quick_options_button", Button.class).setFocus();
	}

	/**
     * Add the mouse and keyboard listeners.
     */
    public void addListeners() {
    	im.addListener(analogListener, new String[] { "ZoomIn", "ZoomOut" });
    	im.addListener(actionListener, new String[] { "toggleFog" });
    }
    
    /**
     * Outputs a message on the players console.
     * @param message
     */
    public void logConsole(String message) {
    	screen.findNiftyControl(CONSOLE, Console.class).output(message);
    }
    
    /**
     * This callback is called when the user enters something within the console in the HUD and hits <enter> key.
     * @param id
     * @param cEvent
     */
	@NiftyEventSubscriber(id = "console")
	public void onConsoleExecuteCommandEvent(final String id, final ConsoleExecuteCommandEvent cEvent) {
		String consoleInput = cEvent.getCommandLine();
		
		String currentPlayerId = game.getPlayer().playerId;
		
		try {
            game.getClient().chat(currentPlayerId, null, null, consoleInput);
        }
        catch (Exception e) {
            logConsole(e.getMessage());
            LogHelper.getLogger().error("onConsoleExecuteCommandEvent(): " + e.getMessage());
        }
		focusQuickOptionsButton();
	}

	public String getInventoryLabel() {
		return inventoryLabel.getText();
	}

	public void setInventoryLabel(String inventoryLabel) {
		this.inventoryLabel.setText(inventoryLabel);
	}

	public void disableEndTurnButton() {
		findElement("nextRoundButton").disable();
	}

	public void enableEndTurnButton() {
		findElement("nextRoundButton").enable();
	}
	
}
