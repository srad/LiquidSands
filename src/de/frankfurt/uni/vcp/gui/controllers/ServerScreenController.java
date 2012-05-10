package de.frankfurt.uni.vcp.gui.controllers;

import java.util.List;

import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.net.TCPClient;
import de.lessvoid.nifty.controls.ListBox;

public class ServerScreenController extends AbstractScreenController {

	/** Used to determine in {@link #update(float)} if it shall be checked for map with a server request. */
	private boolean checkServerMapUpdates = false;
	
	/** Update intervals for {@link #update(float)} */
	private final float UPDATE_INTERVALL = 2.0f;
	
	/** Time counter for {@link #update(float)}. */
	private float timer = 0.0f;
	
	/** List of available games on the server */
	private ListBox<String> gameList;
	
	/**
	 * Move to the "map" screen which allows to select the available maps.
	 */
	public void create() {
		nifty.gotoScreen("map");
	}

	/**
	 * Joins a game.
	 */
	public void join() {
		try {
			String gameId = gameList.getSelection().get(0);
			
			game.setGameId(gameId);
			game.addPlayer(game.getPlayerName());

			nifty.gotoScreen("player");
		} catch (Exception e) {
			LogHelper.getLogger().info(e.getMessage());
		}
	}

	/**
	 * Adds all games to the drop down list on the "Server" screen if they are not already listen in there.
	 */
	public void refresh() {
		try {
			TCPClient client = game.getClient();
			removeDeletedGames(client.gamelist());

			for (String s : client.gamelist()) {
				if (!isInGameList(s)) {
					gameList.addItem(s);
				}
			}
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}
	
	/**
	 * Iterates through the game list and removes game that has deleted from the
	 * server. If we just clear the list the selection will be deleted each
	 * time, that annoying from a usability standpoint.
	 */
	public void removeDeletedGames(List<String> serverGames) {
		try {
			boolean found = false;
			
			for (String game : gameList.getItems()) {
				for(String serverGame : serverGames) {
					if (game.equals(serverGame)) {
						found = true;
						break;
					}
				}
				if (!found) {
					gameList.removeItem(game);
				}
				found = false;
			}
		} catch (Exception e) {
			LogHelper.getLogger().error(e.getMessage());
		}
	}
	
	/**
	 * Returns <b>true</b> if the passed in gameName is already in the {@link #gameList}.
	 * 
	 * @param gameName to check if in the {@link #gameList}
	 * @return True if already in the list.
	 */
	private boolean isInGameList(String gameName) {
		boolean found = false;		
		List<String> items = gameList.getItems();
		
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).equals(gameName)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Moves back to the start screen.
	 */
	public void cancel() {
		nifty.gotoScreen("start");
	}

   /*
   * Initialize the gui of this screen
   */
	@SuppressWarnings("unchecked")
	@Override
	public void onStartScreen() {
		super.onStartScreen();
		
		gameList = screen.findNiftyControl("games_listBox",	ListBox.class);
		checkServerMapUpdates = true;
	}
	
	@Override
	public void onEndScreen() {
		super.onEndScreen();
		checkServerMapUpdates = false;
	}
	
	/**
	 * Updates after each {@link #UPDATE_INTERVALL} time span the
	 * {@link #gameList} by calling the {@link #refresh()} method.
	 */
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		timer += tpf;
		if (checkServerMapUpdates && (timer >= UPDATE_INTERVALL)) {
			timer = 0.0f;
			refresh();
		}
	}
	
}
