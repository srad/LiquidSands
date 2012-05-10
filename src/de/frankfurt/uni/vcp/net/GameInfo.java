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

package de.frankfurt.uni.vcp.net;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.nodes.movables.Movable;
import de.frankfurt.uni.vcp.units.Inventory;
import de.frankfurt.uni.vcp.units.Player;

/**
 * <h3> This class holds data about the current state of the game </h3>
 * 
 * <p> It contains fields for every info-parameter, the server reports
 *     about a specific game. </p>
 * 
 * <p> To avoid confusion between field-names and protocol-paramter-names
 *     the naming-scheme follows the scheme used in the client-server protocol
 * </p>
 * 
 * <p> Datatypes used to represent values sent over the wire as strings have
 *     been chosen to be the ones a user hopefully might expect.
 * </p>
 * 
 *
 */
public class GameInfo extends Info{

    /**
     * Represents the values that our representation of a trade status can have 
     */
    public enum TradeStatus {
        NONE,
        DND,
        ACCEPTED,
        REJECTED,
    }
	
    /**
     * Represents the values that our representation of a game   status can have 
     */
    public enum GameStatus {
        INITED,
        STARTED
    }

    
    private static final HashMap<String,TradeStatus> TRADE_STATUS_MAP;
    private static final HashMap<String,GameStatus> GAME_STATUS_MAP;

    static {
        // TRADE STATUS
        TRADE_STATUS_MAP = new HashMap<String,TradeStatus>();

        TRADE_STATUS_MAP.put("none", TradeStatus.NONE);
        TRADE_STATUS_MAP.put("accepted", TradeStatus.ACCEPTED);
        TRADE_STATUS_MAP.put("rejected", TradeStatus.REJECTED);
        TRADE_STATUS_MAP.put ("DND", TradeStatus.DND);


        // GAME STATUS
        GAME_STATUS_MAP = new HashMap<String,GameStatus>();

        GAME_STATUS_MAP.put("inited", GameStatus.INITED);
        GAME_STATUS_MAP.put("started", GameStatus.STARTED);
    }
    
    // FIELDS
    /**
     * The id of this game.
     */
    public String gameid;
 
    
    public String mapid;
    
    /**
     * The name associated with thid id of this game.
     */
    public String name;
	
    /**
     * <h3> The status of this game </h3>
     * <p> The status is represented as a value of {@link GameStatus} </p>
     */
    public GameStatus gamestatus; // inited | started
	
    /**
     * The current turn of the game.
     */
    public int turn;
    /**
     * This field will contain a reference to the {@link Player}, that won this game.
     * If the game is still running, and thus no winner has been determined until now,
     * this filed will hold {@code null} as its value.
     */
    public Player winner;
	
    /**
     * This field will contain a reference to the {@link Player}, that is the active
     * player of the game.
     */    
    public Player activeplayer;
    
    
    /**
     * This field will contain the {@code activunitslastaction} string as reported
     * by the server. <br>
     * <b> Note: </b> The datatype of this field should be changed, once the set
     * of possible values has been determined.
     */        
    public String activeunitslastaction; // NONE
	

    /**
     * This field will contain a reference to the {@link Player}, that is the partner
     * in a trade
     */    
    public Player tradepartner;
    

    /**
     * This field will contain a reference to the {@link Player}, that is the partner
     * in a trade
     */    
    public Movable tradepartnerunit;
	
    
    /**
     * This field will contains the amount of the items offered in a trade request, as
     * an {@link Inventory}
     */
    public Inventory tradegive;
    
    
    /**
     * This field will contains the amount of the items demanded in a trade request, as
     * an {@link Inventory}
     */
    public Inventory tradeget;
	
    
    /**
     * <h3> The status of the trade request sent most recently. </h3>
     * <p> The status is represented as a value of {@link TradeStatus} </p>
     */    
    public TradeStatus tradestatuslast; // none | accepted | rejected
	
    
    /**
     * The names of all Players participating in the game.
     */  
    public List<String> playernames;
    
    
    /**
     * The values of the key "turnstaken" as they are reported from the
     * server.
     */  
    public List<Boolean> turnstaken;
	
    
	/**
	 * Construct a new GameInfo instance from the key value pairs contained in
	 * the info string passed to the constructor.
	 * @param info The info string containing whitespace separated "key=value"
	 * pairs.
	 */
    public GameInfo (String info) {
        super(info);
        Game game = Game.getInstance();
          
        gameid = tags.get("gameid");
        name = tags.get("name");
        
        mapid = tags.get("mapid");
        
        gamestatus = GAME_STATUS_MAP.get(tags.get("gamestatus"));

        turn = Integer.parseInt(tags.get("turn"));

        winner = game.getPlayer(tags.get("winner"));
        activeplayer = game.getPlayer(tags.get("activeplayer"));


        // TODO: use an enum instead !!
        activeunitslastaction = tags.get("activeunitslastaction");


        // TRADE
        tradepartner = game.getPlayer(tags.get("tradepartner"));
        
        String value;
        value = tags.get("tradepartnerunit");
        if (value.equals (""))
            tradepartnerunit = null;
        else  {
        	// TODO: Commented line below, unused?
            //int id = Integer.parseInt(value);
            tradepartnerunit =  game.getUnit(Integer.parseInt(value));
        }
            
        // TODO: sollte Inventory sein !!
        value = tags.get("tradegive");
        if (! value.equals(""))
            tradegive = new Inventory(value);
        
        value = tags.get("tradeget");
        if (! value.equals(""))
            tradeget = new Inventory(value);
        
        tradestatuslast = TRADE_STATUS_MAP.get(tags.get("tradestatuslast"));
	

        // PLAYERS
        playernames = new LinkedList<String>();
        String string = TCPClient.splitBrace(tags.get("playernames")).get(0);
        for (String s : TCPClient.splitBrace(string))
            playernames.add(s);


        // TURNSTAKEN
        string = TCPClient.splitBrace(tags.get("turnsTaken")).get(0);
        
        turnstaken = new LinkedList<Boolean>();
        for (String s : TCPClient.splitBrace(string))
            turnstaken.add(s.equals("true"));
    }
}
