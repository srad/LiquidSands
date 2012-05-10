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

package de.frankfurt.uni.vcp.nodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.jme3.math.FastMath.sqrt;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import de.frankfurt.uni.vcp.config.MapConfig;
import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.nodes.movables.Movable;
import de.frankfurt.uni.vcp.units.Player;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2.</h3>
 * 
 * <p>The complete {@link Node} holding all {@link Field} of the map.</p>
 * 
 * @author Bernd Sp�th, Wladimir Spindler and Saman Sedighi Rad
 */
public class HexMap extends Node {
    
  public static final String[] LAYERS = {"path", "range"};
    
    
    // CONSTANTS
    /** Used to measure the distance between each hexagon. */
    public static final float SQRT_3 = sqrt(3);

    
    // FIELDS
    /** Holds all {@link Field}. */
    private int width;
    private int height;
    private Field[][] fields;
    
    // SELECTION LAYERS
    /**
     * Only the first layer, that has an entry in this list will be shown.
     */
    public LinkedList<String> layerOrder = new LinkedList<String> ();
    
    /** Holds a {@link List} of all selected {@link Field}. */
    //private List<Field> selection = new LinkedList<Field>();
    
    
    /** List of blocking object within the map. */
    public List<Movable> blockers = new ArrayList<Movable>();
    
    
    
    // CONSTRUCTOR
    /**
     * Construct a new HexMap
     * @param config reference to the MapConfig used to create this map
     */
    public HexMap (MapConfig mapConfig){
        
      for (String l : LAYERS)
        layerOrder.add (l);
      
        width = mapConfig.width;
        height = mapConfig.height;
        
        fields = new Field[mapConfig.height][];
        for (int j=0; j<mapConfig.height; ++j){
            fields[j] = new Field[mapConfig.width];
            for (int i=0; i<mapConfig.width; ++i) {
                if (mapConfig.csv[j][i] != 0) {
                    Field field = new Field (i, j);
                    fields[j][i] = field;
                    this.attachChild(field);

                    Vector3f correctPositon;
                    if (j%2 == 0) {
                        correctPositon = new Vector3f(-SQRT_3 * i, 0, -3f * j/2);
                    }
                    else {
                        correctPositon = new Vector3f(-SQRT_3 * i + SQRT_3/2f, 0, -3f * (j/2) - 1.5f);
                    }
                    field.move(correctPositon);
                }
            }
        }
		setShadowMode(ShadowMode.Receive);
    }
    
    
    // FIELDS
    /**
     * Get the field positioned at specified i and j index
     * @param i  The i-index of the field
     * @param j  The j-index of the field
     * @return The field if there is any, {@code null} otherwise
     */
    public Field getField (int i, int j) {
        return fields[j][i];
    }
    

    // NEW
    public void clearAll () {
    for (int i=0; i<width; ++i)
      for (int j=0; j<height; ++j)
        if (fields[j][i] != null)
          fields[j][i].unselectAll();
    }
    
  public void clearLayer (String layer) {
    for (int i=0; i<width; ++i)
      for (int j=0; j<height; ++j)
        if (fields[j][i] != null)
          fields[j][i].unselect(layer);
  }
  
  public void clearFog(){
	  for (int i=0; i<width; ++i)
	      for (int j=0; j<height; ++j)
	        if (fields[j][i] != null){
	          fields[j][i].disableFog();
	          fields[j][i].setViewers(1);
	        }
  }
  
  public void fillFog(){
	  for (int i=0; i<width; ++i)
	      for (int j=0; j<height; ++j)
	        if (fields[j][i] != null){
	          fields[j][i].enableFog();
	          fields[j][i].setViewers(0);
	        }
  }
    
    
    // OLD
//  // SELECTION
//  /**
//   * Add this field to the list of currently selected fields
//   * @param field The field to add
//   */
//  public void addToSelection (Field field) {
//      selection.add(field);
//  }
//  
//  /**
//   * Add all fields in a given list to the list of currently selected fields
//   * @param fields List of the fields to add
//   */
//  public void addToSelection (Collection<Field> fields) {
//      selection.addAll(fields);
//  }
    
//  /**
//   * Removes {@link Field} selections.
//   */
//  public void clearSelection () {
//      for (Field f : selection) {
//          f.unselect();
//      }
//      selection.clear();
//  }
    
    
    // HELPERS
    /**
     * Return the field (if any) positioned at the given location in 3-D space
     * @param position Position in 3-D space
     * @return The field at the specified position, or {@code null} if no field is 
     * located at this position.
     */
    public Field positionToField(Vector3f position){
        position.y = -1f;
        return pick(position, Vector3f.UNIT_Y);
    }
    
    /**
     * Used to determine the clicked {@link Field}.
     * @param position Position from which the collision shall take place.
     * @param direction Direction from which the collision shall take place.
     * @return Returns the clicked field.
     */
    public Field pick(Vector3f position, Vector3f direction) {      
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(position, direction);

        collideWith(ray, results);
        if (results.size() == 0)
            return null;
        
        Geometry geometry = results.getCollision(0).getGeometry();
        Field field = (Field) geometry.getParent().getParent();
        
        return field;
    }
    
    /**
     * Return field randomly chosen from all usable fields
     * @return A randomly chosen field
     */
    public Field getRandomField() {
        java.util.Random r = new java.util.Random();
        int x,y;
        
        while (true) {
            x = r.nextInt(width);
            y = r.nextInt(height);
            
            if ( this.isFieldAvailable(this.fields[x][y]) ) {
                return this.fields[x][y];
            }
        }
    }
    
    /**
     * Determine wether this field is unused, or if it is blocked, either 
     * by mountains, or by any player unit
     * @param field The field to check
     * @return {@code true} if this field is available, {@code false} otherwise
     */
    public boolean isFieldAvailable(Field field) {
        if (field == null)
            return false;
        
        for (Player p : Game.getInstance().getPlayers()) {
            for (Movable m : p.units){
                Field f = m.getField();
                if (f == field)
                    return false;
            }
        }
        return true;
    }

    
    /**
   * Determine wether this field is available for a given unit
   * @param field The field to check
   * @return {@code true} if this field is available, {@code false} otherwise
   */
    public boolean isFieldAvailableForUnit(Field field, Movable unit) {
        if (field == null)
            return false;
        
        for (Player p : Game.getInstance().getPlayers()) {
            for (Movable m : p.units){
                if (m == unit)
                    continue;
                Field f = m.getField();
                if (f == field)
                    return false;
            }
        }
        return true;
    }
    

    
//  /**
//   * Highlight all fields, that are adjacent to a given unit and occupied 
//   * by any unit
//   * @param unit The unit who's neighborhood should be highlighted
//   */
//  public void highlightNeighbourUnits(String layer, Movable unit) {
//      clearLayer(layer);
//      
//      for (Field f : unit.getField().getNeighbours()) {
//          if (f.getUnitOn() != null) {
//              try {
//                  f.select(layer, ColorRGBA.Red);
//              } catch (Exception e) {
//                  LogHelper.getLogger().error(e.getMessage());
//              }
//          }
//      }
//  }
}
