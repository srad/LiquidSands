package de.frankfurt.uni.vcp.gui.controllers;


import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import de.frankfurt.uni.vcp.config.MapConfig;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.game.MiniMap;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.net.MapPreview;
import de.frankfurt.uni.vcp.net.TCPClient;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.render.NiftyImage;

/**
 * <h3>  This is the screen controllers used during map setup of the game </h3>
 * 
 * <p> This class provides the functionality to select from a given list
 *     of maps provided by the game server. </p>
 *     
 * <p> New games can be started with a given name. </p>
 *     
 * 
 * @author wladimir + saman + bernd
 *
 */
public class MapScreenController extends AbstractScreenController {

	public void cancel() {
		nifty.gotoScreen("server");
	}
	
	 /** 
   * Hide the error message currently displayed
   */
	 public void ok(){
	    screen.findElementByName("Map_error").hide();
	  }
	
	  /**
	   * Display an error message
	   * @param message The message to be dsplayed
	   */
	public void errorMessage (String message) {
	  Element layer = screen.findElementByName("Map_error");
	  layer.show ();
	  
	  Label errorLabel = screen.findNiftyControl("mapErrorLabel", Label.class);
	  LogHelper.getLogger ().error (message);
	  errorLabel.setText (message);
	}
	
	/**
	 * Create a new game
	 */
	public void create() {
		try {
			TCPClient client = game.getClient();

			@SuppressWarnings("unchecked")
			ListBox<String> mapList = screen.findNiftyControl("maps_listBox", ListBox.class);
			String mapId = mapList.getSelection().get(0);

			TextField gameField = screen.findNiftyControl(
					"game_name_textfield", TextField.class);
			String gameId = TCPClient.sanitize(gameField.getText());
			if (gameId.equals(""))
				return;

			game.setGameId(gameId);
			game.setMapId(mapId);

			client.creategame(mapId, gameId);
			game.addPlayer(game.getPlayerName());
			
			nifty.gotoScreen("player");
		} catch (Exception e) {
			errorMessage(e.getMessage());
		}
	}

	/** 
	 * Initialize the gui of this screen
	 */
	@Override
	public void onStartScreen() {
		super.onStartScreen();
		
		@SuppressWarnings("unchecked")
		ListBox<String> mapList = screen.findNiftyControl("maps_listBox", ListBox.class);
		TCPClient client = game.getClient();
		
		mapList.clear();
		
		try {
			for (String s : client.maplist())
				mapList.addItem(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		screen.findElementByName("game_name_textfield").setFocus();
	}
	
	@NiftyEventSubscriber(id="maps_listBox")
	public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
		java.util.List<String> selection = event.getSelection();
		if(selection.isEmpty())
			return;
		System.out.println(selection.get(0));
		TCPClient client=Game.getInstance().getClient();
		MapPreview mp=null;
		try {
			mp = client.mappreview(selection.get(0));
		} catch (Exception e) {
			LogHelper.getLogger().error("Kartenvorschau konnte nicht erstellt werden");
		}
		MapConfig mc=new MapConfig(mp.terrainMap);
		MiniMap mm=new MiniMap(mc);
		
		AssetManager assetManager = Game.getInstance().getAssetManager();
		Texture tex = new Texture2D();
		
		tex.setImage(new AWTLoader().load(mm.getImage(), true));

		// ((DesktopAssetManager)assetManager).clearCache();
		long mmid = System.nanoTime();

		((DesktopAssetManager) assetManager).addToCache(new TextureKey("minimap" + mmid), tex);
		NiftyImage newImage = nifty.getRenderEngine().createImage("minimap" + mmid, false);

		nifty.getScreen("map").findElementByName("mini_map").getRenderer(ImageRenderer.class).setImage(newImage);
	}
}
