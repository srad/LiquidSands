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

package de.frankfurt.uni.vcp;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.system.AppSettings;

import de.frankfurt.uni.vcp.game.Game;
import de.frankfurt.uni.vcp.game.GameSettings;
import de.frankfurt.uni.vcp.helpers.LogHelper;

/**
 * <h3>Visual-Computing Praktikum</h3>
 * 
 * <p>
 * Main starting point, to launch the game.
 * </p>
 * 
 * @author Bernd Spaeth, Wladimir Spindler and Saman Sedighi Rad
 */
public class Main {

	/**
	 * Entry point for application.
	 * 
	 * @param args
	 *            Arguments currently not used.
	 */
	public static void main(String[] args) {
		try {
			Logger.getLogger("").setLevel(Level.WARNING);

			Game game = Game.getInstance();
			GameSettings gameSettings = game.getGameSettings();

			AppSettings settings = new AppSettings(true);
			settings.setFrameRate(30);
			settings.setResolution(gameSettings.gethResolution(), gameSettings.getvResolution());
			settings.setFullscreen(gameSettings.isFullScreen());
			settings.setTitle("Liquid Sands");

			game.setSettings(settings);
			game.setShowSettings(false);

			game.start();
		} catch (Exception e) {
			LogHelper.getLogger().error("Game could not be started: " + e.getMessage());
		}
	}

}