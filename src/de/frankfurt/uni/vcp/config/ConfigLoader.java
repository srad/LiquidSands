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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.frankfurt.uni.vcp.helpers.LogHelper;

/**
 * <h3>Visual-Computing Praktikum - Aufgabe 2</h3>
 * 
 * <p>This class provides the data to generate the map by parsing the needed
 * configuration files and providing its content as a usable data structure.</p>
 * 
 * @author Bernd Späth, Wladimir Spindler and Saman Sedighi Rad
 */
public class ConfigLoader {
	
	public ConfigLoader() {
		LogHelper.getLogger().info("Objekt erzeugt: Configloader");
	}
	
	/**
	 * Creates the helper method to parse the values read from the file which
	 * holds the csv list of the height map data. This implementation uses a
	 * {@link Scanner}, an alternative is {@link StringTokenizer}.
	 * 
	 * @param reader
	 *            This is parsed as coma separated list.
	 * @return Each line of this 2d integer array corresponds to the line within
	 *         the csv file and each coma separated value is a separate colum.
	 */
	private int[][] parseCsv(BufferedReader reader) {
		String line;
		List<Integer> list = new ArrayList<Integer>();
		List<int[]> rows = new ArrayList<int[]>();

		try {
			while ((line = reader.readLine()) != null) {
				Scanner scanner = new Scanner(line);
				scanner.useDelimiter("\\s*[^0-9]+\\s*");
				scanner.useLocale(Locale.US);

				// we want to skip empty lines
				if (!scanner.hasNextInt())
					continue;

				list.clear();

				while (scanner.hasNextInt())
					list.add(scanner.nextInt());

				int row[] = new int[list.size()];
				for (int i = 0; i < list.size(); ++i)
					row[i] = list.get(i);
				rows.add(row);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[][] csv = new int[rows.size()][];
		for (int i = 0; i < rows.size(); ++i)
			csv[i] = rows.get(i);
		
		return csv;
	}
	
	/**
	 * Reads the actual file and returns its content as a string.
	 * 
	 * @param csvFileName
	 *            File name of the csv file.
	 * @return File contents as a {@link String}.
	 */
	public String readCSV(String csvFileName) {
		StringBuffer sb = new StringBuffer();
		File csv = new File(csvFileName);

		try {
			FileReader reader = new FileReader(csv);

			int code = reader.read();
			while (code != (-1)) {
				sb.append((char) code);
				code = reader.read();
			}
			LogHelper.getLogger().info("Datei gelesen: " + csvFileName);
			return sb.toString();
		} catch (FileNotFoundException e) {
			LogHelper.getLogger().error("Datei nicht gefunden: " + csvFileName);
			return null;
		} catch (IOException e) {
			LogHelper.getLogger().error(e.getCause());
			return null;
		}
	}

	/**
	 * Uses helper methods within the {@link ConfigLoader} to load the
	 * configuration file.
	 * 
	 * @param fileName
	 *            File which holds the information which represents the map
	 *            within the game.
	 * @return The {@link MapConfig} is the logical representation of the map
	 *         data which is read from the configuration file.
	 */
	public MapConfig loadMapConfig(String fileName) {
		try {
			File xmlFile = new File("data/maps/" + fileName);

			JAXBContext jaxbContext = JAXBContext.newInstance(MapConfig.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			MapConfig map = (MapConfig) jaxbUnmarshaller.unmarshal(xmlFile);

			LogHelper.getLogger().info("Map Konfiguration geladen: " + map.getMapName());

			map.setMapLayout(readCSV("data/maps/" + map.getHexMapCSV()));

			map.csv = parseCsv(new BufferedReader(new StringReader(map.getMapLayout())));
			map.height = map.csv.length;
			map.width = map.csv[0].length;

			return map;
		} catch (JAXBException jaxbe) {
			LogHelper.getLogger().error(jaxbe.getCause());
			return null;
		}
	}

}
