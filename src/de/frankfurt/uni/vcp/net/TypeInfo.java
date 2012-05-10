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


/**
 * <h3> This class holds data about the unit types available in the game </h3>
 * 
 * <p> It contains fields for every info-parameter, the server reports
 *     about a specific unit. </p>
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
public class TypeInfo extends Info{

    /**
     * The name of this unittype.
     */
    public String name;

    /**
     * The maximum number of hitpoints any unit of this type can have
     */
    public int maxhitpoints;
    /**
     * The maximum damage any unit of this type can ever inflict on an opponent
     */
    public int maxfirepower;
    /**
     * The maximum number of items a unit of this type can transport
     */
    public int maxcargo;
    /**
     * The maximum number of fields, that a unit of this type can travel
     */
    public int maxmovement;
    
    
    TypeInfo (String info) {
        super(info);

        name = tags.get("name");

        maxhitpoints = Integer.parseInt(tags.get("maxhitpoints"));
        maxfirepower = Integer.parseInt(tags.get("maxfirepower"));
        maxcargo = Integer.parseInt(tags.get("maxcargo"));
        maxmovement = Integer.parseInt(tags.get("maxmovement"));       
    }
	
}
