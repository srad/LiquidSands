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

import java.util.LinkedList;
import java.util.List;

import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.helpers.LogHelper;
import de.frankfurt.uni.vcp.nodes.Field;
import de.frankfurt.uni.vcp.units.Inventory;
import de.frankfurt.uni.vcp.units.Player;

/**
 * <h3> This class holds data about a specific unit in the game </h3>
 * 
 * <p> It contains fields for every info-parameter, the server reports
 *     about this unit. </p>
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
public class UnitInfo extends Info{

    /**
     * The unitid used to reference this unit in client-server communication
     */
    public int unitid;

    /**
     * A reference to the {@link Player}, that is the owner of this unit
     */    
    public Player owner;

    /**
     * A reference to the {@link TypeInfo}, that describes the type of unit this instance
     * belongs to
     */    
    public TypeInfo utype;
    
    /**
     * Life status of this unit:  {@code true}, if the unit is dead, {@code false} otherwise
     */
    public boolean destroyed;
	
    /**
     * The number of fields this unit can still move
     */
    public int movement;
    
    /**
     * The fields, that this unit moved in its last move action.
     */    
    public List<Field> lastmovement = new LinkedList<Field>();
	
    // INVISIBLE TO OTHER PLAYERS !!
    /**
     * The number of hitpoints still left on this unit
     */    
    public int hitpoints;
    
    /**
     * The items currently transported by this unit as {@link Inventory}
     */        
    public Inventory cargo;
	
	
    UnitInfo (String info) {
        super(info);
        Game game = Game.getInstance();
        
        unitid = Integer.parseInt(tags.get("unitid"));
        owner = game.getPlayer(tags.get("owner"));
        utype = game.getUnitType(tags.get("utype"));
        destroyed = tags.get("destroyed").equals("false");
        
        // TODO: wirds noch irgendwo verwendet?
        //movement = Integer.parseInt(tags.get("movement"));
        String range = tags.get("movement");
        if (range == null) {
        	LogHelper.getLogger().info("Movement null!");
        }
        movement = (range != null) ? Integer.parseInt(range) : -1;
        
        String string;
        
     // TODO
//        string = TCPClient.splitBrace(tags.get("lastmovement")).get(0);
//        for (String s : TCPClient.splitBrace(string)) {
//            String[] p = s.split (",");
//            lastmovement.add(game.getHexMap().getField(Integer.parseInt(p[0]), Integer.parseInt(p[1])));
//        }
        
        // POSSIBLY INIVISBLE
        string = tags.get("hitpoints");
        if (string != null)
            hitpoints = Integer.parseInt(string);
        string = tags.get("cargo");
        if (string != null)
            cargo = new Inventory(string);
    }
}
