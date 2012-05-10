package de.frankfurt.uni.vcp.gui.controllers;

import java.io.IOException;

import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.net.TCPClient;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;

/**
 * <h3>This is the screen controllers used for the startup screen.</h3>
 * 
 * <p>
 * This class provides the functionality to modify client settings and to view
 * the background story.
 * </p>
 * 
 * <p>
 * A connection to a game server with a given name can be started.
 * </p>
 * 
 * 
 * @author wladimir + saman + bernd
 * 
 */
public class StartScreenController extends AbstractScreenController {

	/**
	 * Start a new game
	 */
	public void newGame() {
		pageTurnSound.play();
		game.setRunning(false);

		try {
			LogHelper.getLogger().info("Trying to log in to the server...");
			game.getClient().logon();
			LogHelper.getLogger().info("...logged in to the server");
		} catch (Exception e) {
			nifty.gotoScreen("start");
			showErrorMessage(e.getMessage());
			LogHelper.getLogger().error(e.getMessage());
		}
		nifty.gotoScreen("player");
	}

	/**
	 * Quit this game
	 * 
	 * @throws IOException
	 */
	public void quitGame() throws IOException {
		LogHelper.getLogger().info("Quitting game");
		
		game.saveGameSettings();
		LogHelper.getLogger().info("Saved settings");
		
		game.stop();
	}

	/**
	 * Depending on the screen name given display either the scenario or the
	 * settings screen
	 * 
	 * @param screen
	 *            The name of the screen to be shown
	 */
	public void test(String screen) {
		if (screen.equals("scenario"))
			nifty.gotoScreen(screen);
		else if (screen.equals("settings"))
			nifty.gotoScreen(screen);
		pageTurnSound.play();
	}

	/**
	 * Resume a game currently running
	 */
	public void resumeGame() {
		nifty.gotoScreen("hud");
	}

	/**
	 * Connect to the game server
	 */
	public void connect() {
		try {
			String playerName = getPlayerName();
			String serverLocation = getServerLocation();

			LogHelper.getLogger().info("Player name: \"" + playerName + "\"");
			LogHelper.getLogger().info("Server location: \"" + serverLocation + "\"");

			if (playerName.equals("") || serverLocation.equals("")) {
				throw new Exception("Player name or server location is missing!");
			}

			TCPClient client = new TCPClient(serverLocation, 1504, TCPClient.CLIENT_INFO);

			// The entered name on the start screen will be the players name.
			game.setPlayerName(playerName);

			game.setClient(client);
			game.setRunning(false);
			
			// Before we go ahead, first save the settings.
			game.saveGameSettings();
			
			client.logon();
			nifty.gotoScreen("server");
		} catch (Exception e) {
			showErrorMessage(e.getMessage());
			LogHelper.getLogger().error(e.getMessage());
		}
	}

	/**
	 * Hide the error message currently displayed
	 */
	public void ok() {
		screen.findElementByName("Server_error").hide();
	}
	
	/**
	 * Pops up the error window and shows a message.
	 * @param message The error message.
	 */
	public void showErrorMessage(String message) {
		Label l = screen.findNiftyControl("startScreenErrorMessage", Label.class);
		l.setText(message);
		screen.findElementByName("Server_error").show();
	}

	/**
	 * Initialize the gui of this screen
	 */
	@Override
	public void onStartScreen() {
		super.onStartScreen();

		if (game != null) {
			if (game.isRunning())
				this.screen.findElementByName("resume_panel").show();
			else
				this.screen.findElementByName("resume_panel").hide();
		} else {
			this.screen.findElementByName("resume_panel").hide();
		}
	}

	/**
	 * Sets the server field value on the start screen.
	 * 
	 * @param location
	 */
	public void setServerLocation(String location) {
		screen.findNiftyControl("server_textfield", TextField.class).setText(location);
	}

	/**
	 * Returns server location string from the text box on the start page.
	 * @return Server location.
	 */
	public String getServerLocation() {
		return screen.findNiftyControl("server_textfield", TextField.class).getText().trim();
	}
	
	/**
	 * Returns the players name on the start screen.
	 * @return Player name.
	 */
	public String getPlayerName() {
		return screen.findNiftyControl("playerName_textfield", TextField.class).getText().trim();
	}

	/**
	 * Sets on the start screen a user name.
	 * @param playerName
	 */
	public void setPlayerName(String playerName) {
		screen.findNiftyControl("playerName_textfield", TextField.class).setText(playerName);
	}
	
}
