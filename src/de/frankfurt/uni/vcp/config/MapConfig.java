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

package de.frankfurt.uni.vcp.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>This class provides a logical representation of the map used within the game.</p>
 * 
 * @author Bernd Sp�th, Wladimir Spindler and Saman Sedighi Rad
 */
@XmlRootElement(name = "map-config")
public class MapConfig {

	/** Name of the map. */
	private String mapName;

	private String hexMapCSV;
	private String mapLayout;

	public int width;
	public int height;
	/**
	 * 2d array which provides the map topology used to generate the height map.<br />
	 * A
	 */
	public int[][] csv;

	
	@Override
	public String toString() {
		String string = "";
		
		for (int j=0; j<height; ++j) {
			for (int i =0; i<width; ++i) {
				string += csv[j][i] + " ";
			}
			if (j < height -1)
				string += "\n";
		}
		
		return string;
	}
	
	public MapConfig () {
	}
	
	public MapConfig (int[][] csv) {	
		this.csv = csv;
		this.height = csv.length;
		this.width = csv[0].length;
	}
	
	
	/**
	 * Decide wether a field referenced by given indices is usable for a
	 * players unit.
	 * @param i The i-index of the field
	 * @param j The j-index of the field
	 * @return true if the field is usable, false otherwise
	 */
	public boolean isUsable(int i, int j) {
		if (j >= 0 && j < height && i >= 0 && i < width)
			if (csv[j][i] != 0)
				return true;
		return false;
	}

	/**
	 * Get the name associated with this map
	 * @return The name of the map
	 */
	public String getMapName() {
		return mapName;
	}

	/**
	 * Set the name to be associated with this map
	 * @param name The name of the map
	 */
	@XmlElement(name = "name")
	public void setMapName(String name) {
		this.mapName = name;
	}

	/**
	 * Get the csv data of this map as a String
	 * @return The csv data of the map
	 */
	public String getHexMapCSV() {
		return hexMapCSV;
	}

	/**
	 * Set the csv data of this map as a String
	 * @param hexmapcsv The csv data of the map
	 */
	@XmlElement(name = "hexmapcsv")
	public void setHexMapCSV(String hexmapcsv) {
		this.hexMapCSV = hexmapcsv;
	}

	/**
	 * Get the layout of this map as a String
	 * @return The layout of the map
	 */
	public String getMapLayout() {
		return mapLayout;
	}

	/**
	 * Set the layout of this map as a String
	 * @param mapLayout The layout of the map
	 */
	public void setMapLayout(String mapLayout) {
		this.mapLayout = mapLayout;
	}

}
