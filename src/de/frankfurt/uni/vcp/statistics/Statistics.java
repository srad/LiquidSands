package de.frankfurt.uni.vcp.statistics;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * <p>
 * This class holds game wide statistics about all actions within the game in:
 * <ol>
 * <li>Global statistic are hold in {@linkplain #gameStatistic} which is a
 * {@linkplain GameStatistics} object</li>
 * <li>or by units {@linkplain #unitStatistics} or</li>
 * <li>by single players {@linkplain #playersStatistics}.</li>
 * </ol>
 * <p>
 * Units and players are a set of unique statistics identified by name.
 * Therefore they are represented by a {@linkplain Hashtable} which identifies
 * them by {@linkplain String} (their name) and holds an object of type
 * {@linkplain SingleStatistic}.
 * </p>
 * </p>
 * <p>
 * The class constructor also handles internally automatically persistence for
 * the statistics.
 * </p>
 * <p>
 * A file is created in the project root with name {@linkplain #FILE_NAME}.
 * </p>
 * 
 */
public class Statistics implements Serializable {

	private static final long serialVersionUID = -4860400148443549948L;

	private Hashtable<String, SingleStatistic> playersStatistics;

	private Hashtable<String, SingleStatistic> unitStatistics;

	private SingleStatistic gameStatistic;

	private static final String FILE_NAME = "statistics.dat";

	/**
	 * The constructor will try to load an existing statistics, if it's not
	 * found it will create a new file.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public Statistics() throws IOException, ClassNotFoundException {
		try {
			Statistics statistics = load();
			this.setPlayersStatistics(statistics.getPlayersStatistics());
			this.setUnitStatistics(statistics.getUnitStatistics());
			this.setGameStatistic(statistics.getGameStatistic());

		} catch (FileNotFoundException e) {
			this.setPlayersStatistics(new Hashtable<String, SingleStatistic>());
			this.setUnitStatistics(new Hashtable<String, SingleStatistic>());
			this.setGameStatistic(new SingleStatistic());
			save(this);
		}
	}

	/**
	 * Internal method to load an existing {@linkplain Statistics} form a file.
	 * 
	 * @return Instance of {@linkplain Statistics}.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static Statistics load() throws FileNotFoundException, IOException,
			ClassNotFoundException {

		FileInputStream savedFile = new FileInputStream(FILE_NAME);
		ObjectInputStream save = new ObjectInputStream(savedFile);

		return (Statistics) save.readObject();
	}

	/**
	 * Internal static method to save an instance to {@linkplain Statistics}.
	 * 
	 * @param statistics
	 * @throws IOException
	 */
	private static void save(Statistics statistics) throws IOException {
		FileOutputStream saveFile = new FileOutputStream(FILE_NAME);
		ObjectOutputStream save = new ObjectOutputStream(saveFile);

		save.writeObject(statistics);
		save.close();
	}

	/**
	 * Saves the current settings, it must be forced manually.
	 * 
	 * @throws IOException
	 *             File could not be saved...
	 */
	public void save() throws IOException {
		save(this);
	}

	/**
	 * Returns all statistics.
	 * 
	 * @return {@linkplain Hashtable} containing the statistics of all players
	 */
	public Hashtable<String, SingleStatistic> getPlayersStatistics() {
		return playersStatistics;
	}

	/**
	 * Returns the statistics of an single player. If the player is not yet in the list he will be created.
	 * 
	 * @param playerName
	 *            Players name.
	 * @return the {@linkplain SingleStatistic} of an single player.
	 */
	public SingleStatistic getPlayersStatistics(String playerName) {
		if (!playersStatistics.containsKey(playerName)) {
			setPlayersStatistics(playerName, new SingleStatistic());
		}
		return playersStatistics.get(playerName);
	}

	/**
	 * Set all statistics, be careful not to overwrite existing statistics. You
	 * might want to use
	 * {@linkplain #setPlayersStatistics(String, SingleStatistic)} for a single
	 * player.
	 * 
	 * @param playersStatistics
	 */
	public void setPlayersStatistics(
			Hashtable<String, SingleStatistic> playersStatistics) {
		this.playersStatistics = playersStatistics;
	}

	/**
	 * Sets the {@linkplain SingleStatistic} of an single player.	
	 * @param playerName
	 * @param singleStatistic
	 */
	public void setPlayersStatistics(String playerName,	SingleStatistic singleStatistic) {
		playersStatistics.put(playerName, singleStatistic);
	}

	/**
	 * Returns all unit statistics.
	 * 
	 * @return {@linkplain Hashtable} containing the statistics of all units.
	 */
	public Hashtable<String, SingleStatistic> getUnitStatistics() {
		return unitStatistics;
	}

	/**
	 * Returns the statistics of an single unit.
	 * 
	 * @param unitName
	 *            Units name.
	 * @return the {@linkplain SingleStatistic} of an unit.
	 */
	public SingleStatistic getUnitStatistics(String unitName) {
		if (!unitStatistics.contains(unitName)) {
			unitStatistics.put(unitName, new SingleStatistic());
		}
		return unitStatistics.get(unitName);
	}

	/**
	 * Sets the statistics for all units.
	 * @param unitStatistics
	 */
	public void setUnitStatistics(
			Hashtable<String, SingleStatistic> unitStatistics) {
		this.unitStatistics = unitStatistics;
	}

	/**
	 * Sets the statistics of an single unit.
	 * @param unitName Name of the unit.
	 * @param singleStatistic
	 */
	public void setUnitStatistics(String unitName, SingleStatistic singleStatistic) {
		unitStatistics.put(unitName, singleStatistic);
	}

	/**
	 * Returns the game wide statistics.
	 * @return
	 */
	public SingleStatistic getGameStatistic() {
		return gameStatistic;
	}

	public void setGameStatistic(SingleStatistic gameStatistic) {
		this.gameStatistic = gameStatistic;
	}

}
