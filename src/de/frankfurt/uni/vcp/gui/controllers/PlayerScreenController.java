package de.frankfurt.uni.vcp.gui.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.net.GameInfo;
import de.frankfurt.uni.vcp.net.GameInfo.GameStatus;
import de.frankfurt.uni.vcp.net.MapPreview;
import de.frankfurt.uni.vcp.net.MessageInfo;
import de.frankfurt.uni.vcp.net.ProtocolError;
import de.frankfurt.uni.vcp.net.StatusError;
import de.frankfurt.uni.vcp.net.TCPClient;
import de.frankfurt.uni.vcp.units.Player;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleExecuteCommandEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;

/**
 * <h3>  This is the screen controllers used during player setup of the game </h3>
 * 
 * <p> This class provides the functionality to add players to the game. </p>
 *     
 * <p> The current game can be set running. </p>
 *     
 * 
 * @author wladimir + saman + bernd
 *
 */
public class PlayerScreenController extends AbstractScreenController {

	/** List of player which already joined the game. */
	ListBox<String> playerBox;
	
	/** If true then the {@link #playerBox} will be updated with the server information about joined players. */
	private boolean startJoinedPlayerUpdate = false;
	
	/** Update intervall in seconds in which  the {@link #playerBox} shall be updated */
	private final float UPDATE_INTERVALL_JOINED_PLAYERS = 2.0f;
	
	/** This timer is currently used by {@link #update(float)} to update the {@link #playerBox}. */
	private float timer = 0.0f;
	
	 /** 
   * Hide the error message currently displayed
   */
	 public void ok(){
	    screen.findElementByName("Player_error").hide();
	  }

	  /**
	   * 	   * Display an error message
	   * @param message The message to be dsplayed
	   */
	  public void errorMessage (String message) {
	    Element layer = screen.findElementByName("Player_error");
	    layer.show ();

	    Label errorLabel = screen.findNiftyControl("playerErrorLabel", Label.class);
	    LogHelper.getLogger ().error (message);
	    errorLabel.setText (message);
	  }
	
	/**
	 * Cancel player setup and return to the previous screen
	 * @throws ProtocolError 
	 * @throws StatusError 
	 * @throws IOException 
	 */
	public void cancel() throws IOException, StatusError, ProtocolError{
	    
	    if (game.getClient().getGameOwnerKey() != null) {
	        removeGame();
	    }
	    else {
	        String playerId = game.getPlayer().playerId;
	        game.getClient().delplayer(game.getGameId(), playerId);
	    }
	    
		nifty.gotoScreen("map");
	}
	
    /**
     * This callback is called when the user enters something within the console in the HUD and hits <enter> key.
     * @param id
     * @param event
     */
	@NiftyEventSubscriber(id = "chatConsole")
	public void onConsoleExecuteCommandEvent(final String id, final ConsoleExecuteCommandEvent event) {
		String consoleInput = event.getCommandLine();
		
		String currentPlayerId = game.getPlayer().playerId;
		
		try {
            game.getClient().chat(currentPlayerId, null, null, consoleInput);
        }
        catch (Exception e) {
        	writeChat(e.getMessage());
            LogHelper.getLogger().error("onConsoleExecuteCommandEvent(): " + e.getMessage());
            e.printStackTrace();
        }
	}
	
    /**
     * Outputs a message on the players console.
     * @param message
     */
    public void writeChat(String message) {
    	screen.findNiftyControl("chatConsole", Console.class).output(message);
    }
    
	
	/**
	 * Start the game
	 * @throws Exception
	 */
	public void start() throws Exception {
	    TCPClient client = game.getClient();
	    
	    GameInfo info = game.getGameInfo();
	    MapPreview preview = client.mappreview(info.mapid);
	    
	    
	    if (info.playernames.size() != preview.numberOfPlayers) {
	        errorMessage(preview.numberOfPlayers + " players are required to start the game!\nPlease wait until all players have joined.");
	        return;
	    }
	    
        LogHelper.getLogger().info("Starting game");
        client.startgame(game.getGameId());
	}

	public void removePlayer () throws IOException, StatusError, ProtocolError {
	    
	    List<String> selection = playerBox.getSelection();
	    
	    if (selection.size() == 0)
	        return;
	    
	    String playerName = selection.get(0);
	   
	    System.out.println(
	    game.getPlayer());
	    Player p = game.getPlayer();
	    if (p.playerName.equals(playerName))
	        return;
	    
	    String playerId = game.getPlayer().playerId;
	    game.getClient().delplayer(game.getGameId(), playerId, playerName);
	}
	
	
	/**
	 * Remove the created game and go back to the maps overview.
	 */
	public void removeGame() {
		try {
			game.getClient().removegame(game.getGameId());
			// Set it back to null.
			game.setGameId(null);
		} catch (Exception e) {
			errorMessage(e.getMessage());
			LogHelper.getLogger().error(e.getMessage());
			e.printStackTrace();
		}
	}

	 /** 
   * Initialize the gui of this screen
   */
	@SuppressWarnings("unchecked")
	@Override
	public void onStartScreen() {
		super.onStartScreen();	
		//TextField nameField = screen.findNiftyControl("name_textfield", TextField.class);
		//nameField.setFocus();
	    playerBox = screen.findNiftyControl("players_listBox", ListBox.class);
	    this.startJoinedPlayerUpdate = true;
	    
	    // Toggle start game visibility.
	    if (game.getClient().getGameOwnerKey() != null) {
	        
	    	screen.findElementByName("startButton").setVisible(true);
	    	
	    	screen.findElementByName("creatorPanel").setVisible(true);
	    }
	    try {
			game.setMapId(game.getGameInfo().mapid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StatusError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEndScreen() {
		super.onEndScreen();
		startJoinedPlayerUpdate = false;
	}

	/**
	 * Check each {@link #UPDATE_INTERVALL_JOINED_PLAYERS} seconds if any user
	 * has joined. If two user join, then the game will be started
	 * automatically.
	 */
	@Override
	public void update(float tpf) {
		super.update(tpf);
		timer += tpf;

		try {
			if (startJoinedPlayerUpdate	&& (timer >= UPDATE_INTERVALL_JOINED_PLAYERS)) {
				timer = 0.0f;

				GameInfo gameInfo = game.getGameInfo();
				game.gameInfo = gameInfo;

				List<String> boxNames = playerBox.getItems();
				
				
				List<String> list = new LinkedList<String> ();
				
				// ADD NEW PLAYERS
				for (String gn : gameInfo.playernames) {
				    boolean found = false;
				    for (String bn : boxNames)
				        if (gn.equals(bn))
				            found = true;
				    if (!found)
				        list.add(gn);
				}
				for (String s : list)
				    playerBox.addItem(s);
				
				list.clear();
				
				// REMOVE PLAYERS
				for (String bn : boxNames) {
                    boolean found = false;
                    for (String gn : gameInfo.playernames)
                        if (gn.equals(bn))
                            found = true;
                    if (!found)
                        list.add(bn);        
                }
				for (String s : list)
				    playerBox.removeItem(s);
				
				
				//playerBox.clear();
				//playerBox.addAllItems(gameInfo.playernames);
				
				
				
				
				// CHAT MESSAGES
				List<MessageInfo> messages = game.getClient().getdata();
				for (MessageInfo i : messages) {
					// TODO: ATTENTION WORKAROUND TO FIND THE PLAYERS NAME FOR
					// THE CHAT. The problem now is that we only have the player id
					// and we try to match is with the player name to get the real name
					// for the chat display.
					for (String playerName : gameInfo.playernames) {
						if (i.senderplayerid.startsWith(playerName)) {
							writeChat(playerName + "> " + i.message);
						}
					}
				}
				// CHAT END

				if (gameInfo.gamestatus == GameStatus.STARTED) {
					startJoinedPlayerUpdate = false;

					game.startgame();
					nifty.gotoScreen("hud");
				}

				LogHelper.getLogger().info("Updated player list.");
			}
		} catch (Exception e) {
			String message = "";
			if (e.getMessage().isEmpty() || (e.getMessage() == null)) {
				message = "no exception messsage";
			}
			else {
				message = e.getMessage();
			}
			errorMessage(message);
			LogHelper.getLogger().error(message);
//			if (e.getMessage() != null)
//			    if (e.getMessage().endsWith ("_is_not_a_vaild_player")
//			            || e.getMessage().endsWith ("unkown_gameid")) {
			        nifty.gotoScreen("server");
//			    }
//			e.printStackTrace();
		}
	}
	
}
