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

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>This class is supposed to represent a units properties.</p>
 * 
 * @author Bernd Späth, Wladimir Spindler and Saman Sedighi Rad
 */
public class MeepleConfig {
	
	public String name;
	public int movement;
	public int hitPoints;
	public int maxFirepower;
	public int maxCargo;

	public void parse(File file) {
		try {
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(file);
			doc.getDocumentElement().normalize();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
